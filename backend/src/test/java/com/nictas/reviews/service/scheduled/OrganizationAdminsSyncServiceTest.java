package com.nictas.reviews.service.scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.repository.DeveloperRepository;
import com.nictas.reviews.repository.PullRequestReviewRepository;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;

@ExtendWith(MockitoExtension.class)
class OrganizationAdminsSyncServiceTest {

    private static final String DEVELOPERS_URL = "https://example.com";
    private static final String DEVELOPERS_ORG = "foo";

    @Mock
    private GitHubClientProvider clientProvider;
    @Mock
    private GitHubClient client;
    @Mock
    private DeveloperRepository developerRepository;
    @Mock
    private PullRequestReviewRepository pullRequestReviewRepository;

    private OrganizationAdminsSyncService organizationAdminsSyncService;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(DEVELOPERS_URL)).thenReturn(client);
        organizationAdminsSyncService = new OrganizationAdminsSyncService(clientProvider, DEVELOPERS_URL,
                DEVELOPERS_ORG);
    }

    @Test
    void testAssignReviewerAddsMissingDevelopers() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com");

        when(client.getOrganizationAdmins(DEVELOPERS_ORG)).thenReturn(List.of(developerFoo, developerBar));

        organizationAdminsSyncService.fetchAndUpdateOrganizationAdmins();

        assertEquals(List.of(developerFoo, developerBar), organizationAdminsSyncService.getOrganizationAdmins());
    }

}
