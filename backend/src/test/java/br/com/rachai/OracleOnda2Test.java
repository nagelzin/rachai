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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "ORACLE_INTEGRATION_TEST", matches = "true")
class OracleOnda2Test {
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired GrupoRepository grupos;
    @Autowired GrupoParticipanteRepository membros;
    Usuario admin, membro, terceiro;
    Grupo grupo, outro;
    GrupoParticipante pa, pm, pt;

    @BeforeEach
    void preparar() {
        String prefix = UUID.randomUUID().toString();
        admin = usuarios.saveAndFlush(new Usuario("Ana", prefix + "a@example.test", "hash-teste"));
        membro =
                usuarios.saveAndFlush(
                        new Usuario("Bruno", prefix + "b@example.test", "hash-teste"));
        terceiro =
                usuarios.saveAndFlush(
                        new Usuario("Carla", prefix + "c@example.test", "hash-teste"));
        grupo = grupos.saveAndFlush(new Grupo("Onda 2", null, admin));
        outro = grupos.saveAndFlush(new Grupo("Outro", null, terceiro));
        pa = membros.saveAndFlush(new GrupoParticipante(grupo, admin));
        pm = membros.saveAndFlush(new GrupoParticipante(grupo, membro));
        pt = membros.saveAndFlush(new GrupoParticipante(outro, terceiro));
    }

    Map<String, Object> dados(Long pagador) {
        return new HashMap<>(
                Map.of(
                        "descricao",
                        "Mercado",
                        "valorTotal",
                        "123.45",
                        "data",
                        "2026-10-14",
                        "pagadorId",
                        pagador,
                        "participanteIds",
                        List.of(pa.getId())));
    }

    MockHttpServletRequestBuilder req(Usuario u, MockHttpServletRequestBuilder r, Object dados)
            throws Exception {
        return r.with(user(u.getEmail()))
                .with(csrf())
                .contentType("application/json")
                .content(json.writeValueAsString(dados));
    }

    JsonNode data(MvcResult r) throws Exception {
        return json.readTree(r.getResponse().getContentAsString());
    }

    String criarUrl() {
        return "/api/grupos/" + grupo.getId() + "/despesas";
    }

