package br.com.rachai.security;

import br.com.rachai.model.Usuario;
import br.com.rachai.repository.UsuarioRepository;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        var encoder = new Pbkdf2PasswordEncoder("", 16, 600000, 256);
        encoder.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        return encoder;
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> {
            var u =
                    usuarios.findByEmail(Usuario.normalizarEmail(email))
                            .filter(v -> v.getSenhaHash() != null)
                            .orElseThrow(
                                    () -> new UsernameNotFoundException("Credenciais inválidas."));
            return User.withUsername(u.getEmail())
                    .password(u.getSenhaHash())
                    .roles("USUARIO")
                    .build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(Customizer.withDefaults())
                .requestCache(c -> c.disable())
                .authorizeHttpRequests(
                        a ->
                                a.dispatcherTypeMatchers(DispatcherType.ERROR)
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/csrf",
                                                "/api/auth/cadastro",
                                                "/api/auth/login")
                                        .permitAll()
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .denyAll())
                .formLogin(
                        f ->
                                f.loginProcessingUrl("/api/auth/login")
                                        .usernameParameter("email")
                                        .successHandler(
                                                (req, res, auth) -> {
                                                    res.setStatus(204);
                                                })
                                        .failureHandler(
                                                (req, res, error) ->
                                                        json(
                                                                res,
                                                                401,
                                                                "E-mail ou senha incorretos.")))
                .logout(
                        l ->
                                l.logoutUrl("/api/auth/logout")
                                        .deleteCookies("JSESSIONID")
                                        .logoutSuccessHandler(
                                                (req, res, auth) -> res.setStatus(204)))
                .exceptionHandling(
                        e ->
                                e.authenticationEntryPoint(
                                                (req, res, error) ->
                                                        json(
                                                                res,
                                                                401,
                                                                "Entre na sua conta para"
                                                                        + " continuar."))
                                        .accessDeniedHandler(
                                                (req, res, error) ->
                                                        json(
                                                                res,
                                                                403,
                                                                "Acesso negado ou sessão de"
                                                                        + " formulário expirada."
                                                                        + " Atualize e tente"
                                                                        + " novamente.")))
                .addFilterBefore(new TentativasFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    static void json(jakarta.servlet.http.HttpServletResponse response, int status, String mensagem)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"mensagem\":\"" + mensagem + "\"}");
    }
}
