package br.com.rachai;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.rachai.model.Usuario;
import br.com.rachai.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.*;

/** Requisições em transações distintas, com limpeza restrita às contas criadas pelo teste. */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "ORACLE_INTEGRATION_TEST", matches = "true")
class OracleConcorrenciaTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder senhas;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @Test
    void convitesERespostasSimultaneosNaoDuplicamMembro() throws Exception {
        String prefix = UUID.randomUUID().toString();
        String hash = senhas.encode("Senha longa somente para teste");
        var admin =
                usuarios.saveAndFlush(
                        new Usuario("Teste concorrência", prefix + "a@example.test", hash));
        var membro =
                usuarios.saveAndFlush(
                        new Usuario("Teste concorrência", prefix + "b@example.test", hash));
        try (var pool = Executors.newFixedThreadPool(2)) {
            var grupo =
                    mvc.perform(
                                    post("/api/grupos")
                                            .with(user(admin.getEmail()))
                                            .with(csrf())
                                            .contentType("application/json")
                                            .content("{\"nome\":\"Grupo concorrência\"}"))
                            .andExpect(status().isCreated())
                            .andReturn();
            long g = json.readTree(grupo.getResponse().getContentAsString()).get("id").asLong();
            var start = new CountDownLatch(1);
            Callable<Integer> enviar =
                    () -> {
                        start.await();
                        return mvc.perform(
                                        post("/api/grupos/" + g + "/convites")
                                                .with(user(admin.getEmail()))
                                                .with(csrf())
                                                .contentType("application/json")
                                                .content(
                                                        json.writeValueAsString(
                                                                Map.of(
                                                                        "email",
                                                                        membro.getEmail()))))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    };
            var a = pool.submit(enviar);
            var b = pool.submit(enviar);
            start.countDown();
            var statusEnvio =
                    new ArrayList<>(
                            List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)));
            Collections.sort(statusEnvio);
            assertEquals(List.of(201, 409), statusEnvio);
            long convite =
                    jdbc.queryForObject(
                            "select id from convites where grupo_id=? and destinatario_id=?",
                            Long.class,
                            g,
                            membro.getId());
            var startResposta = new CountDownLatch(1);
            Callable<Integer> aceitar =
                    () -> {
                        startResposta.await();
                        return mvc.perform(
                                        post("/api/convites/" + convite + "/resposta")
                                                .with(user(membro.getEmail()))
                                                .with(csrf())
                                                .contentType("application/json")
                                                .content("{\"aceitar\":true}"))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    };
            var c = pool.submit(aceitar);
            var d = pool.submit(aceitar);
            startResposta.countDown();
            var statusResposta =
                    new ArrayList<>(
                            List.of(c.get(20, TimeUnit.SECONDS), d.get(20, TimeUnit.SECONDS)));
            Collections.sort(statusResposta);
            assertEquals(List.of(200, 409), statusResposta);
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "select count(*) from grupo_participantes where grupo_id=? and"
                                    + " usuario_id=?",
                            Integer.class,
                            g,
                            membro.getId()));
        } finally {
            new TransactionTemplate(transactions)
                    .executeWithoutResult(
                            status -> {
                                jdbc.update(
                                        "delete from convites where grupo_id in (select id from"
                                                + " grupos where usuario_responsavel_id=?)",
                                        admin.getId());
                                jdbc.update(
                                        "delete from grupo_participantes where grupo_id in (select"
                                                + " id from grupos where usuario_responsavel_id=?)",
                                        admin.getId());
                                jdbc.update(
                                        "delete from grupos where usuario_responsavel_id=?",
                                        admin.getId());
                                jdbc.update(
                                        "delete from usuarios where id in (?,?)",
                                        admin.getId(),
                                        membro.getId());
                            });
        }
    }

    @Test
    void duasEdicoesDaMesmaVersaoNaoSobrescrevemUmaOutra() throws Exception {
        var conta =
                usuarios.saveAndFlush(
                        new Usuario(
                                "Concorrência despesa",
                                UUID.randomUUID() + "@example.test",
                                "hash-teste"));
        try (var pool = Executors.newFixedThreadPool(2)) {
            var grupoResult =
                    mvc.perform(
                                    post("/api/grupos")
                                            .with(user(conta.getEmail()))
                                            .with(csrf())
                                            .contentType("application/json")
                                            .content("{\"nome\":\"Correção simultânea\"}"))
                            .andExpect(status().isCreated())
                            .andReturn();
            long g =
                    json.readTree(grupoResult.getResponse().getContentAsString())
                            .get("id")
                            .asLong();
            long p =
                    jdbc.queryForObject(
                            "select id from grupo_participantes where grupo_id=? and usuario_id=?",
                            Long.class,
                            g,
                            conta.getId());
            var dados =
                    new HashMap<String, Object>(
                            Map.of(
                                    "descricao",
                                    "Inicial",
                                    "valorTotal",
                                    "10.00",
                                    "data",
                                    "2026-10-17",
                                    "pagadorId",
                                    p,
                                    "participanteIds",
                                    List.of(p)));
            String base = "/api/grupos/" + g + "/despesas";
            var nova =
                    mvc.perform(
                                    post(base)
                                            .with(user(conta.getEmail()))
                                            .with(csrf())
                                            .contentType("application/json")
                                            .content(json.writeValueAsString(dados)))
                            .andExpect(status().isCreated())
                            .andReturn();
            long id = json.readTree(nova.getResponse().getContentAsString()).get("id").asLong();
            dados.put("descricao", "Corrigida");
            String body = json.writeValueAsString(Map.of("versao", 0, "dados", dados));
            var start = new CountDownLatch(1);
            Callable<Integer> editar =
                    () -> {
                        start.await();
                        return mvc.perform(
                                        put(base + "/" + id)
                                                .with(user(conta.getEmail()))
                                                .with(csrf())
                                                .contentType("application/json")
                                                .content(body))
                                .andReturn()
                                .getResponse()
                                .getStatus();
                    };
            var a = pool.submit(editar);
            var b = pool.submit(editar);
            start.countDown();
            var codigos =
                    new ArrayList<>(
                            List.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS)));
            Collections.sort(codigos);
            assertEquals(List.of(200, 409), codigos);
            assertEquals(
                    1L,
                    jdbc.queryForObject("select versao from despesas where id=?", Long.class, id));
        } finally {
            new TransactionTemplate(transactions)
                    .executeWithoutResult(
                            status -> {
                                jdbc.update(
                                        "delete from despesa_participantes where grupo_id in"
                                                + " (select id from grupos where"
                                                + " usuario_responsavel_id=?)",
                                        conta.getId());
                                jdbc.update(
                                        "delete from despesas where grupo_id in (select id from"
                                                + " grupos where usuario_responsavel_id=?)",
                                        conta.getId());
                                jdbc.update(
                                        "delete from grupo_participantes where grupo_id in (select"
                                                + " id from grupos where usuario_responsavel_id=?)",
                                        conta.getId());
                                jdbc.update(
                                        "delete from grupos where usuario_responsavel_id=?",
                                        conta.getId());
                                jdbc.update("delete from usuarios where id=?", conta.getId());
                            });
        }
    }
}