    @Test
    void previaNaoGravaEDivisaoPersistidaRecalculaNaEdicao() throws Exception {
        var p3 = membros.saveAndFlush(new GrupoParticipante(grupo, terceiro));
        var ids = List.of(p3.getId(), pm.getId(), pa.getId());
        var previo = Map.of("valorTotal", "100.00", "participanteIds", ids);
        mvc.perform(req(admin, post(criarUrl() + "/previa-divisao"), previo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pa.getId()))
                .andExpect(jsonPath("$[0].valorParte").value("33.34"))
                .andExpect(jsonPath("$[1].valorParte").value("33.33"))
                .andExpect(jsonPath("$[2].valorParte").value("33.33"));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from despesas where grupo_id=?",
                        Integer.class,
                        grupo.getId()));
        var body = dados(pm.getId());
        body.put("valorTotal", "100.00");
        body.put("participanteIds", ids);
        var created =
                data(
                        mvc.perform(req(admin, post(criarUrl()), body))
                                .andExpect(status().isCreated())
                                .andReturn());
        long id = created.get("id").asLong();
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get(criarUrl() + "/" + id).with(user(membro.getEmail())))
                .andExpect(jsonPath("$.participantes[0].valorParte").value("33.34"));
        assertEquals(
                0,
                new java.math.BigDecimal("100.00")
                        .compareTo(
                                jdbc.queryForObject(
                                        "select sum(valor_parte) from despesa_participantes where"
                                                + " despesa_id=?",
                                        java.math.BigDecimal.class,
                                        id)));
        body.put("valorTotal", "0.01");
        body.put("participanteIds", List.of(p3.getId(), pm.getId()));
        mvc.perform(req(admin, put(criarUrl() + "/" + id), Map.of("versao", 0, "dados", body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes.length()").value(2))
                .andExpect(jsonPath("$.participantes[0].valorParte").value("0.01"))
                .andExpect(jsonPath("$.participantes[1].valorParte").value("0.00"));
        assertEquals(
                0,
                new java.math.BigDecimal("0.01")
                        .compareTo(
                                jdbc.queryForObject(
                                        "select sum(valor_parte) from despesa_participantes where"
                                                + " despesa_id=?",
                                        java.math.BigDecimal.class,
                                        id)));
    }

    @Test
    void previaExigeMembroCsrfValorValidoEEnvolvidosDoGrupo() throws Exception {
        var body = Map.of("valorTotal", "100.00", "participanteIds", List.of(pa.getId()));
        String url = criarUrl() + "/previa-divisao";
        mvc.perform(req(terceiro, post(url), body)).andExpect(status().isNotFound());
        mvc.perform(
                        post(url)
                                .with(user(admin.getEmail()))
                                .contentType("application/json")
                                .content(json.writeValueAsString(body)))
                .andExpect(status().isForbidden());
        for (Object ids :
                List.of(List.of(), List.of(pa.getId(), pa.getId()), List.of(pt.getId()))) {
            mvc.perform(req(admin, post(url), Map.of("valorTotal", "1.00", "participanteIds", ids)))
                    .andExpect(status().is(((List<?>) ids).isEmpty() ? 400 : 409));
        }
        mvc.perform(
                        req(
                                admin,
                                post(url),
                                Map.of(
                                        "valorTotal",
                                        "1.001",
                                        "participanteIds",
                                        List.of(pa.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaCompletaOrdenadaPrivadaComCentavosDepoisDeLerDoOracle() throws Exception {
        mvc.perform(get(criarUrl())).andExpect(status().isUnauthorized());
        mvc.perform(get(criarUrl()).with(user(terceiro.getEmail())))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/grupos/9223372036854775807/despesas").with(user(admin.getEmail())))
                .andExpect(status().isNotFound());
        mvc.perform(get(criarUrl()).with(user(membro.getEmail())))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        var antigo = dados(pa.getId());
        antigo.put("descricao", "Antiga");
        antigo.put("data", "2026-10-01");
        long idAntigo =
                data(mvc.perform(req(admin, post(criarUrl()), antigo)).andReturn())
                        .get("id")
                        .asLong();
        var novo = dados(pa.getId());
        novo.put("valorTotal", "300");
        novo.put("descricao", "Mais recente");
        novo.put("participanteIds", List.of(pa.getId(), pm.getId()));
        long idNovo =
                data(mvc.perform(req(admin, post(criarUrl()), novo))
                                .andExpect(status().isCreated())
                                .andReturn())
                        .get("id")
                        .asLong();
        long idEmpate =
                data(mvc.perform(req(admin, post(criarUrl()), novo))
                                .andExpect(status().isCreated())
                                .andReturn())
                        .get("id")
                        .asLong();
        var alheia = new HashMap<>(novo);
        alheia.put("pagadorId", pt.getId());
        alheia.put("participanteIds", List.of(pt.getId()));
        mvc.perform(req(terceiro, post("/api/grupos/" + outro.getId() + "/despesas"), alheia))
                .andExpect(status().isCreated());
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get(criarUrl()).with(user(membro.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(idEmpate))
                .andExpect(jsonPath("$[1].id").value(idNovo))
                .andExpect(jsonPath("$[2].id").value(idAntigo))
                .andExpect(jsonPath("$[0].valorTotal").value("300.00"))
                .andExpect(jsonPath("$[0].pagador.nome").value("Ana"))
                .andExpect(jsonPath("$[0].participantes.length()").value(2));
        mvc.perform(get(criarUrl() + "/" + idNovo).with(user(admin.getEmail())))
                .andExpect(jsonPath("$.valorTotal").value("300.00"));
        antigo.put("data", "2026-10-20");
        mvc.perform(
                        req(
                                admin,
                                put(criarUrl() + "/" + idAntigo),
                                Map.of("versao", 0, "dados", antigo)))
                .andExpect(status().isOk());
        mvc.perform(get(criarUrl()).with(user(admin.getEmail())))
                .andExpect(jsonPath("$[0].id").value(idAntigo));
        mvc.perform(req(admin, delete(criarUrl() + "/" + idEmpate + "?versao=0"), Map.of()))
                .andExpect(status().isNoContent());
        mvc.perform(get(criarUrl()).with(user(admin.getEmail())))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].id").value(idNovo));
    }

    @Test
    void listaPreservaLegadoERevogaAcessoDoExMembro() throws Exception {
        jdbc.update(
                "insert into despesas(grupo_id, descricao, valor_total, data_despesa) values (?,"
                        + " 'Legada', 300, date '2026-10-01')",
                grupo.getId());
        mvc.perform(get(criarUrl()).with(user(membro.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].valorTotal").value("300.00"))
                .andExpect(jsonPath("$[0].pagador").isEmpty())
                .andExpect(jsonPath("$[0].participantes").isEmpty());
        membros.delete(pm);
        membros.flush();
        entityManager.clear();
        mvc.perform(get(criarUrl()).with(user(membro.getEmail()))).andExpect(status().isNotFound());
    }

    @Test
    void pagadorDoMesmoGrupoObrigatorioEProtegidoNaRemocao() throws Exception {
        mvc.perform(req(membro, post(criarUrl()), dados(pt.getId())))
                .andExpect(status().isConflict());
        var sem = dados(pm.getId());
        sem.remove("pagadorId");
        mvc.perform(req(membro, post(criarUrl()), sem)).andExpect(status().isBadRequest());
        var result =
                mvc.perform(req(membro, post(criarUrl()), dados(pm.getId())))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.pagador.id").value(pm.getId()))
                        .andExpect(jsonPath("$.pagador.nome").value("Bruno"))
                        .andReturn();
        long id = data(result).get("id").asLong();
        assertEquals(
                pm.getId(),
                jdbc.queryForObject(
                        "select pagador_participante_id from despesas where id=?", Long.class, id));
        mvc.perform(
                        req(
                                admin,
                                delete(
                                        "/api/grupos/"
                                                + grupo.getId()
                                                + "/participantes/"
                                                + pm.getId()),
                                Map.of()))
                .andExpect(status().isConflict());
    }

    @Test
    void envolvidosSaoObrigatoriosUnicosEDoMesmoGrupo() throws Exception {
        for (Object ids :
                List.of(List.of(), List.of(pm.getId(), pm.getId()), List.of(pt.getId()))) {
            var body = dados(pa.getId());
            body.put("participanteIds", ids);
            int esperado = ((List<?>) ids).isEmpty() ? 400 : 409;
            mvc.perform(req(admin, post(criarUrl()), body)).andExpect(status().is(esperado));
        }
        var body = dados(pa.getId());
        body.put("participanteIds", List.of(pm.getId()));
        var d =
                data(
                        mvc.perform(req(admin, post(criarUrl()), body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.participantes.length()").value(1))
                                .andExpect(jsonPath("$.participantes[0].id").value(pm.getId()))
                                .andReturn());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "select count(*) from despesa_participantes where despesa_id=? and"
                                + " participante_id=?",
                        Integer.class,
                        d.get("id").asLong(),
                        pm.getId()));
        mvc.perform(
                        req(
                                admin,
                                delete(
                                        "/api/grupos/"
                                                + grupo.getId()
                                                + "/participantes/"
                                                + pm.getId()),
                                Map.of()))
                .andExpect(status().isConflict());
    }

    long criar() throws Exception {
        return data(mvc.perform(req(membro, post(criarUrl()), dados(pm.getId())))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    @Test
    void consultaEdicaoExclusaoIsoladasEExigemCsrf() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id;
        var edicao = Map.of("versao", 0, "dados", dados(pa.getId()));
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(
                        put(url).with(user(membro.getEmail()))
                                .contentType("application/json")
                                .content(json.writeValueAsString(edicao)))
                .andExpect(status().isForbidden());
        mvc.perform(delete(url).param("versao", "0").with(user(membro.getEmail())))
                .andExpect(status().isForbidden());
        mvc.perform(req(terceiro, get(url), Map.of())).andExpect(status().isNotFound());
        mvc.perform(req(terceiro, put(url), edicao)).andExpect(status().isNotFound());
        mvc.perform(req(terceiro, delete(url).param("versao", "0"), Map.of()))
                .andExpect(status().isNotFound());
        mvc.perform(
                        req(
                                terceiro,
                                get("/api/grupos/" + outro.getId() + "/despesas/" + id),
                                Map.of()))
                .andExpect(status().isNotFound());
        mvc.perform(req(membro, get(url), Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(0));
    }

    @Test
    void edicaoTrocaVinculosEExclusaoLimpaSemApagarMembros() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id;
        var novos = dados(pa.getId());
        novos.put("descricao", "  Corrigida  ");
        novos.put("valorTotal", "99.99");
        novos.put("participanteIds", List.of(pm.getId()));
        mvc.perform(req(membro, put(url), Map.of("versao", 0, "dados", novos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.descricao").value("Corrigida"))
                .andExpect(jsonPath("$.valorTotal").value("99.99"))
                .andExpect(jsonPath("$.pagador.id").value(pa.getId()));
        assertEquals(
                List.of(pm.getId()),
                jdbc.queryForList(
                        "select participante_id from despesa_participantes where despesa_id=?",
                        Long.class,
                        id));
        mvc.perform(req(membro, put(url), Map.of("versao", 0, "dados", novos)))
                .andExpect(status().isConflict());
        mvc.perform(req(membro, delete(url).param("versao", "0"), Map.of()))
                .andExpect(status().isConflict());
        mvc.perform(req(membro, delete(url), Map.of())).andExpect(status().isBadRequest());
        mvc.perform(req(membro, delete(url).param("versao", "1"), Map.of()))
                .andExpect(status().isNoContent());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from despesa_participantes where despesa_id=?",
                        Integer.class,
                        id));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "select count(*) from grupo_participantes where grupo_id=?",
                        Integer.class,
                        grupo.getId()));
        mvc.perform(req(admin, get(url), Map.of())).andExpect(status().isNotFound());
        mvc.perform(req(admin, delete(url).param("versao", "1"), Map.of()))
                .andExpect(status().isNotFound());
    }

    @Test
    void alteracaoSomenteDosEnvolvidosTambemAvancaVersao() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id;
        var novos = dados(pm.getId());
        novos.put("participanteIds", List.of(pm.getId()));
        mvc.perform(req(membro, put(url), Map.of("versao", 0, "dados", novos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1));
        mvc.perform(req(membro, put(url), Map.of("versao", 1, "dados", novos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(2));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "select count(*) from despesa_participantes where despesa_id=?",
                        Integer.class,
                        id));
    }

    @Test
    void correcaoInvalidaNaoAlteraNada() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id;
        var invalidos = dados(pa.getId());
        invalidos.put("participanteIds", List.of(pt.getId()));
        mvc.perform(req(membro, put(url), Map.of("versao", 0, "dados", invalidos)))
                .andExpect(status().isConflict());
        invalidos.put("participanteIds", List.of(pa.getId()));
        invalidos.put("valorTotal", "1.999");
        mvc.perform(req(membro, put(url), Map.of("versao", 0, "dados", invalidos)))
                .andExpect(status().isBadRequest());
        mvc.perform(req(membro, put(url), Map.of("dados", dados(pa.getId()))))
                .andExpect(status().isBadRequest());
        mvc.perform(req(membro, get(url), Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.valorTotal").value("123.45"))
                .andExpect(jsonPath("$.pagador.id").value(pm.getId()));
    }

    @Autowired DespesaRepository despesas;

    @Test
    void despesaAntigaPreservadaECompletaSomenteNaEdicao() throws Exception {
        var antiga =
                despesas.saveAndFlush(
                        new Despesa(
                                grupo,
                                "Legada",
                                new java.math.BigDecimal("7.25"),
                                java.time.LocalDate.of(2026, 10, 12)));
        String url = criarUrl() + "/" + antiga.getId();
        mvc.perform(req(admin, get(url), Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagador").isEmpty())
                .andExpect(jsonPath("$.participantes").isEmpty())
                .andExpect(jsonPath("$.valorTotal").value("7.25"));
        var corrigidos = dados(pa.getId());
        corrigidos.put("valorTotal", "7.25");
        mvc.perform(req(admin, put(url), Map.of("versao", 0, "dados", corrigidos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagador.id").value(pa.getId()))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void oracleTambemImpedeVinculosEntreGrupos() throws Exception {
        long id = criar();
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "update despesas set pagador_participante_id=? where id=?",
                                pt.getId(),
                                id));
        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                "insert into"
                                    + " despesa_participantes(despesa_id,participante_id,grupo_id)"
                                    + " values(?,?,?)",
                                id,
                                pt.getId(),
                                grupo.getId()));
    }
}
