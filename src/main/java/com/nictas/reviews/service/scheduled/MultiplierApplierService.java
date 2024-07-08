package com.nictas.reviews.service.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestReview;
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

    @Autowired
    public MultiplierApplierService(MultiplierService multiplierService,
                                    PullRequestScoreComputer pullRequestScoreComputer,
                                    PullRequestReviewService pullRequestReviewService) {
        this.multiplierService = multiplierService;
        this.pullRequestScoreComputer = pullRequestScoreComputer;
        this.pullRequestReviewService = pullRequestReviewService;
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
                    double score = pullRequestScoreComputer.computeScore(review.getPullRequestFileDetails(),
                            latestMultiplier);
                    log.info("Applying new score {} with latest multiplier to PR review: {}", score, review);
                    pullRequestReviewService.updateReview(review.withScore(score)
                            .withMultiplier(latestMultiplier));
                });
    }

}
