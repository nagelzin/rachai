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
class OracleComprovanteTest {
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

    long criar() throws Exception {
        return data(mvc.perform(req(admin, post(criarUrl()), dados(pa.getId())))
                        .andExpect(status().isCreated())
                        .andReturn())
                .get("id")
                .asLong();
    }

    byte[] png() throws Exception {
        var out = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(
                new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB),
                "png",
                out);
        return out.toByteArray();
    }

    org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder upload(
            String url, Usuario pessoa, long versao, byte[] bytes, String nome) {
        return (org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder)
                multipart(url)
                        .file(
                                new org.springframework.mock.web.MockMultipartFile(
                                        "arquivo", nome, "application/octet-stream", bytes))
                        .param("versao", Long.toString(versao))
                        .with(user(pessoa.getEmail()))
                        .with(csrf());
    }

    @Test
    void armazenaBlobSubstituiRemoveEExcluiJuntoComDespesa() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id + "/comprovante";
        byte[] imagem = png();
        mvc.perform(upload(url, membro, 0, imagem, "foto.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.comprovante.nome").value("foto.png"));
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get(url).with(user(admin.getEmail())))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imagem))
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        org.hamcrest.Matchers.containsString("sandbox")));
        mvc.perform(get(url + "?baixar=true").with(user(admin.getEmail())))
                .andExpect(
                        header().string(
                                        "Content-Disposition",
                                        org.hamcrest.Matchers.containsString("attachment")));
        assertArrayEquals(
                imagem,
                jdbc.queryForObject(
                        "select conteudo from comprovantes where despesa_id=?", byte[].class, id));
        mvc.perform(
                        req(
                                admin,
                                put(criarUrl() + "/" + id),
                                Map.of("versao", 1, "dados", dados(pm.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comprovante.nome").value("foto.png"));
        byte[] pdf =
                "%PDF-1.4\n1 0 obj <<>> endobj\n%%EOF"
                        .getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        mvc.perform(upload(url, admin, 2, pdf, "nota.pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(3))
                .andExpect(jsonPath("$.comprovante.tipo").value("application/pdf"));
        mvc.perform(upload(url, membro, 2, imagem, "antiga.png")).andExpect(status().isConflict());
        mvc.perform(get(url).with(user(membro.getEmail())))
                .andExpect(content().bytes(pdf))
                .andExpect(
                        header().string(
                                        "Content-Disposition",
                                        org.hamcrest.Matchers.containsString("attachment")));
        mvc.perform(req(admin, delete(url + "?versao=3"), Map.of()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(4))
                .andExpect(jsonPath("$.comprovante").isEmpty());
        mvc.perform(get(url).with(user(admin.getEmail()))).andExpect(status().isNotFound());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from comprovantes where despesa_id=?", Integer.class, id));
        mvc.perform(upload(url, admin, 4, imagem, "foto.png")).andExpect(status().isOk());
        mvc.perform(req(admin, delete(criarUrl() + "/" + id + "?versao=5"), Map.of()))
                .andExpect(status().isNoContent());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from comprovantes where despesa_id=?", Integer.class, id));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "select count(*) from grupo_participantes where grupo_id=?",
                        Integer.class,
                        grupo.getId()));
    }

    @Test
    void exigeSessaoCsrfEGrupoEmTodasAsOperacoes() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id + "/comprovante";
        byte[] imagem = png();
        mvc.perform(
                        multipart(url)
                                .file("arquivo", imagem)
                                .param("versao", "0")
                                .with(user(admin.getEmail())))
                .andExpect(status().isForbidden());
        mvc.perform(multipart(url).file("arquivo", imagem).param("versao", "0").with(csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(upload(url, terceiro, 0, imagem, "foto.png")).andExpect(status().isNotFound());
        mvc.perform(upload(url, admin, 0, imagem, "foto.png")).andExpect(status().isOk());
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).with(user(terceiro.getEmail()))).andExpect(status().isNotFound());
        mvc.perform(req(terceiro, delete(url + "?versao=1"), Map.of()))
                .andExpect(status().isNotFound());
        mvc.perform(
                        get("/api/grupos/" + outro.getId() + "/despesas/" + id + "/comprovante")
                                .with(user(terceiro.getEmail())))
                .andExpect(status().isNotFound());
        membros.delete(pm);
        membros.flush();
        entityManager.clear();
        mvc.perform(get(url).with(user(membro.getEmail()))).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaArquivoInvalidoSemMudarRegistroOuVersao() throws Exception {
        long id = criar();
        String url = criarUrl() + "/" + id + "/comprovante";
        mvc.perform(upload(url, admin, 0, "<html>fake</html>".getBytes(), "fake.pdf"))
                .andExpect(status().isBadRequest());
        mvc.perform(upload(url, admin, 0, new byte[0], "vazio.png"))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart(url).param("versao", "0").with(user(admin.getEmail())).with(csrf()))
                .andExpect(status().isBadRequest());
        mvc.perform(get(criarUrl() + "/" + id).with(user(admin.getEmail())))
                .andExpect(jsonPath("$.versao").value(0))
                .andExpect(jsonPath("$.comprovante").isEmpty());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "select count(*) from comprovantes where despesa_id=?", Integer.class, id));
    }
}
