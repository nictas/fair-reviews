package com.nictas.reviews.service.scheduled;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.repository.DeveloperRepository;
import com.nictas.reviews.repository.PullRequestReviewRepository;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeveloperSyncServiceTest {

    private static final String DEVELOPERS_URL = "https://example.com";
    private static final String DEVELOPERS_ORG = "foo";
    private static final String DEVELOPERS_TEAM = "bar";

    private static final Multiplier MULTIPLIER_1 = Multiplier.builder()
            .id(UUID.fromString("2f7fc3e6-b54f-4593-aaca-98aeed3d6d02"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(2.0)
                            .deletionsMultiplier(0.4)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.5)
                            .deletionsMultiplier(0.2)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 3, 3, 17, 15, 0, 0, ZoneOffset.UTC))
            .build();

    private static final PullRequestReview REVIEW_1 = PullRequestReview.builder()
            .id(UUID.fromString("91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
            .pullRequestUrl("https://github.com/foo/bar/pull/87")
            .pullRequestFileDetails(new PullRequestFileDetails(15, 11, List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(15)
                            .deletions(11)
                            .build())))
            .score(20.)
            .multiplier(MULTIPLIER_1)
            .build();

    private static final PullRequestReview REVIEW_2 = PullRequestReview.builder()
            .id(UUID.fromString("dcb724e6-d2cb-4e63-a1ab-d5bc59e5cfdc"))
            .pullRequestUrl("https://github.com/foo/bar/pull/90")
            .pullRequestFileDetails(new PullRequestFileDetails(11, 25, List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(10)
                            .deletions(22)
                            .build(),
                    ChangedFile.builder()
                            .name("bar.java")
                            .additions(1)
                            .deletions(3)
                            .build())))
            .score(60.)
            .multiplier(MULTIPLIER_1)
            .build();

    @Mock
    private GitHubClientProvider clientProvider;
    @Mock
    private GitHubClient client;
    @Mock
    private DeveloperRepository developerRepository;
    @Mock
    private PullRequestReviewRepository pullRequestReviewRepository;

    private DeveloperSyncService developerSyncService;

    @Captor
    private ArgumentCaptor<PullRequestReview> reviewCaptor;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(DEVELOPERS_URL)).thenReturn(client);
        developerSyncService = new DeveloperSyncService(clientProvider, developerRepository,
                pullRequestReviewRepository, DEVELOPERS_URL, DEVELOPERS_ORG, DEVELOPERS_TEAM);
    }

    @Test
    void testAssignReviewerAddsMissingDevelopers() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com", 10.);
        Developer developerBaz = new Developer("baz", "baz@example.com");
        Developer developerQux = new Developer("qux", "qux@example.com", 30.);

        when(client.getDevelopers(DEVELOPERS_ORG, DEVELOPERS_TEAM))
                .thenReturn(List.of(developerFoo, developerBar, developerBaz, developerQux));
        when(developerRepository.findById(developerFoo.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findById(developerBar.getLogin())).thenReturn(Optional.of(developerBar));
        when(developerRepository.findById(developerBaz.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findById(developerQux.getLogin())).thenReturn(Optional.of(developerQux));
        when(developerRepository.findWithLowestScore(Collections.emptyList())).thenReturn(Optional.of(developerBar));
        when(pullRequestReviewRepository.findByDeveloperLogin(developerBar.getLogin(), Pageable.unpaged()))
                .thenReturn(Page.empty());

        developerSyncService.fetchAndUpdateDevelopers();

        verify(developerRepository).save(developerFoo.withScore(10.0));
        verify(developerRepository).save(developerBaz.withScore(10.0));
        verify(developerRepository, times(2)).save(any()); // Verify that no other developers were created
    }

    @Test
    void testAssignReviewerWithoutExistingDevelopers() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com");
        Developer developerBaz = new Developer("baz", "baz@example.com");

        when(client.getDevelopers(DEVELOPERS_ORG, DEVELOPERS_TEAM))
                .thenReturn(List.of(developerFoo, developerBar, developerBaz));
        when(developerRepository.findById(developerFoo.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findById(developerBar.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findById(developerBaz.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findWithLowestScore(Collections.emptyList())).thenReturn(Optional.empty());
        when(pullRequestReviewRepository.findByDeveloperLogin(DeveloperSyncService.DUMMY_DEVELOPER.getLogin(),
                Pageable.unpaged())).thenReturn(Page.empty());

        developerSyncService.fetchAndUpdateDevelopers();

        verify(developerRepository).save(developerFoo);
        verify(developerRepository).save(developerBar);
        verify(developerRepository).save(developerBaz);
        verify(developerRepository, times(3)).save(any()); // Verify that no other developers were created
    }

    @Test
    void testAssignReviewerReplicatesHistory() {
        Developer developerFoo = new Developer("foo", "foo@example.com");
        Developer developerBar = new Developer("bar", "bar@example.com", 10.);

        when(client.getDevelopers(DEVELOPERS_ORG, DEVELOPERS_TEAM)).thenReturn(List.of(developerFoo, developerBar));
        when(developerRepository.findById(developerFoo.getLogin())).thenReturn(Optional.empty());
        when(developerRepository.findById(developerBar.getLogin())).thenReturn(Optional.of(developerBar));
        when(developerRepository.findWithLowestScore(Collections.emptyList())).thenReturn(Optional.of(developerBar));

        Pageable pageable = Pageable.unpaged();
        Page<PullRequestReview> reviewsPage = new PageImpl<>(
                List.of(REVIEW_1.withDeveloper(developerBar), REVIEW_2.withDeveloper(developerBar)), pageable, 2);
        when(pullRequestReviewRepository.findByDeveloperLogin(developerBar.getLogin(), pageable))
                .thenReturn(reviewsPage);

        developerSyncService.fetchAndUpdateDevelopers();

        verify(developerRepository).save(developerFoo.withScore(10.0));
        verify(developerRepository, times(1)).save(any()); // Verify that no other developers were created

        verify(pullRequestReviewRepository, times(2)).save(reviewCaptor.capture());

        List<PullRequestReview> replicatedReviews = reviewCaptor.getAllValues();

        PullRequestReview replicatedReview1 = replicatedReviews.get(0);
        assertEquals(developerFoo, replicatedReview1.getDeveloper());
        assertEquals(REVIEW_1.getScore(), replicatedReview1.getScore());
        assertEquals(REVIEW_1.getMultiplier(), replicatedReview1.getMultiplier());
        assertEquals(REVIEW_1.getPullRequestUrl(), replicatedReview1.getPullRequestUrl());
        assertEquals(REVIEW_1.getPullRequestFileDetails(), replicatedReview1.getPullRequestFileDetails());
        assertNotEquals(REVIEW_1.getId(), replicatedReview1.getId());
        assertNotEquals(REVIEW_1.getCreatedAt(), replicatedReview1.getCreatedAt());

        PullRequestReview replicatedReview2 = replicatedReviews.get(1);
        assertEquals(developerFoo, replicatedReview2.getDeveloper());
        assertEquals(REVIEW_2.getScore(), replicatedReview2.getScore());
        assertEquals(REVIEW_2.getMultiplier(), replicatedReview2.getMultiplier());
        assertEquals(REVIEW_2.getPullRequestUrl(), replicatedReview2.getPullRequestUrl());
        assertEquals(REVIEW_2.getPullRequestFileDetails(), replicatedReview2.getPullRequestFileDetails());
        assertNotEquals(REVIEW_2.getId(), replicatedReview2.getId());
        assertNotEquals(REVIEW_2.getCreatedAt(), replicatedReview2.getCreatedAt());
    }

}
