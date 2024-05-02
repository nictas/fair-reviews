package com.nictas.reviews.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@With
@Builder
@Jacksonized
@AllArgsConstructor
public class PullRequestReview {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private final String pullRequestUrl;
    private final PullRequestFileDetails pullRequestFileDetails;
    private final double score;
    private final Developer developer;

}
