package com.nictas.reviews.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nictas.reviews.domain.PullRequestReview;

@Repository
public interface PullRequestReviewRepository extends JpaRepository<PullRequestReview, UUID> {

    Page<PullRequestReview> findByPullRequestUrl(String pullRequestUrl, Pageable pageable);

    Page<PullRequestReview> findByDeveloperLogin(String developerLogin, Pageable pageable);

    @Query("SELECT p FROM PullRequestReview p WHERE p.multiplier.id != :id")
    Page<PullRequestReview> findWithDifferentMultiplierIds(UUID id, Pageable pageable);

}
