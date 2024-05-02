package com.nictas.reviews.controller.rest.dto;

import java.util.UUID;

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
    private final PullRequestFileDetails pullRequestFileDetails;
    private final double score;

    public static PullRequestReviewWithoutDeveloper from(PullRequestReview review) {
        return PullRequestReviewWithoutDeveloper.builder()
                .id(review.getId())
                .pullRequestUrl(review.getPullRequestUrl())
                .pullRequestFileDetails(review.getPullRequestFileDetails())
                .build();
    }

}
