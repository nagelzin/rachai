package br.com.rachai;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.rachai.model.*;
import br.com.rachai.repository.*;

import com.fasterxml.jackson.databind.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "ORACLE_INTEGRATION_TEST", matches = "true")
class OracleSaldoTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired GrupoRepository grupos;
    @Autowired GrupoParticipanteRepository membros;
    @Autowired DespesaRepository despesas;
    @Autowired jakarta.persistence.EntityManager em;
    Usuario ana, bruno, fora;
    Grupo grupo;
    GrupoParticipante pa, pb;

    @BeforeEach
    void preparar() {
        var prefix = UUID.randomUUID().toString();
        // Nomes iguais não podem juntar os saldos de contas distintas.
        ana = usuarios.saveAndFlush(new Usuario("Pessoa", prefix + "a@example.test", "hash"));
        bruno = usuarios.saveAndFlush(new Usuario("Pessoa", prefix + "b@example.test", "hash"));
        fora = usuarios.saveAndFlush(new Usuario("Outra", prefix + "c@example.test", "hash"));
        grupo = grupos.saveAndFlush(new Grupo("Saldos teste", null, ana));
        pa = membros.saveAndFlush(new GrupoParticipante(grupo, ana));
        pb = membros.saveAndFlush(new GrupoParticipante(grupo, bruno));
    }

    String url() {
        return "/api/grupos/" + grupo.getId() + "/saldos";
    }

    String despesasUrl() {
        return "/api/grupos/" + grupo.getId() + "/despesas";
    }

    Map<String, Object> dados(String valor, Long pagador, List<Long> envolvidos) {
        return Map.of(
                "descricao",
                "Teste saldo",
                "valorTotal",
                valor,
                "data",
                "2026-10-19",
                "pagadorId",
                pagador,
                "participanteIds",
                envolvidos);
    }

    JsonNode consultar() throws Exception {
        em.flush();
        em.clear();
        var response =
                mvc.perform(get(url()).with(user(bruno.getEmail())))
                        .andExpect(status().isOk())
                        .andExpect(
                                header().string(
                                                "Cache-Control",
                                                org.hamcrest.Matchers.containsString("no-store")))
                        .andReturn();
        return json.readTree(response.getResponse().getContentAsString());
    }

    long criar(String valor, Long pagador, List<Long> envolvidos) throws Exception {
        var result =
                mvc.perform(
                                post(despesasUrl())
                                        .with(user(ana.getEmail()))
                                        .with(csrf())
                                        .contentType("application/json")
                                        .content(
                                                json.writeValueAsString(
                                                        dados(valor, pagador, envolvidos))))
                        .andExpect(status().isCreated())
                        .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void consultaDerivadaRefleteCadastroEdicaoExclusaoESemMovimento() throws Exception {
        var vazio = consultar();
        assertTrue(vazio.get("completo").asBoolean());
        assertEquals("0.00", vazio.at("/participantes/0/saldo").asText());
        long id = criar("100.03", pa.getId(), List.of(pa.getId(), pb.getId()));
        var r = consultar();
        assertEquals(2, r.get("participantes").size());
        assertEquals(pa.getId().longValue(), r.at("/participantes/0/participanteId").asLong());
        assertEquals("50.01", r.at("/participantes/0/saldo").asText());
        assertEquals("RECEBER", r.at("/participantes/0/situacao").asText());
        assertEquals("-50.01", r.at("/participantes/1/saldo").asText());
        assertEquals("PAGAR", r.at("/participantes/1/situacao").asText());
        assertEquals(0, despesas.findById(id).orElseThrow().getVersao());
        mvc.perform(
                        put(despesasUrl() + "/" + id)
                                .with(user(ana.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "versao",
                                                        0,
                                                        "dados",
                                                        dados(
                                                                "60.00",
                                                                pb.getId(),
                                                                List.of(pa.getId()))))))
                .andExpect(status().isOk());
        r = consultar();
        assertEquals("-60.00", r.at("/participantes/0/saldo").asText());
        assertEquals("60.00", r.at("/participantes/1/saldo").asText());
        mvc.perform(
                        delete(despesasUrl() + "/" + id)
                                .param("versao", "1")
                                .with(user(ana.getEmail()))
                                .with(csrf()))
                .andExpect(status().isNoContent());
        r = consultar();
        assertEquals("ZERADO", r.at("/participantes/0/situacao").asText());
        assertEquals("0.00", r.at("/participantes/1/saldo").asText());
    }

    @Test
    void legadoBloqueiaSaldoAteCorrecaoExplicita() throws Exception {
        criar("10.00", pa.getId(), List.of(pb.getId()));
        var antiga =
                despesas.saveAndFlush(
                        new Despesa(grupo, "Legada", new BigDecimal("8.00"), LocalDate.now()));
        var r = consultar();
        assertFalse(r.get("completo").asBoolean());
        assertTrue(r.get("participantes").isEmpty());
        assertEquals(antiga.getId().longValue(), r.at("/despesasPendentes/0").asLong());
        assertNull(despesas.findById(antiga.getId()).orElseThrow().getPagador());
        mvc.perform(
                        put(despesasUrl() + "/" + antiga.getId())
                                .with(user(ana.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        json.writeValueAsString(
                                                Map.of(
                                                        "versao",
                                                        0,
                                                        "dados",
                                                        dados(
                                                                "8.00",
                                                                pb.getId(),
                                                                List.of(pa.getId()))))))
                .andExpect(status().isOk());
        r = consultar();
        assertTrue(r.get("completo").asBoolean());
        assertTrue(r.get("despesasPendentes").isEmpty());
        assertEquals("2.00", r.at("/participantes/0/saldo").asText());
        assertEquals("-2.00", r.at("/participantes/1/saldo").asText());
    }

    @Test
    void acessoRestritoAoGrupoERevogadoNaRemocao() throws Exception {
        mvc.perform(get(url())).andExpect(status().isUnauthorized());
        mvc.perform(get(url()).with(user(fora.getEmail()))).andExpect(status().isNotFound());
        mvc.perform(get("/api/grupos/999999999/saldos").with(user(ana.getEmail())))
                .andExpect(status().isNotFound());
        consultar();
        mvc.perform(
                        delete("/api/grupos/" + grupo.getId() + "/participantes/" + pb.getId())
                                .with(user(ana.getEmail()))
                                .with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(url()).with(user(bruno.getEmail()))).andExpect(status().isNotFound());
    }
}
