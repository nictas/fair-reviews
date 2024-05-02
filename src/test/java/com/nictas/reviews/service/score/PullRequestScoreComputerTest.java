package com.nictas.reviews.service.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;
import com.nictas.reviews.service.score.PullRequestScoreComputer.PullRequestAssessment;

@ExtendWith(MockitoExtension.class)
class PullRequestScoreComputerTest {

    private static final String PR_URL = "https://github.com/example/repository/pull/123";

    @Mock
    private GitHubClientProvider clientProvider;

    @Mock
    private GitHubClient client;

    @InjectMocks
    private PullRequestScoreComputer scoreComputer;

    @BeforeEach
    void setUp() {
        when(clientProvider.getClientForUrl(PR_URL)).thenReturn(client);
    }

    @Test
    void testComputeScore() {
        PullRequestFileDetails pullRequestInfo = new PullRequestFileDetails(List.of(//
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
        when(client.getPullRequestInfo(any())).thenReturn(pullRequestInfo);

        PullRequestAssessment assessment = scoreComputer.computeScore(PR_URL);
        PullRequestAssessment expectedAssessment = new PullRequestAssessment(PR_URL, pullRequestInfo, 243.);

        assertEquals(expectedAssessment, assessment);
    }

    @Test
    void testComputeScoreWithNoChanges() {
        PullRequestFileDetails pullRequestInfo = new PullRequestFileDetails(Collections.emptyList());
        when(client.getPullRequestInfo(any())).thenReturn(pullRequestInfo);

        PullRequestAssessment assessment = scoreComputer.computeScore(PR_URL);
        PullRequestAssessment expectedAssessment = new PullRequestAssessment(PR_URL, pullRequestInfo, 0.);

        assertEquals(expectedAssessment, assessment);
    }

}
