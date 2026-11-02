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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/** Filtros reais, services, JPA e Oracle. Dados revertidos ao terminar cada teste. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "ORACLE_INTEGRATION_TEST", matches = "true")
class OracleOnda1Test {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder senhas;
    Usuario ana, bruno, carla;
    final String senha = "Frase de teste longa 2026!";

    @BeforeEach
    void contas() {
        String prefix = UUID.randomUUID().toString();
        String hash = senhas.encode(senha);
        ana = usuarios.saveAndFlush(new Usuario("Ana", prefix + "a@example.test", hash));
        bruno = usuarios.saveAndFlush(new Usuario("Bruno", prefix + "b@example.test", hash));
        carla = usuarios.saveAndFlush(new Usuario("Ana", prefix + "c@example.test", hash));
    }

    MockHttpServletRequestBuilder as(Usuario u, MockHttpServletRequestBuilder r) {
        return r.with(user(u.getEmail())).with(csrf());
    }

    MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder r, Object v) throws Exception {
        return r.contentType("application/json").content(json.writeValueAsString(v));
    }

    JsonNode data(MvcResult r) throws Exception {
        return json.readTree(r.getResponse().getContentAsString());
    }

    long grupo() throws Exception {
        return data(mvc.perform(as(ana, body(post("/api/grupos"), Map.of("nome", "Teste privado"))))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    long convite(long g, Usuario u) throws Exception {
        return data(mvc.perform(
                                as(
                                        ana,
                                        body(
                                                post("/api/grupos/" + g + "/convites"),
                                                Map.of(
                                                        "email",
                                                        u.getEmail().toUpperCase(Locale.ROOT)))))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    ResultActions responder(Usuario u, long convite, boolean aceitar) throws Exception {
        return mvc.perform(
                as(
                        u,
                        body(
                                post("/api/convites/" + convite + "/resposta"),
                                Map.of("aceitar", aceitar))));
    }

    long pagador(long g) {
        return jdbc.queryForObject(
                "select id from grupo_participantes where grupo_id=? and usuario_id=?",
                Long.class,
                g,
                ana.getId());
    }

    @Test
    void anonimoECsrfBloqueados() throws Exception {
        mvc.perform(get("/api/grupos")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/cadastro").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").param("email", ana.getEmail()).param("password", senha))
                .andExpect(status().isForbidden());
        mvc.perform(
                        post("/api/grupos")
                                .with(user(ana.getEmail()))
                                .contentType("application/json")
                                .content("{\"nome\":\"Teste\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastroNormalizaEmailESalvaApenasHash() throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        var r =
                Map.of(
                        "nome",
                        "  Nova Pessoa  ",
                        "email",
                        email.toUpperCase(Locale.ROOT),
                        "senha",
                        senha);
        mvc.perform(body(post("/api/auth/cadastro").with(csrf()), r))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        var u = usuarios.findByEmail(email).orElseThrow();
        assertNotEquals(senha, u.getSenhaHash());
        assertTrue(senhas.matches(senha, u.getSenhaHash()));
        assertEquals("Nova Pessoa", u.getNome());
        mvc.perform(body(post("/api/auth/cadastro").with(csrf()), r))
                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaSenhaCurtaEDadosInvalidos() throws Exception {
        mvc.perform(
                        body(
                                post("/api/auth/cadastro").with(csrf()),
                                Map.of("nome", " ", "email", "errado", "senha", "curta")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRealRenovaSessaoLogoutRevogaEAceitaEmailMaiusculo() throws Exception {
        var result = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession();
        var token = data(result);
        String oldId = session.getId();
        mvc.perform(
                        post("/api/auth/login")
                                .session(session)
                                .header(
                                        token.get("headerName").asText(),
                                        token.get("token").asText())
                                .param("email", ana.getEmail().toUpperCase(Locale.ROOT))
                                .param("password", senha))
                .andExpect(status().isNoContent());
        assertNotEquals(oldId, session.getId());
        mvc.perform(get("/api/auth/eu").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ana.getId()))
                .andExpect(
                        header().string(
                                        "Cache-Control",
                                        org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/eu")).andExpect(status().isUnauthorized());
        mvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .param("email", ana.getEmail())
                                .param("password", "senha incorreta"))
                .andExpect(status().isUnauthorized());
        var legado = usuarios.saveAndFlush(new Usuario("Legado"));
        assertNull(legado.getSenhaHash());
    }

    @Test
    void criadorEMembroEOutroNaoVeNemManipula() throws Exception {
        long g = grupo();
        String base = "/api/grupos/" + g;
        mvc.perform(as(ana, get("/api/grupos"))).andExpect(jsonPath("$[0].id").value(g));
        mvc.perform(as(bruno, get("/api/grupos"))).andExpect(content().json("[]"));
        mvc.perform(as(bruno, get(base))).andExpect(status().isNotFound());
        mvc.perform(as(bruno, get(base + "/participantes"))).andExpect(status().isNotFound());
        mvc.perform(
                        as(
                                bruno,
                                body(
                                        post(base + "/despesas"),
                                        Map.of(
                                                "descricao",
                                                "x",
                                                "valorTotal",
                                                "1.00",
                                                "data",
                                                "2026-10-13",
                                                "pagadorId",
                                                pagador(g),
                                                "participanteIds",
                                                List.of(pagador(g))))))
                .andExpect(status().isNotFound());
        mvc.perform(as(bruno, body(post(base + "/convites"), Map.of("email", carla.getEmail()))))
                .andExpect(status().isNotFound());
        mvc.perform(
                        as(
                                ana,
                                body(
                                        post("/api/grupos"),
                                        Map.of(
                                                "nome",
                                                "Teste",
                                                "usuarioResponsavelId",
                                                bruno.getId()))))
                .andExpect(status().isBadRequest());
        mvc.perform(as(ana, get(base + "/participantes")))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].administrador").value(true));
    }

    @Test
    void aceitarLiberaSomenteDestinatarioERemocaoRevoga() throws Exception {
        long g = grupo(), c = convite(g, bruno);
        String base = "/api/grupos/" + g;
        mvc.perform(as(bruno, get(base))).andExpect(status().isNotFound());
        mvc.perform(as(bruno, get("/api/convites"))).andExpect(jsonPath("$[0].id").value(c));
        mvc.perform(as(carla, get("/api/convites"))).andExpect(content().json("[]"));
        responder(carla, c, true).andExpect(status().isNotFound());
        responder(bruno, c, true).andExpect(status().isOk());
        responder(bruno, c, true).andExpect(status().isConflict());
        mvc.perform(as(bruno, get("/api/grupos"))).andExpect(jsonPath("$[0].id").value(g));
        mvc.perform(as(bruno, get(base))).andExpect(status().isOk());
        mvc.perform(as(bruno, get(base + "/convites"))).andExpect(status().isForbidden());
        mvc.perform(as(bruno, body(post(base + "/convites"), Map.of("email", carla.getEmail()))))
                .andExpect(status().isForbidden());
        long p =
                jdbc.queryForObject(
                        "select id from grupo_participantes where grupo_id=? and usuario_id=?",
                        Long.class,
                        g,
                        bruno.getId());
        mvc.perform(as(bruno, delete(base + "/participantes/" + p)))
                .andExpect(status().isForbidden());
        mvc.perform(as(ana, delete(base + "/participantes/" + p)))
                .andExpect(status().isNoContent());
        mvc.perform(as(bruno, get(base))).andExpect(status().isNotFound());
        mvc.perform(as(bruno, get("/api/grupos"))).andExpect(content().json("[]"));
        responder(bruno, c, true).andExpect(status().isConflict());
        assertEquals(c, convite(g, bruno));
        responder(bruno, c, true).andExpect(status().isOk());
    }

    @Test
    void recusarNaoLiberaEPodeReceberNovoConvite() throws Exception {
        long g = grupo(), c = convite(g, bruno);
        mvc.perform(
                        as(
                                ana,
                                body(
                                        post("/api/grupos/" + g + "/convites"),
                                        Map.of("email", bruno.getEmail()))))
                .andExpect(status().isConflict());
        responder(bruno, c, false).andExpect(status().isOk());
        mvc.perform(as(bruno, get("/api/grupos/" + g))).andExpect(status().isNotFound());
        mvc.perform(as(bruno, get("/api/convites"))).andExpect(content().json("[]"));
        assertEquals(c, convite(g, bruno));
    }

    @Test
    void mesmoNomePermitidoEAdminProtegido() throws Exception {
        long g = grupo(), c = convite(g, carla);
        responder(carla, c, true).andExpect(status().isOk());
        assertEquals(
                2,
                jdbc.queryForObject(
                        "select count(*) from grupo_participantes where grupo_id=? and nome='Ana'",
                        Integer.class,
                        g));
        long p =
                jdbc.queryForObject(
                        "select id from grupo_participantes where grupo_id=? and usuario_id=?",
                        Long.class,
                        g,
                        ana.getId());
        mvc.perform(as(ana, delete("/api/grupos/" + g + "/participantes/" + p)))
                .andExpect(status().isConflict());
        mvc.perform(
                        as(
                                ana,
                                body(
                                        post("/api/grupos/" + g + "/convites"),
                                        Map.of("email", carla.getEmail()))))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "9999999999.99"})
    void membroPersisteCentavosEVinculos(String valorEsperado) throws Exception {
        long g = grupo();
        responder(bruno, convite(g, bruno), true).andExpect(status().isOk());
        var r =
                mvc.perform(
                                as(
                                        bruno,
                                        body(
                                                post("/api/grupos/" + g + "/despesas"),
                                                Map.of(
                                                        "descricao",
                                                        "Mercado",
                                                        "valorTotal",
                                                        valorEsperado,
                                                        "data",
                                                        "2026-10-13",
                                                        "pagadorId",
                                                        pagador(g),
                                                        "participanteIds",
                                                        List.of(pagador(g))))))
                        .andExpect(status().isCreated())
                        .andReturn();
        long id = data(r).get("id").asLong();
        BigDecimal valor =
                jdbc.queryForObject(
                        "select valor_total from despesas where id=? and grupo_id=?",
                        BigDecimal.class,
                        id,
                        g);
        assertEquals(0, new BigDecimal(valorEsperado).compareTo(valor));
    }
}
