package com.nictas.reviews.service.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.PullRequestReviewService;
import com.nictas.reviews.service.score.PullRequestScoreComputer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MultiplierApplierService {

    private final MultiplierService multiplierService;
    private final PullRequestScoreComputer pullRequestScoreComputer;
    private final PullRequestReviewService pullRequestReviewService;
    private final DeveloperService developerService;

    @Autowired
    public MultiplierApplierService(MultiplierService multiplierService,
                                    PullRequestScoreComputer pullRequestScoreComputer,
                                    PullRequestReviewService pullRequestReviewService,
                                    DeveloperService developerService) {
        this.multiplierService = multiplierService;
        this.pullRequestScoreComputer = pullRequestScoreComputer;
        this.pullRequestReviewService = pullRequestReviewService;
        this.developerService = developerService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void applyLatestMultiplier() {
        log.info("Applying latest multiplier to all existing PR reviews");
        Multiplier latestMultiplier = multiplierService.getLatestMultiplier();
        log.info("Latest multiplier: {}", latestMultiplier);
        Page<PullRequestReview> reviews = pullRequestReviewService
                .getReviewsWithDifferentMultiplierIds(latestMultiplier.getId(), Pageable.unpaged());
        applyMultiplier(latestMultiplier, reviews);
    }

    private void applyMultiplier(Multiplier latestMultiplier, Page<PullRequestReview> reviews) {
        reviews.stream()
                .forEach(review -> {
                    double oldScore = review.getScore();
                    double newScore = pullRequestScoreComputer.computeScore(review.getPullRequestFileDetails(),
                            latestMultiplier);
                    double scoreDifference = newScore - oldScore;
                    Developer developer = developerService.getDeveloper(review.getDeveloper()
                            .getLogin());
                    Developer updatedDeveloper = developer.withScore(developer.getScore() + scoreDifference);
                    log.info("Applying new score {} with latest multiplier to PR review: {}", newScore, review);
                    pullRequestReviewService.updateReview(review.withScore(newScore)
                            .withMultiplier(latestMultiplier)
                            .withDeveloper(updatedDeveloper));
                    log.info("Applying score difference {} to developer: {}", scoreDifference, developer);
                    developerService.updateDeveloper(updatedDeveloper);
                });
    }

}
