package com.nictas.reviews.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    static final String ROLE_USER = "USER";
    static final String ROLE_ADMIN = "ADMIN";

    private final GitHubOpaqueTokenIntrospector gitHubOpaqueTokenIntrospector;

    @Autowired
    public SecurityConfiguration(GitHubOpaqueTokenIntrospector gitHubOpaqueTokenIntrospector) {
        this.gitHubOpaqueTokenIntrospector = gitHubOpaqueTokenIntrospector;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(customizer -> customizer.anyRequest()
                        .hasRole(ROLE_USER))
                .oauth2ResourceServer(customizer -> customizer.opaqueToken(
                        opaqueTokenCustomizer -> opaqueTokenCustomizer.introspector(gitHubOpaqueTokenIntrospector)))
                .build();
    }

}
