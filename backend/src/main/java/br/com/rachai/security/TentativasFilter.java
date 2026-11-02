package br.com.rachai.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.*;
import java.util.HashMap;

/** Proteção local por IP; não confia em X-Forwarded-For enviado pelo cliente. */
public class TentativasFilter extends OncePerRequestFilter {
    private record Janela(Instant inicio, int total) {}

    private final HashMap<String, Janela> janelas = new HashMap<>();
    private final Clock clock;

    public TentativasFilter() {
        this(Clock.systemUTC());
    }

    TentativasFilter(Clock clock) {
        this.clock = clock;
    }

    synchronized boolean permitir(String ip) {
        var now = clock.instant();
        janelas.entrySet().removeIf(e -> e.getValue().inicio().plusSeconds(900).isBefore(now));
        var atual = janelas.get(ip);
        if (atual == null && janelas.size() >= 10000) return false;
        if (atual != null && atual.total() >= 60) return false;
        janelas.put(
                ip,
                new Janela(
                        atual == null ? now : atual.inicio(),
                        atual == null ? 1 : atual.total() + 1));
        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if ("POST".equals(req.getMethod())
                && ("/api/auth/login".equals(req.getServletPath())
                        || "/api/auth/cadastro".equals(req.getServletPath()))
                && !permitir(req.getRemoteAddr())) {
            res.setHeader("Retry-After", "900");
            SecurityConfig.json(
                    res, 429, "Muitas tentativas. Aguarde 15 minutos e tente novamente.");
            return;
        }
        chain.doFilter(req, res);
    }
}
