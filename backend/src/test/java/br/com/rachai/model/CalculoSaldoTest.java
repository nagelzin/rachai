package br.com.rachai.model;

import static org.junit.jupiter.api.Assertions.*;

import br.com.rachai.model.CalculoSaldo.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

class CalculoSaldoTest {
    private Parcela parcela(long id, String valor) {
        return new Parcela(id, new BigDecimal(valor));
    }

    private Lancamento despesa(long id, Long pagador, String valor, Parcela... parcelas) {
        return new Lancamento(id, pagador, new BigDecimal(valor), List.of(parcelas));
    }

    private void saldo(Resultado r, long id, String pago, String devido, String saldo) {
        assertEquals(new BigDecimal(pago), r.totais().get(id).pago());
        assertEquals(new BigDecimal(devido), r.totais().get(id).devido());
        assertEquals(new BigDecimal(saldo), r.totais().get(id).saldo());
    }

    private void somaZero(Resultado r) {
        assertTrue(r.despesasPendentes().isEmpty());
        assertEquals(
                0,
                r.totais().values().stream()
                        .map(Total::saldo)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .signum());
    }

    @Test
    void positivoNegativoEZeradoComParticipanteSemDespesas() {
        var r =
                CalculoSaldo.calcular(
                        List.of(3L, 1L, 2L),
                        List.of(
                                despesa(
                                        1,
                                        1L,
                                        "100.00",
                                        parcela(1, "50.00"),
                                        parcela(2, "50.00"))));
        saldo(r, 1, "100.00", "50.00", "50.00");
        saldo(r, 2, "0.00", "50.00", "-50.00");
        saldo(r, 3, "0.00", "0.00", "0.00");
        assertEquals(List.of(1L, 2L, 3L), new ArrayList<>(r.totais().keySet()));
        somaZero(r);
    }

    @Test
    void multiplasDespesasSubconjuntosEPagadorForaDoConsumo() {
        var r =
                CalculoSaldo.calcular(
                        List.of(1L, 2L, 3L),
                        List.of(
                                despesa(
                                        1,
                                        1L,
                                        "100.00",
                                        parcela(1, "33.34"),
                                        parcela(2, "33.33"),
                                        parcela(3, "33.33")),
                                despesa(2, 2L, "60.00", parcela(1, "30.00"), parcela(3, "30.00")),
                                despesa(3, 3L, "15.00", parcela(3, "15.00"))));
        saldo(r, 1, "100.00", "63.34", "36.66");
        saldo(r, 2, "60.00", "33.33", "26.67");
        saldo(r, 3, "15.00", "78.33", "-63.33");
        somaZero(r);
    }

    @Test
    void parcelasPersistidasSaoUsadasSemRefazerDivisaoIgual() {
        var r =
                CalculoSaldo.calcular(
                        List.of(1L, 2L),
                        List.of(despesa(1, 2L, "10.00", parcela(1, "2.00"), parcela(2, "8.00"))));
        saldo(r, 1, "0.00", "2.00", "-2.00");
        saldo(r, 2, "10.00", "8.00", "2.00");
        somaZero(r);
    }

    @Test
    void centavoParcelaZeroETotaisAcimaDoLimiteDeUmaDespesa() {
        var r =
                CalculoSaldo.calcular(
                        List.of(1L, 2L),
                        List.of(
                                despesa(1, 2L, "0.01", parcela(1, "0.01"), parcela(2, "0.00")),
                                despesa(2, 1L, "9999999999.99", parcela(2, "9999999999.99")),
                                despesa(3, 1L, "9999999999.99", parcela(2, "9999999999.99"))));
        saldo(r, 1, "19999999999.98", "0.01", "19999999999.97");
        saldo(r, 2, "0.01", "19999999999.98", "-19999999999.97");
        somaZero(r);
    }

    @Test
    void grupoVazioEConsumoProprioTemSaldoZero() {
        var vazio = CalculoSaldo.calcular(List.of(1L), List.of());
        saldo(vazio, 1, "0.00", "0.00", "0.00");
        var proprio =
                CalculoSaldo.calcular(
                        List.of(1L), List.of(despesa(1, 1L, "8.50", parcela(1, "8.50"))));
        saldo(proprio, 1, "8.50", "8.50", "0.00");
        somaZero(proprio);
    }

    @Test
    void dadosIncompletosOuInconsistentesBloqueiamTodoSaldoSemInferirDados() {
        var invalidos =
                List.of(
                        despesa(1, null, "10.00", parcela(1, "10.00")),
                        despesa(2, 1L, "10.00"),
                        despesa(3, 1L, "10.00", new Parcela(1L, null)),
                        despesa(4, 1L, "10.00", parcela(1, "9.99")),
                        despesa(5, 9L, "10.00", parcela(1, "10.00")),
                        despesa(6, 1L, "10.00", parcela(9, "10.00")),
                        despesa(7, 1L, "10.00", parcela(1, "5.00"), parcela(1, "5.00")),
                        despesa(8, 1L, "10.00", parcela(1, "-1.00"), parcela(2, "11.00")),
                        despesa(9, 1L, "10.00", parcela(1, "0.001"), parcela(2, "9.999")));
        for (var invalido : invalidos) {
            var r =
                    CalculoSaldo.calcular(
                            List.of(1L, 2L),
                            List.of(invalido, despesa(100, 1L, "1.00", parcela(2, "1.00"))));
            assertEquals(List.of(invalido.id()), r.despesasPendentes());
            assertTrue(r.totais().isEmpty());
        }
    }

    @Test
    void ordemDasDespesasNaoAlteraResultadoENaoDuplicaPessoas() {
        var a = despesa(1, 1L, "10.00", parcela(2, "10.00"));
        var b = despesa(2, 2L, "10.00", parcela(1, "10.00"));
        var r = CalculoSaldo.calcular(List.of(1L, 2L), List.of(a, b));
        assertEquals(r, CalculoSaldo.calcular(List.of(2L, 1L), List.of(b, a)));
        saldo(r, 1, "10.00", "10.00", "0.00");
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculoSaldo.calcular(List.of(1L, 1L), List.of()));
    }
}
