package com.nictas.reviews.service.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;

@ExtendWith(MockitoExtension.class)
class DeveloperSyncServiceTest {

    private static final String DEVELOPERS_URL = "https://example.com";
    private static final String DEVELOPERS_ORG = "foo";
    private static final String DEVELOPERS_TEAM = "bar";

    @Mock
    private GitHubClientProvider clientProvider;
    @Mock
    private GitHubClient client;
    @Mock
    private DeveloperService developerService;

    private DeveloperSyncService developerSyncService;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(DEVELOPERS_URL)).thenReturn(client);
        developerSyncService = new DeveloperSyncService(clientProvider, developerService, DEVELOPERS_URL,
                DEVELOPERS_ORG, DEVELOPERS_TEAM);
    }

    @Test
    void testAssignReviewerAddsMissingDevelopers() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com");
        Developer developerBaz = new Developer("baz", "baz@example.com");

        when(client.getDevelopers(DEVELOPERS_ORG, DEVELOPERS_TEAM))
                .thenReturn(List.of(developerFoo, developerBar, developerBaz));
        when(developerService.existsDeveloper(developerFoo.getLogin())).thenReturn(false);
        when(developerService.existsDeveloper(developerBar.getLogin())).thenReturn(true);
        when(developerService.existsDeveloper(developerBaz.getLogin())).thenReturn(false);

        developerSyncService.fetchAndUpdateDevelopers();

        verify(developerService).createDeveloper(developerFoo);
        verify(developerService).createDeveloper(developerBaz);
        verify(developerService, times(2)).createDeveloper(any()); // Verify that no other developers were created
    }

}
