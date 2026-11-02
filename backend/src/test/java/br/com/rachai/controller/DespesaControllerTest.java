package br.com.rachai.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.rachai.dto.*;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.service.DespesaService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DespesaController.class)
class DespesaControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean DespesaService service;

    @Test
    void preservaCentavosERecebeStringDecimal() throws Exception {
        when(service.cadastrar(eq(1L), any()))
                .thenReturn(
                        new DespesaResponse(
                                2L,
                                1L,
                                "Mercado",
                                "9999999999.99",
                                LocalDate.of(2026, 10, 12),
                                new PessoaDespesaResponse(1L, "Ana", 1L),
                                java.util.List.of(),
                                0L,
                                null));
        mvc.perform(
                        post("/api/grupos/1/despesas")
                                .contentType("application/json")
                                .content(
                                        """
{"descricao":"Mercado","valorTotal":"9999999999.99","data":"2026-10-12","pagadorId":1,"participanteIds":[1]}
"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value("9999999999.99"));
        var captor = ArgumentCaptor.forClass(CriarDespesaRequest.class);
        verify(service).cadastrar(eq(1L), captor.capture());
        assertEquals(new BigDecimal("9999999999.99"), captor.getValue().valorTotal());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1.00", "0.001", "1.999", "10000000000.00", "NaN", "1,25"})
    void rejeitaValoresInvalidos(String valor) throws Exception {
        mvc.perform(
                        post("/api/grupos/1/despesas")
                                .contentType("application/json")
                                .content(
                                        "{\"descricao\":\"Mercado\",\"valorTotal\":\""
                                                + valor
                                                + "\",\"data\":\"2026-10-12\",\"pagadorId\":1,\"participanteIds\":[1]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"descricao\":\" \"}",
                "{\"descricao\":\"Mercado\",\"valorTotal\":1,\"data\":\"2026-02-30\"}",
                "{\"descricao\":\"Mercado\",\"valorTotal\":1,\"data\":\"2026-10-12\",\"saldo\":1}"
            })
    void rejeitaCamposAusentesDatasInvalidasECamposForaDaOnda(String json) throws Exception {
        mvc.perform(post("/api/grupos/1/despesas").contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void grupoInexistenteRetorna404() throws Exception {
        when(service.cadastrar(eq(99L), any()))
                .thenThrow(new NaoEncontradoException("Grupo não encontrado."));
        mvc.perform(
                        post("/api/grupos/99/despesas")
                                .contentType("application/json")
                                .content(
                                        """
{"descricao":"Mercado","valorTotal":"0.01","data":"2026-10-12","pagadorId":1,"participanteIds":[1]}
"""))
                .andExpect(status().isNotFound());
    }
}
