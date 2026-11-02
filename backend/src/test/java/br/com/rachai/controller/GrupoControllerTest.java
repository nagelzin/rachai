package br.com.rachai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.rachai.dto.*;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.service.GrupoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(GrupoController.class)
class GrupoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean GrupoService service;

    @Test
    void criaGrupoComLocalizacao() throws Exception {
        when(service.criar(any()))
                .thenReturn(
                        new GrupoResponse(1L, "Viagem", null, 1L, "Equipe", LocalDateTime.now()));
        mvc.perform(
                        post("/api/grupos")
                                .contentType("application/json")
                                .content(
                                        """
                                        {"nome":"Viagem"}
                                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/grupos/1"))
                .andExpect(jsonPath("$.nome").value("Viagem"));
    }

    @Test
    void rejeitaNomeVazio() throws Exception {
        mvc.perform(
                        post("/api/grupos")
                                .contentType("application/json")
                                .content("{\"nome\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());
        verifyNoInteractions(service);
    }

    @Test
    void consultaGrupo() throws Exception {
        when(service.consultar(1L))
                .thenReturn(
                        new GrupoResponse(1L, "Viagem", null, 1L, "Equipe", LocalDateTime.now()));
        mvc.perform(get("/api/grupos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void grupoInexistenteRetorna404() throws Exception {
        when(service.consultar(99L)).thenThrow(new NaoEncontradoException("Grupo não encontrado."));
        mvc.perform(get("/api/grupos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Grupo não encontrado."));
    }

    @Test
    void identificadorInvalidoRetorna400() throws Exception {
        mvc.perform(get("/api/grupos/abc")).andExpect(status().isBadRequest());
    }
}
