/*
 * SecurityConfig.java
 * Copyright (c) UEG.
 *
 */

package br.ueg.prog.webi.api.config;


import br.ueg.prog.webi.api.exception.FilterChainExceptionHandler;
import br.ueg.prog.webi.api.security.AuthenticationProvider;
import br.ueg.prog.webi.api.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Classe de configuração referente a segurança da aplicação.
 *
 * @author UEG
 */

/*@Configuration
@EnableWebSecurity
@EnableMethodSecurity*/
public abstract class ApiSecurityConfig {


    /**
     * Retorna a instância de {@link AuthenticationProvider} necessária na validação
     * do 'Token JWT'.
     *
     * @return
     */
   @Autowired
    protected  LogoutService logoutHandler;

   @Autowired
   protected  AuthenticationProvider authenticationProvider;

    /**
     * para ser utilizado para o tratamento de execção padronizado da aplicação
     * https://stackoverflow.com/a/55864206/21944037
     */
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Autowired
    private FilterChainExceptionHandler filterChainExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers(
                        "/api/v1/auth/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/swagger-ui.html"
                )
                .permitAll()


                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filterChainExceptionHandler, LogoutFilter.class)
                .logout()
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        ;
        configureHttpSecurity(http);

        return http.build();
    }

    /**
     * Método utilizado para realizar configuração de seguranças quando necessário
     * @param http - Referência do HttpSecurity para configurações se necessário
     */
    protected abstract void configureHttpSecurity(HttpSecurity http);


    /**
     * Retorna a instância de {@link JwtAuthenticationFilter}.
     *
     * @return
     * @throws Exception
     */

    protected JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationProvider);
    }
}
