package com.nictas.reviews.service.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.Multiplier.FileMultiplier;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;
import com.nictas.reviews.service.score.PullRequestScoreComputer.PullRequestAssessment;

@ExtendWith(MockitoExtension.class)
class PullRequestScoreComputerTest {

    private static final String PR_URL = "https://github.com/example/repository/pull/123";

    private static final Multiplier MULTIPLIER = Multiplier.builder()
            .id(UUID.fromString("2f7fc3e6-b54f-4593-aaca-98aeed3d6d02"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(2.0)
                            .deletionsMultiplier(0.1)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.5)
                            .deletionsMultiplier(0.2)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 3, 3, 17, 15, 0, 0, ZoneOffset.UTC))
            .build();

    @Mock
    private GitHubClientProvider clientProvider;

    @Mock
    private GitHubClient client;

    @Mock
    private MultiplierService multiplierService;

    @InjectMocks
    private PullRequestScoreComputer scoreComputer;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(PR_URL)).thenReturn(client);
        when(multiplierService.getLatestMultiplier()).thenReturn(MULTIPLIER);
    }

    @Test
    void testComputeScore() {
        PullRequestFileDetails pullRequestInfo = new PullRequestFileDetails(List.of(//
                ChangedFile.builder()
                        .name("file1.txt")
                        .additions(20)
                        .deletions(10) // 22
                        .build(),
                ChangedFile.builder()
                        .name("file2.java")
                        .additions(25)
                        .deletions(10) // 51
                        .build(),
                ChangedFile.builder()
                        .name("file3.yaml")
                        .additions(10)
                        .deletions(20) // 9
                        .build(),
                ChangedFile.builder()
                        .name("file4.yaml")
                        .additions(40) // 30
                        .deletions(50)
                        .build()));
        when(client.getPullRequestInfo(any())).thenReturn(pullRequestInfo);

        PullRequestAssessment assessment = scoreComputer.computeScore(PR_URL);
        PullRequestAssessment expectedAssessment = new PullRequestAssessment(PR_URL, pullRequestInfo, 112., MULTIPLIER);

        assertEquals(expectedAssessment, assessment);
    }

    @Test
    void testComputeScoreWithNoChanges() {
        PullRequestFileDetails pullRequestInfo = new PullRequestFileDetails(Collections.emptyList());
        when(client.getPullRequestInfo(any())).thenReturn(pullRequestInfo);

        PullRequestAssessment assessment = scoreComputer.computeScore(PR_URL);
        PullRequestAssessment expectedAssessment = new PullRequestAssessment(PR_URL, pullRequestInfo, 0., MULTIPLIER);

        assertEquals(expectedAssessment, assessment);
    }

}
