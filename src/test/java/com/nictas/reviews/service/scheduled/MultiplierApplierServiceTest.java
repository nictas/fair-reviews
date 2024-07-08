package com.nictas.reviews.service.scheduled;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.PullRequestReviewService;
import com.nictas.reviews.service.score.PullRequestScoreComputer;

@ExtendWith(MockitoExtension.class)
class MultiplierApplierServiceTest {

    private static final Developer DEVELOPER = new Developer("foo", "foo@example.com");

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

    private static final Multiplier MULTIPLIER_2 = Multiplier.builder()
            .id(UUID.fromString("98626460-80e1-4acc-b2ea-b28e018ca6d2"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(3.0)
                            .deletionsMultiplier(0.2)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.2)
                            .deletionsMultiplier(0.1)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 5, 13, 6, 0, 0, 0, ZoneOffset.UTC))
            .build();

    private static final PullRequestReview REVIEW_1 = PullRequestReview.builder()
            .id(UUID.fromString("91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
            .pullRequestUrl("https://github.com/foo/bar/pull/87")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(15)
                            .deletions(11)
                            .build())))
            .score(20.)
            .developer(DEVELOPER)
            .multiplier(MULTIPLIER_1)
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
            .score(60.)
            .developer(DEVELOPER)
            .multiplier(MULTIPLIER_1)
            .build();

    @Mock
    private MultiplierService multiplierService;
    @Mock
    private PullRequestScoreComputer pullRequestScoreComputer;
    @Mock
    private PullRequestReviewService pullRequestReviewService;
    @Mock
    private DeveloperService developerService;
    @InjectMocks
    private MultiplierApplierService multiplierApplierService;

    @Test
    void testApplyLatestMultiplier() {
        when(multiplierService.getLatestMultiplier()).thenReturn(MULTIPLIER_2);
        Pageable pageable = Pageable.unpaged();
        when(pullRequestReviewService.getReviewsWithDifferentMultiplierIds(MULTIPLIER_2.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(REVIEW_1, REVIEW_2), pageable, 2));
        when(pullRequestScoreComputer.computeScore(REVIEW_1.getPullRequestFileDetails(), MULTIPLIER_2))
                .thenReturn(100.);
        when(pullRequestScoreComputer.computeScore(REVIEW_2.getPullRequestFileDetails(), MULTIPLIER_2))
                .thenReturn(200.);
        when(developerService.getDeveloper(DEVELOPER.getLogin())).thenReturn(DEVELOPER);

        multiplierApplierService.applyLatestMultiplier();

        Developer developerWithUpdatedScore1 = DEVELOPER.withScore(80.);
        verify(pullRequestReviewService).saveReview(REVIEW_1.withScore(100.)
                .withMultiplier(MULTIPLIER_2)
                .withDeveloper(developerWithUpdatedScore1));
        verify(developerService).saveDeveloper(developerWithUpdatedScore1);
        Developer developerWithUpdatedScore2 = DEVELOPER.withScore(140.);
        verify(pullRequestReviewService).saveReview(REVIEW_2.withScore(200.)
                .withMultiplier(MULTIPLIER_2)
                .withDeveloper(developerWithUpdatedScore2));
        verify(developerService).saveDeveloper(developerWithUpdatedScore2);
    }

}
