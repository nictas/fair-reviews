package com.nictas.reviews.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.util.PaginationUtils;

@Component
public class InMemoryPullRequestReviewRepository implements PullRequestReviewRepository {

    private final List<PullRequestReview> pullRequestReviews = new ArrayList<>();

    @Override
    public Page<PullRequestReview> getAll(Pageable pageable) {
        return PaginationUtils.applyPagination(pullRequestReviews, pageable);
    }

    @Override
    public Optional<PullRequestReview> get(UUID id) {
        return pullRequestReviews.stream()
                .filter(review -> id.equals(review.getId()))
                .findFirst();
    }

    @Override
    public Page<PullRequestReview> getByUrl(String pullRequestUrl, Pageable pageable) {
        List<PullRequestReview> result = pullRequestReviews.stream()
                .filter(review -> pullRequestUrl.equals(review.getPullRequestUrl()))
                .toList();
        return PaginationUtils.applyPagination(result, pageable);
    }

    @Override
    public Page<PullRequestReview> getByDeveloperLogin(String developerLogin, Pageable pageable) {
        List<PullRequestReview> result = pullRequestReviews.stream()
                .filter(review -> developerLogin.equals(review.getDeveloper()
                        .getLogin()))
                .toList();
        return PaginationUtils.applyPagination(result, pageable);
    }

    @Override
    public void create(PullRequestReview pullRequestReview) {
        pullRequestReviews.add(pullRequestReview);
    }

    @Override
    public void update(PullRequestReview pullRequestReview) {
        delete(pullRequestReview.getId());
        create(pullRequestReview);
    }

    @Override
    public int delete(UUID id) {
        var deleted = pullRequestReviews.removeIf(candidate -> id.equals(candidate.getId()));
        return deleted ? 1 : 0;
    }

}
