package br.com.rachai.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.*;

class TentativasFilterTest {
    @Test
    void limitaPorIpSemBloquearOutro() {
        var f = new TentativasFilter();
        for (int i = 0; i < 60; i++) assertTrue(f.permitir("ip1"));
        assertFalse(f.permitir("ip1"));
        assertTrue(f.permitir("ip2"));
    }

    @Test
    void liberaDepoisDaJanela() {
        class Relogio extends Clock {
            Instant agora = Instant.parse("2026-10-13T00:00:00Z");

            public ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            public Clock withZone(ZoneId z) {
                return this;
            }

            public Instant instant() {
                return agora;
            }
        }
        var clock = new Relogio();
        var f = new TentativasFilter(clock);
        for (int i = 0; i < 60; i++) f.permitir("ip");
        clock.agora = clock.agora.plusSeconds(901);
        assertTrue(f.permitir("ip"));
    }
}
