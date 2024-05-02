package com.nictas.reviews.service.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.repository.DeveloperRepository;
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
    private DeveloperRepository developerRepository;

    private DeveloperSyncService developerSyncService;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(DEVELOPERS_URL)).thenReturn(client);
        developerSyncService = new DeveloperSyncService(clientProvider, developerRepository, DEVELOPERS_URL,
                DEVELOPERS_ORG, DEVELOPERS_TEAM);
    }

    @Test
    void testAssignReviewerAddsMissingDevelopers() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com");
        Developer developerBaz = new Developer("baz", "baz@example.com");

        when(client.getDevelopers(DEVELOPERS_ORG, DEVELOPERS_TEAM))
                .thenReturn(List.of(developerFoo, developerBar, developerBaz));
        when(developerRepository.get(developerFoo.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.get(developerBar.getLogin())).thenReturn(Optional.of(developerBar));
        when(developerRepository.get(developerBaz.getLogin())).thenReturn(Optional.empty());

        developerSyncService.fetchAndUpdateDevelopers();

        verify(developerRepository).create(developerFoo);
        verify(developerRepository).create(developerBaz);
        verify(developerRepository, times(2)).create(any()); // Verify that no other developers were created
    }

}
