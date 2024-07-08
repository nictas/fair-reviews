package com.nictas.reviews.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.PullRequestReview;

public interface PullRequestReviewRepository {

    Page<PullRequestReview> getAll(Pageable pageable);

    Optional<PullRequestReview> get(UUID id);

    Page<PullRequestReview> getByUrl(String pullRequestUrl, Pageable pageable);

    Page<PullRequestReview> getByDeveloperLogin(String developerLogin, Pageable pageable);

    Page<PullRequestReview> getWithDifferentMultiplierIds(UUID id, Pageable pageable);

    void create(PullRequestReview pullRequestReview);

    void update(PullRequestReview pullRequestReview);

    int delete(UUID id);

}
