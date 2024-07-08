package com.nictas.reviews.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientException;
import com.nictas.reviews.service.github.GitHubClientProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GitHubOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final GitHubClientProvider clientProvider;
    private final String developersUrl;
    private final String developersOrg;

    @Autowired
    public GitHubOpaqueTokenIntrospector(GitHubClientProvider clientProvider,
                                         @Value("${developers.github.url}") String developersUrl,
                                         @Value("${developers.github.org}") String developersOrg) {
        this.clientProvider = clientProvider;
        this.developersUrl = developersUrl;
        this.developersOrg = developersOrg;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            return introspectToken(token);
        } catch (GitHubClientException e) {
            log.error("Error introspecting token", e);

            OAuth2Error error = new OAuth2Error("invalid_token", "The token is invalid or has expired", null);
            throw new OAuth2AuthenticationException(error, e);
        }
    }

    private OAuth2AuthenticatedPrincipal introspectToken(String token) {
        GitHubClient client = clientProvider.getClientForUrl(developersUrl, token);
        List<Developer> organizationAdmins = client.getOrganizationAdmins(developersOrg);
        Developer user = client.getMyself();

        Set<GrantedAuthority> authorities = getGrantedAuthorities(user, organizationAdmins);
        Map<String, Object> attributes = Map.of("name", user.getLogin());
        return new OAuth2IntrospectionAuthenticatedPrincipal(user.getLogin(), attributes, authorities);
    }

    private Set<GrantedAuthority> getGrantedAuthorities(Developer user, List<Developer> organizationAdmins) {
        boolean isAdmin = isAdmin(user, organizationAdmins);
        if (isAdmin) {
            return Set.of(new SimpleGrantedAuthority(UserRoles.ROLE_USER),
                    new SimpleGrantedAuthority(UserRoles.ROLE_ADMIN));
        }
        return Set.of(new SimpleGrantedAuthority(UserRoles.ROLE_USER));
    }

    private boolean isAdmin(Developer user, List<Developer> organizationAdmins) {
        return organizationAdmins.stream()
                .map(Developer::getEmail)
                .anyMatch(adminEmail -> adminEmail.equals(user.getEmail()));
    }

}
