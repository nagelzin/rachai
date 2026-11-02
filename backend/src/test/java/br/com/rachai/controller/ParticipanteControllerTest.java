package br.com.rachai.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.service.ParticipanteService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ParticipanteController.class)
class ParticipanteControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ParticipanteService service;

    @Test
    void listaVazia() throws Exception {
        when(service.listar(1L)).thenReturn(List.of());
        mvc.perform(get("/api/grupos/1/participantes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void inclusaoLivrePorNomeNaoExisteMais() throws Exception {
        mvc.perform(
                        post("/api/grupos/1/participantes")
                                .contentType("application/json")
                                .content("{\"nome\":\"Ana\"}"))
                .andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(service);
    }

    @Test
    void removeCom204() throws Exception {
        mvc.perform(delete("/api/grupos/1/participantes/2")).andExpect(status().isNoContent());
        verify(service).remover(1L, 2L);
    }

    @Test
    void participanteDeOutroGrupoRetorna404() throws Exception {
        doThrow(new NaoEncontradoException("Ausente")).when(service).remover(1L, 2L);
        mvc.perform(delete("/api/grupos/1/participantes/2")).andExpect(status().isNotFound());
    }
}
