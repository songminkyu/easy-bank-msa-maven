package io.github.songminkyu.card.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import io.github.songminkyu.card.exception.security.SecurityProblemSupport;
import io.github.songminkyu.card.security.AuthoritiesConstants;
import io.github.songminkyu.card.security.oauth2.JwtGrantedAuthorityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
            "/graphiql", " /favicon.ico", "/v3/api-docs/**",
            "/swagger-ui/**", "/webjars/**", "/swagger-ui.html");
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        MvcRequestMatcher.Builder mvc,
        Converter<Jwt, AbstractAuthenticationToken> authenticationConverter,
        SecurityProblemSupport problemSupport) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("script-src 'self' 'unsafe-inline'"))
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport))
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(mvc.pattern("/graphql"), mvc.pattern("/subscription")).permitAll()
                    .requestMatchers(toH2Console()).permitAll()
                    .requestMatchers(mvc.pattern("/*/actuator/**")).permitAll()
                    .requestMatchers(HttpMethod.GET).permitAll()
                    .requestMatchers(mvc.pattern("/api/card/**")).hasAuthority(AuthoritiesConstants.CARD)
                    .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter))
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport));
        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
        return jwtAuthenticationConverter;
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    AuthenticationProvider authenticationProvider(
        JwtDecoder jwtDecoder,
        Converter<Jwt, AbstractAuthenticationToken> authenticationConverter) {
        var  authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        authenticationProvider.setJwtAuthenticationConverter(authenticationConverter);
        return authenticationProvider;
    }
}