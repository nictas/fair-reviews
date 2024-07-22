package com.nictas.reviews.controller.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestReview;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@RequiredArgsConstructor
public class PullRequestReviewWithoutDeveloper {

    private final UUID id;
    private final String pullRequestUrl;
    private final double score;
    private final PullRequestFileDetails pullRequestFileDetails;
    private final Multiplier multiplier;
    private final OffsetDateTime createdAt;

    public static PullRequestReviewWithoutDeveloper from(PullRequestReview review) {
        return PullRequestReviewWithoutDeveloper.builder()
                .id(review.getId())
                .score(review.getScore())
                .pullRequestUrl(review.getPullRequestUrl())
                .pullRequestFileDetails(review.getPullRequestFileDetails())
                .multiplier(review.getMultiplier())
                .createdAt(review.getCreatedAt())
                .build();
    }

}
