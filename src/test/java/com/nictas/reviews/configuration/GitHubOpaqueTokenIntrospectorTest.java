package com.nictas.reviews.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientException;
import com.nictas.reviews.service.github.GitHubClientProvider;
import com.nictas.reviews.service.scheduled.OrganizationAdminsSyncService;

@ExtendWith(MockitoExtension.class)
class GitHubOpaqueTokenIntrospectorTest {

    private static final String TOKEN = "test";
    private static final String DEVELOPERS_URL = "https://example.com";
    private static final Developer DEVELOPER_FOO = new Developer("foo", "foo@example.com");
    private static final Developer DEVELOPER_BAR = new Developer("bar", "bar@example.com");
    private static final Developer DEVELOPER_BAZ = new Developer("baz", "baz@example.com");

    @Mock
    private GitHubClientProvider clientProvider;
    @Mock
    private GitHubClient client;
    @Mock
    private OrganizationAdminsSyncService organizationAdminsSyncService;
    private GitHubOpaqueTokenIntrospector introspector;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(DEVELOPERS_URL, TOKEN)).thenReturn(client);
        introspector = new GitHubOpaqueTokenIntrospector(clientProvider, organizationAdminsSyncService, DEVELOPERS_URL);
    }

    @Test
    void testIntrospectSuccess() {
        when(client.getMyself()).thenReturn(DEVELOPER_FOO);
        when(organizationAdminsSyncService.getOrganizationAdmins()).thenReturn(List.of(DEVELOPER_BAR, DEVELOPER_BAZ));

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        assertEquals(DEVELOPER_FOO.getLogin(), principal.getName());
        var expectedAuthorities = Set.of(new SimpleGrantedAuthority(UserRoles.ROLE_USER));
        var authorities = principal.getAuthorities();
        // Equals does not work because the set types do not match.
        assertTrue(expectedAuthorities.containsAll(authorities));
        assertTrue(authorities.containsAll(expectedAuthorities));
    }

    @Test
    void testIntrospectAdminSuccess() {
        when(client.getMyself()).thenReturn(DEVELOPER_BAZ);
        when(organizationAdminsSyncService.getOrganizationAdmins()).thenReturn(List.of(DEVELOPER_BAR, DEVELOPER_BAZ));

        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        assertEquals(DEVELOPER_BAZ.getLogin(), principal.getName());
        var expectedAuthorities = Set.of(new SimpleGrantedAuthority(UserRoles.ROLE_USER),
                new SimpleGrantedAuthority(UserRoles.ROLE_ADMIN));
        var authorities = principal.getAuthorities();
        // Equals does not work because the set types do not match.
        assertTrue(expectedAuthorities.containsAll(authorities));
        assertTrue(authorities.containsAll(expectedAuthorities));
    }

    @Test
    void testIntrospectWithException() {
        when(client.getMyself()).thenThrow(new GitHubClientException("GitHub client error"));

        OAuth2AuthenticationException exception = assertThrows(OAuth2AuthenticationException.class,
                () -> introspector.introspect(TOKEN));

        assertEquals("invalid_token", exception.getError()
                .getErrorCode());
        assertEquals("The token is invalid or has expired", exception.getError()
                .getDescription());
    }

}
