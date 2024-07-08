package com.nictas.reviews.service.score;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.Multiplier.FileMultiplier;
import com.nictas.reviews.domain.PullRequest;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.github.GitHubClient;
import com.nictas.reviews.service.github.GitHubClientProvider;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PullRequestScoreComputer {

    private final GitHubClientProvider clientProvider;
    private final MultiplierService multiplierService;

    @Autowired
    public PullRequestScoreComputer(GitHubClientProvider clientProvider, MultiplierService multiplierService) {
        this.clientProvider = clientProvider;
        this.multiplierService = multiplierService;
    }

    public PullRequestAssessment computeScore(String pullRequestUrl) {
        GitHubClient client = clientProvider.getClientForUrl(pullRequestUrl);

        log.info("Computing score for PR {}", pullRequestUrl);
        PullRequest pullRequest = PullRequest.fromUrl(pullRequestUrl);
        PullRequestFileDetails pullRequestFileDetails = client.getPullRequestInfo(pullRequest);
        log.info("Fetched info for PR {}: {}", pullRequestUrl, pullRequestFileDetails);
        Multiplier multiplier = multiplierService.getLatestMultiplier();
        double score = computeScore(pullRequestFileDetails, multiplier);

        return PullRequestAssessment.builder()
                .pullRequestUrl(pullRequestUrl)
                .pullRequestFileDetails(pullRequestFileDetails)
                .score(score)
                .multiplier(multiplier)
                .build();
    }

    private double computeScore(PullRequestFileDetails pullRequestInfo, Multiplier multiplier) {
        return pullRequestInfo.getChangedFiles()
                .stream()
                .mapToDouble(file -> computeScore(file, multiplier))
                .sum();
    }

    private double computeScore(ChangedFile file, Multiplier multiplier) {
        FileMultiplier fileMultiplier = findFileMultiplier(file, multiplier);
        if (fileMultiplier == null) {
            return computeScoreWithDefaultMultipliers(file, multiplier);
        }
        return computeScore(file, fileMultiplier);
    }

    private FileMultiplier findFileMultiplier(ChangedFile file, Multiplier multiplier) {
        return multiplier.getFileMultipliers()
                .stream()
                .filter(fileMultiplier -> file.getName()
                        .endsWith(fileMultiplier.getFileExtension()))
                .findFirst()
                .orElse(null);
    }

    private double computeScoreWithDefaultMultipliers(ChangedFile file, Multiplier multiplier) {
        double additionsMultiplier = multiplier.getDefaultAdditionsMultiplier();
        double deletionsMultiplier = multiplier.getDefaultDeletionsMultiplier();
        return computeScore(file, additionsMultiplier, deletionsMultiplier);
    }

    private double computeScore(ChangedFile file, FileMultiplier fileMultiplier) {
        return computeScore(file, fileMultiplier.getAdditionsMultiplier(), fileMultiplier.getDeletionsMultiplier());
    }

    private double computeScore(ChangedFile file, double additionsMultiplier, double deletionsMultiplier) {
        double additionsScore = file.getAdditions() * additionsMultiplier;
        double deletionsScore = file.getDeletions() * deletionsMultiplier;
        return additionsScore + deletionsScore;
    }

    @Data
    @Builder
    @RequiredArgsConstructor
    public static class PullRequestAssessment {

        private final String pullRequestUrl;
        private final PullRequestFileDetails pullRequestFileDetails;
        private final double score;
        private final Multiplier multiplier;

    }

}
