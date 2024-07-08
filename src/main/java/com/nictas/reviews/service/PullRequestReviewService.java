package com.nictas.reviews.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.PullRequestReviewRepository;
import com.nictas.reviews.service.score.PullRequestScoreComputer;
import com.nictas.reviews.service.score.PullRequestScoreComputer.PullRequestAssessment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PullRequestReviewService {

    private final DeveloperService developerService;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final PullRequestScoreComputer pullRequestScoreComputer;

    @Autowired
    public PullRequestReviewService(DeveloperService developerService,
                                    PullRequestReviewRepository pullRequestReviewRepository,
                                    PullRequestScoreComputer pullRequestScoreComputer) {
        this.developerService = developerService;
        this.pullRequestReviewRepository = pullRequestReviewRepository;
        this.pullRequestScoreComputer = pullRequestScoreComputer;
    }

    public Page<PullRequestReview> getAllReviews(Pageable pageable) {
        log.info("Getting all pull request reviews");
        return pullRequestReviewRepository.getAll(pageable);
    }

    public PullRequestReview getReview(UUID id) {
        log.info("Getting pull request review {}", id);
        return pullRequestReviewRepository.get(id)
                .orElseThrow(() -> new NotFoundException("Could not find review with ID: " + id));
    }

    public Page<PullRequestReview> getReviewsByUrl(String pullRequestUrl, Pageable pageable) {
        log.info("Getting all pull request reviews with URL {}", pullRequestUrl);
        return pullRequestReviewRepository.getByUrl(pullRequestUrl, pageable);
    }

    public Page<PullRequestReview> getReviewsByDeveloperLogin(String developerLogin, Pageable pageable) {
        log.info("Getting all pull request reviews for developer {}", developerLogin);
        return pullRequestReviewRepository.getByDeveloperLogin(developerLogin, pageable);
    }

    public void createReview(PullRequestReview pullRequestReview) {
        log.info("Creating pull request review: {}");
        pullRequestReviewRepository.create(pullRequestReview);
    }

    public void updateReview(PullRequestReview pullRequestReview) {
        log.info("Updating pull request review: {}");
        pullRequestReviewRepository.update(pullRequestReview);
    }

    public void deleteReview(UUID id) {
        log.info("Deleting pull request review {}", id);
        var review = getReview(id);
        decreaseDeveloperScore(review);
        pullRequestReviewRepository.delete(id);
    }

    public List<Developer> assign(String pullRequestUrl, List<String> assigneeList,
                                  List<String> assigneeExclusionList) {
        log.info("Assigning pull request {} to a developer with assignee list {} and assignee exclusion list {}",
                pullRequestUrl, assigneeList, assigneeExclusionList);
        List<Developer> assignees = getAssignees(assigneeList, assigneeExclusionList);
        PullRequestAssessment assessment = pullRequestScoreComputer.computeScore(pullRequestUrl);
        return assignees.stream()
                .map(assignee -> increaseDeveloperScore(assignee, assessment))
                .toList();
    }

    private List<Developer> getAssignees(List<String> assigneeList, List<String> assigneeExclusionList) {
        if (assigneeList.isEmpty()) {
            return List.of(developerService.getDeveloperWithLowestScore(assigneeExclusionList));
        }
        return assigneeList.stream()
                .map(developerService::getDeveloper)
                .toList();
    }

    private Developer increaseDeveloperScore(Developer developer, PullRequestAssessment assessment) {
        Developer developerWithIncreasedScore = developer.withScore(developer.getScore() + assessment.getScore());
        developerService.updateDeveloper(developerWithIncreasedScore);
        pullRequestReviewRepository.create(PullRequestReview.builder()
                .pullRequestUrl(assessment.getPullRequestUrl())
                .pullRequestFileDetails(assessment.getPullRequestFileDetails())
                .score(assessment.getScore())
                .developer(developerWithIncreasedScore)
                .build());
        return developerWithIncreasedScore;
    }

    private void decreaseDeveloperScore(PullRequestReview review) {
        Developer developer = review.getDeveloper();
        Developer developerWithDecreasedScore = developer.withScore(developer.getScore() - review.getScore());
        developerService.updateDeveloper(developerWithDecreasedScore);
    }

}
