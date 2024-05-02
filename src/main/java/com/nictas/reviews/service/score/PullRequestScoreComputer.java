package com.nictas.reviews.service.score;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.PullRequest;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;

import lombok.Data;

@Component
public class PullRequestScoreComputer {

    private final GitHubClientProvider clientProvider;

    @Autowired
    public PullRequestScoreComputer(GitHubClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    public PullRequestAssessment computeScore(String pullRequestUrl) {
        GitHubClient client = clientProvider.getClientForUrl(pullRequestUrl);

        PullRequest pullRequest = PullRequest.fromUrl(pullRequestUrl);
        PullRequestFileDetails pullRequestFileDetails = client.getPullRequestInfo(pullRequest);
        double score = computeScore(pullRequestFileDetails);

        return new PullRequestAssessment(pullRequestUrl, pullRequestFileDetails, score);
    }

    private double computeScore(PullRequestFileDetails pullRequestInfo) {
        return pullRequestInfo.getChangedFiles()
                .stream()
                .mapToInt(file -> file.getAdditions() + file.getDeletions())
                .sum();
    }

    @Data
    public static class PullRequestAssessment {

        private final String pullRequestUrl;
        private final PullRequestFileDetails pullRequestFileDetails;
        private final double score;

    }

}
