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
    void detalheReconciliaSaldoIncluiPagadorForaDoConsumoEProtegeAcesso() throws Exception {
        criar("100.03", pa.getId(), List.of(pa.getId(), pb.getId()));
        criar("8.00", pb.getId(), List.of(pa.getId()));
        em.flush();
        em.clear();
        var result =
                mvc.perform(get(url() + "/" + pb.getId()).with(user(ana.getEmail())))
                        .andExpect(status().isOk())
                        .andReturn();
        var d = json.readTree(result.getResponse().getContentAsString());
        assertEquals("-42.01", d.at("/participante/saldo").asText());
        assertEquals(2, d.get("despesas").size());
        var soma = new BigDecimal("0.00");
        for (var item : d.get("despesas"))
            soma = soma.add(new BigDecimal(item.get("impacto").asText()));
        assertEquals(new BigDecimal("-42.01"), soma);
        assertEquals("0.00", d.at("/despesas/0/parte").asText());
        mvc.perform(get(url() + "/" + pb.getId()).with(user(fora.getEmail())))
                .andExpect(status().isNotFound());
        mvc.perform(get(url() + "/999999999").with(user(ana.getEmail())))
                .andExpect(status().isNotFound());
        mvc.perform(get(url() + "/" + pb.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void historicoRespeitaVinculoDataEOrdenacao() throws Exception {
        var outro = grupos.saveAndFlush(new Grupo("Privado", null, fora));
        membros.saveAndFlush(new GrupoParticipante(outro, fora));
        mvc.perform(get("/api/grupos/historico").with(user(ana.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(grupo.getId()));
        mvc.perform(get("/api/grupos/historico?ate=2000-01-01").with(user(ana.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/grupos/historico?ate=errada").with(user(ana.getEmail())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sugestoesResumoEControleDeAcesso() throws Exception {
        criar("100.03", pa.getId(), List.of(pa.getId(), pb.getId()));
        var endpoint = "/api/grupos/" + grupo.getId() + "/acertos/sugestoes";
        mvc.perform(get(endpoint).with(user(bruno.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sugestoes[0].valor").value("50.01"))
                .andExpect(jsonPath("$.sugestoes[0].devedorId").value(pb.getId()))
                .andExpect(
                        jsonPath("$.resumo").value(org.hamcrest.Matchers.containsString("100,03")));
        mvc.perform(get(endpoint).with(user(fora.getEmail()))).andExpect(status().isNotFound());
        mvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
        despesas.saveAndFlush(
                new Despesa(grupo, "Legada", new BigDecimal("1.00"), LocalDate.now()));
        mvc.perform(get(endpoint).with(user(ana.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completo").value(false))
                .andExpect(jsonPath("$.sugestoes").isEmpty())
                .andExpect(jsonPath("$.resumo").isEmpty());
    }

    @Test
    void pagamentoNaoDuplicaPreservaDespesasERecalculaAposCorrecaoECancelamento() throws Exception {
        long despesa = criar("100.03", pa.getId(), List.of(pa.getId(), pb.getId()));
        var endpoint = "/api/grupos/" + grupo.getId() + "/acertos";
        var consulta =
                json.readTree(
                        mvc.perform(get(endpoint + "/sugestoes").with(user(bruno.getEmail())))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        var corpo =
                json.writeValueAsString(
                        Map.of(
                                "devedorId",
                                pb.getId(),
                                "recebedorId",
                                pa.getId(),
                                "valor",
                                "50.01",
                                "revisao",
                                consulta.get("revisao").asText()));
        mvc.perform(
                        post(endpoint)
                                .with(user(fora.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(corpo))
                .andExpect(status().isNotFound());
        membros.saveAndFlush(new GrupoParticipante(grupo, fora));
        consulta =
                json.readTree(
                        mvc.perform(get(endpoint + "/sugestoes").with(user(bruno.getEmail())))
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        corpo =
                json.writeValueAsString(
                        Map.of(
                                "devedorId",
                                pb.getId(),
                                "recebedorId",
                                pa.getId(),
                                "valor",
                                "50.01",
                                "revisao",
                                consulta.get("revisao").asText()));
        mvc.perform(
                        post(endpoint)
                                .with(user(fora.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(corpo))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post(endpoint)
                                .with(user(bruno.getEmail()))
                                .contentType("application/json")
                                .content(corpo))
                .andExpect(status().isForbidden());
        var pagamento =
                json.readTree(
                        mvc.perform(
                                        post(endpoint)
                                                .with(user(bruno.getEmail()))
                                                .with(csrf())
                                                .contentType("application/json")
                                                .content(corpo))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        mvc.perform(
                        post(endpoint)
                                .with(user(bruno.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(corpo))
                .andExpect(status().isConflict());
        mvc.perform(get(endpoint + "/sugestoes").with(user(ana.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sugestoes").isEmpty())
                .andExpect(jsonPath("$.registros[0].status").value("PAGO"));
        assertEquals("50.01", consultar().at("/participantes/0/saldo").asText());
        mvc.perform(
                        put(despesasUrl() + "/" + despesa)
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
                                                                "120.00",
                                                                pa.getId(),
                                                                List.of(pa.getId(), pb.getId()))))))
                .andExpect(status().isOk());
        mvc.perform(get(endpoint + "/sugestoes").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.sugestoes[0].valor").value("9.99"));
        mvc.perform(
                        post(endpoint + "/" + pagamento.get("id").asLong() + "/cancelamento")
                                .with(user(ana.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content("{\"versao\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(get(endpoint + "/sugestoes").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.sugestoes[0].valor").value("60.00"));
        mvc.perform(
                        delete(despesasUrl() + "/" + despesa)
                                .param("versao", "1")
                                .with(user(ana.getEmail()))
                                .with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(
                        delete("/api/grupos/" + grupo.getId() + "/participantes/" + pb.getId())
                                .with(user(ana.getEmail()))
                                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void personalizadaPersisteEValidaSomaSemAlterarDespesaEmFalha() throws Exception {
        var custom =
                new HashMap<String, Object>(
                        dados("10.00", pa.getId(), List.of(pa.getId(), pb.getId())));
        custom.put(
                "valoresIndividuais",
                Map.of(pa.getId().toString(), "2.00", pb.getId().toString(), "8.00"));
        var response =
                mvc.perform(
                                post(despesasUrl())
                                        .with(user(ana.getEmail()))
                                        .with(csrf())
                                        .contentType("application/json")
                                        .content(json.writeValueAsString(custom)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.tipoDivisao").value("PERSONALIZADA"))
                        .andReturn();
        var id = json.readTree(response.getResponse().getContentAsString()).get("id").asLong();
        assertEquals("8.00", consultar().at("/participantes/0/saldo").asText());
        mvc.perform(get(url() + "/" + pb.getId()).with(user(bruno.getEmail())))
                .andExpect(jsonPath("$.despesas[0].parte").value("8.00"));
        custom.put(
                "valoresIndividuais",
                Map.of(pa.getId().toString(), "1.00", pb.getId().toString(), "8.00"));
        mvc.perform(
                        put(despesasUrl() + "/" + id)
                                .with(user(ana.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(
                                        json.writeValueAsString(
                                                Map.of("versao", 0, "dados", custom))))
                .andExpect(status().isConflict());
        em.clear();
        mvc.perform(get(despesasUrl() + "/" + id).with(user(ana.getEmail())))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.participantes[0].valorParte").value("2.00"));
        custom.put(
                "valoresIndividuais",
                Map.of(pa.getId().toString(), "-1.00", pb.getId().toString(), "11.00"));
        mvc.perform(
                        post(despesasUrl())
                                .with(user(ana.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(json.writeValueAsString(custom)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void progressoPendenciasEEstornoAposExcluirDespesa() throws Exception {
        var base = "/api/grupos/" + grupo.getId();
        mvc.perform(get(base + "/progresso").with(user(ana.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semAcertos").value(true))
                .andExpect(jsonPath("$.valorPendente").value("0.00"));
        long id = criar("20.00", pa.getId(), List.of(pa.getId(), pb.getId()));
        mvc.perform(get(base + "/pendencias").with(user(bruno.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes.length()").value(2));
        mvc.perform(get(base + "/progresso").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.percentualConcluido").value("0.00"))
                .andExpect(jsonPath("$.pendentes").value(1));
        var consulta =
                json.readTree(
                        mvc.perform(get(base + "/acertos/sugestoes").with(user(ana.getEmail())))
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        var registro =
                json.readTree(
                        mvc.perform(
                                        post(base + "/acertos")
                                                .with(user(ana.getEmail()))
                                                .with(csrf())
                                                .contentType("application/json")
                                                .content(
                                                        json.writeValueAsString(
                                                                Map.of(
                                                                        "devedorId",
                                                                        pb.getId(),
                                                                        "recebedorId",
                                                                        pa.getId(),
                                                                        "valor",
                                                                        "10.00",
                                                                        "revisao",
                                                                        consulta.get("revisao")
                                                                                .asText()))))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString());
        mvc.perform(get(base + "/progresso").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.percentualConcluido").value("100.00"))
                .andExpect(jsonPath("$.quitados").value(1))
                .andExpect(jsonPath("$.pendentes").value(0));
        mvc.perform(
                        delete(despesasUrl() + "/" + id)
                                .param("versao", "0")
                                .with(user(ana.getEmail()))
                                .with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(base + "/acertos/sugestoes").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.sugestoes[0].devedorId").value(pa.getId()))
                .andExpect(jsonPath("$.sugestoes[0].valor").value("10.00"));
        mvc.perform(get(base + "/progresso").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.percentualConcluido").value("50.00"))
                .andExpect(jsonPath("$.valorPago").value("10.00"));
        mvc.perform(
                        post(base + "/acertos/" + registro.get("id").asLong() + "/cancelamento")
                                .with(user(bruno.getEmail()))
                                .with(csrf())
                                .contentType("application/json")
                                .content("{\"versao\":0}"))
                .andExpect(status().isOk());
        mvc.perform(get(base + "/progresso").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.semAcertos").value(true))
                .andExpect(jsonPath("$.quitados").value(0))
                .andExpect(jsonPath("$.valorPago").value("0.00"));
    }

    @Test
    void acompanhamentoNaoExibeQuitacaoFalsaQuandoHaLegadoEProtegeAcesso() throws Exception {
        despesas.saveAndFlush(new Despesa(grupo, "Antiga", BigDecimal.ONE, LocalDate.now()));
        for (var rota : List.of("progresso", "pendencias")) {
            var endpoint = "/api/grupos/" + grupo.getId() + "/" + rota;
            mvc.perform(get(endpoint).with(user(ana.getEmail())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completo").value(false))
                    .andExpect(jsonPath("$.participantes").isEmpty());
            mvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
            mvc.perform(get(endpoint).with(user(fora.getEmail()))).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/grupos/" + grupo.getId() + "/progresso").with(user(ana.getEmail())))
                .andExpect(jsonPath("$.percentualConcluido").isEmpty())
                .andExpect(jsonPath("$.quitados").isEmpty());
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
