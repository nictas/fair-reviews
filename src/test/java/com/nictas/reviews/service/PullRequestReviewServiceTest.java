package com.nictas.reviews.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.PullRequestReviewRepository;
import com.nictas.reviews.service.score.PullRequestScoreComputer;
import com.nictas.reviews.service.score.PullRequestScoreComputer.PullRequestAssessment;

@ExtendWith(MockitoExtension.class)
class PullRequestReviewServiceTest {

    private static final String PR_URL = "https://example.com/foo/bar/123";
    private static final PullRequestFileDetails PR_FILE_DETAILS = new PullRequestFileDetails(List.of(//
            ChangedFile.builder()
                    .name("file1.txt")
                    .additions(10)
                    .deletions(5)
                    .build(),
            ChangedFile.builder()
                    .name("file2.java")
                    .additions(20)
                    .deletions(3)
                    .build(),
            ChangedFile.builder()
                    .name("file3.js")
                    .additions(31)
                    .deletions(14)
                    .build(),
            ChangedFile.builder()
                    .name("file4.py")
                    .additions(77)
                    .deletions(83)
                    .build()));
    private static final double PR_SCORE = 30.3;
    private static final Developer DEVELOPER_FOO = new Developer("foo", "foo@example.com");
    private static final Developer DEVELOPER_BAR = new Developer("bar", "bar@example.com");
    private static final Developer DEVELOPER_BAZ = new Developer("baz", "baz@example.com");
    private static final PullRequestReview REVIEW_1 = PullRequestReview.builder()
            .id(UUID.fromString("91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
            .pullRequestUrl("https://github.com/foo/bar/pull/87")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(15)
                            .deletions(11)
                            .build())))
            .score(20.7)
            .developer(DEVELOPER_FOO)
            .build();
    private static final PullRequestReview REVIEW_2 = PullRequestReview.builder()
            .id(UUID.fromString("dcb724e6-d2cb-4e63-a1ab-d5bc59e5cfdc"))
            .pullRequestUrl("https://github.com/foo/bar/pull/90")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
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
            .score(60.1)
            .developer(DEVELOPER_FOO)
            .build();

    @Mock
    private DeveloperService developerService;

    @Mock
    private PullRequestReviewRepository pullRequestReviewRepository;

    @Mock
    private PullRequestScoreComputer pullRequestScoreComputer;

    @Captor
    private ArgumentCaptor<PullRequestReview> pullRequestReviewCaptor;

    @InjectMocks
    private PullRequestReviewService pullRequestReviewService;

    @Test
    void testGetAllReviews() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PullRequestReview> reviewsPage = new PageImpl<>(reviews, pageable, reviews.size());
        when(pullRequestReviewRepository.getAll(pageable)).thenReturn(reviewsPage);

        Page<PullRequestReview> actualReviewsPage = pullRequestReviewService.getAllReviews(pageable);

        assertSame(reviewsPage, actualReviewsPage);
    }

    @Test
    void testGetReview() {
        when(pullRequestReviewRepository.get(REVIEW_1.getId())).thenReturn(Optional.of(REVIEW_1));

        PullRequestReview review = pullRequestReviewService.getReview(REVIEW_1.getId());

        assertEquals(REVIEW_1, review);
    }

    @Test
    void testGetReviewNotFound() {
        UUID id = UUID.randomUUID();
        when(pullRequestReviewRepository.get(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pullRequestReviewService.getReview(id));

        assertEquals("Could not find review with ID: " + id, exception.getMessage());
    }

    @Test
    void testCreateReview() {
        pullRequestReviewService.createReview(REVIEW_1);

        verify(pullRequestReviewRepository).create(REVIEW_1);
    }

    @Test
    void testUpdateReview() {
        pullRequestReviewService.updateReview(REVIEW_1);

        verify(pullRequestReviewRepository).update(REVIEW_1);
    }

    @Test
    void testGetReviewsByUrl() {
        List<PullRequestReview> reviews = List.of(REVIEW_1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PullRequestReview> reviewsPage = new PageImpl<>(reviews, pageable, reviews.size());
        when(pullRequestReviewRepository.getByUrl(REVIEW_1.getPullRequestUrl(), pageable)).thenReturn(reviewsPage);

        Page<PullRequestReview> actualReviewsPage = pullRequestReviewService
                .getReviewsByUrl(REVIEW_1.getPullRequestUrl(), pageable);

        assertSame(reviewsPage, actualReviewsPage);
    }

    @Test
    void testGetReviewsByDeveloperLogin() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PullRequestReview> reviewsPage = new PageImpl<>(reviews, pageable, reviews.size());
        when(pullRequestReviewRepository.getByDeveloperLogin(DEVELOPER_FOO.getLogin(), pageable))
                .thenReturn(reviewsPage);

        Page<PullRequestReview> actualReviewsPage = pullRequestReviewService
                .getReviewsByDeveloperLogin(DEVELOPER_FOO.getLogin(), pageable);

        assertSame(reviewsPage, actualReviewsPage);
        verify(pullRequestReviewRepository, times(1)).getByDeveloperLogin(DEVELOPER_FOO.getLogin(), pageable);
    }

    @Test
    void testAssign() {
        List<String> loginExclusionList = List.of(DEVELOPER_BAR.getLogin());
        when(developerService.getDeveloperWithLowestScore(loginExclusionList)).thenReturn(DEVELOPER_FOO);
        when(pullRequestScoreComputer.computeScore(PR_URL))
                .thenReturn(new PullRequestAssessment(PR_URL, PR_FILE_DETAILS, PR_SCORE));
        when(pullRequestReviewRepository.getByUrl(PR_URL, Pageable.unpaged())).thenReturn(Page.empty());

        List<Developer> assignees = pullRequestReviewService.assign(PR_URL, Collections.emptyList(),
                loginExclusionList);
        Developer expectedAssignee = DEVELOPER_FOO.withScore(PR_SCORE);

        assertEquals(List.of(expectedAssignee), assignees);
        verify(developerService).updateDeveloper(expectedAssignee);
    }

    @Test
    void testAssignWithExplicitlySpecifiedAssignees() {
        when(developerService.getDeveloper(DEVELOPER_FOO.getLogin())).thenReturn(DEVELOPER_FOO);
        when(developerService.getDeveloper(DEVELOPER_BAR.getLogin())).thenReturn(DEVELOPER_BAR);
        when(pullRequestScoreComputer.computeScore(PR_URL))
                .thenReturn(new PullRequestAssessment(PR_URL, PR_FILE_DETAILS, PR_SCORE));
        when(pullRequestReviewRepository.getByUrl(PR_URL, Pageable.unpaged())).thenReturn(Page.empty());

        List<Developer> assignees = pullRequestReviewService.assign(PR_URL,
                List.of(DEVELOPER_FOO.getLogin(), DEVELOPER_BAR.getLogin()), Collections.emptyList());
        Developer expectedAssigneeFoo = DEVELOPER_FOO.withScore(PR_SCORE);
        Developer expectedAssigneeBar = DEVELOPER_BAR.withScore(PR_SCORE);

        assertEquals(List.of(expectedAssigneeFoo, expectedAssigneeBar), assignees);
        verify(developerService).updateDeveloper(expectedAssigneeFoo);
        verify(developerService).updateDeveloper(expectedAssigneeBar);
    }

    @Test
    void testAssignCreatesReviewEntry() {
        when(developerService.getDeveloper(DEVELOPER_FOO.getLogin())).thenReturn(DEVELOPER_FOO);
        when(developerService.getDeveloper(DEVELOPER_BAR.getLogin())).thenReturn(DEVELOPER_BAR);
        when(pullRequestScoreComputer.computeScore(PR_URL))
                .thenReturn(new PullRequestAssessment(PR_URL, PR_FILE_DETAILS, PR_SCORE));
        when(pullRequestReviewRepository.getByUrl(PR_URL, Pageable.unpaged())).thenReturn(Page.empty());

        pullRequestReviewService.assign(PR_URL, List.of(DEVELOPER_FOO.getLogin(), DEVELOPER_BAR.getLogin()),
                Collections.emptyList());

        verify(pullRequestReviewRepository, times(2)).create(pullRequestReviewCaptor.capture());

        List<PullRequestReview> reviews = pullRequestReviewCaptor.getAllValues();
        assertEquals(2, reviews.size());
        PullRequestReview reviewOfFoo = reviews.get(0);
        PullRequestReview reviewOfBar = reviews.get(1);
        assertEquals(PR_URL, reviewOfFoo.getPullRequestUrl());
        assertEquals(PR_URL, reviewOfBar.getPullRequestUrl());
        assertEquals(PR_FILE_DETAILS, reviewOfFoo.getPullRequestFileDetails());
        assertEquals(PR_FILE_DETAILS, reviewOfBar.getPullRequestFileDetails());
        assertEquals(PR_SCORE, reviewOfFoo.getScore());
        assertEquals(PR_SCORE, reviewOfBar.getScore());
        assertEquals(DEVELOPER_FOO.withScore(PR_SCORE), reviewOfFoo.getDeveloper());
        assertEquals(DEVELOPER_BAR.withScore(PR_SCORE), reviewOfBar.getDeveloper());
    }

    @Test
    void testAssignWithAlreadyExistingReviews() {
        when(developerService.getDeveloperWithLowestScore(List.of(DEVELOPER_BAR.getLogin(), DEVELOPER_FOO.getLogin())))
                .thenReturn(DEVELOPER_BAZ);
        when(pullRequestScoreComputer.computeScore(PR_URL))
                .thenReturn(new PullRequestAssessment(PR_URL, PR_FILE_DETAILS, PR_SCORE));
        PullRequestReview review = PullRequestReview.builder()
                .pullRequestUrl(PR_URL)
                .pullRequestFileDetails(PR_FILE_DETAILS)
                .score(PR_SCORE)
                .developer(DEVELOPER_FOO)
                .build();
        Pageable pageable = Pageable.unpaged();
        when(pullRequestReviewRepository.getByUrl(PR_URL, pageable))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));

        List<Developer> assignees = pullRequestReviewService.assign(PR_URL, Collections.emptyList(),
                List.of(DEVELOPER_BAR.getLogin()));
        Developer expectedAssignee = DEVELOPER_BAZ.withScore(PR_SCORE);

        assertEquals(List.of(expectedAssignee), assignees);
        verify(developerService).updateDeveloper(expectedAssignee);
    }

    @Test
    void testAssignWithConflictingAssignees() {
        PullRequestReview review = PullRequestReview.builder()
                .pullRequestUrl(PR_URL)
                .pullRequestFileDetails(PR_FILE_DETAILS)
                .score(PR_SCORE)
                .developer(DEVELOPER_FOO)
                .build();
        Pageable pageable = Pageable.unpaged();
        when(pullRequestReviewRepository.getByUrl(PR_URL, pageable))
                .thenReturn(new PageImpl<>(List.of(review), pageable, 1));

        List<String> assigneeList = List.of(DEVELOPER_FOO.getLogin(), DEVELOPER_BAR.getLogin());
        List<String> assigneeExclusionList = Collections.emptyList();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> pullRequestReviewService.assign(PR_URL, assigneeList, assigneeExclusionList));
        assertEquals("Developers [foo] have already reviewed the PR before", exception.getMessage());
    }

    @Test
    void testDeleteReview() {
        UUID id = REVIEW_1.getId();
        when(pullRequestReviewRepository.get(id)).thenReturn(Optional.of(REVIEW_1));

        assertDoesNotThrow(() -> pullRequestReviewService.deleteReview(id));

        verify(developerService)
                .updateDeveloper(DEVELOPER_FOO.withScore(DEVELOPER_FOO.getScore() - REVIEW_1.getScore()));
        verify(pullRequestReviewRepository).delete(id);
    }

    @Test
    void testDeleteReviewNotFound() {
        UUID id = REVIEW_1.getId();
        when(pullRequestReviewRepository.get(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pullRequestReviewService.deleteReview(id));

        assertEquals("Could not find review with ID: " + id, exception.getMessage());
    }

}
