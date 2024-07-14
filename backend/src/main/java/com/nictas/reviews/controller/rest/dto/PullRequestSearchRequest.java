package com.nictas.reviews.controller.rest.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@RequiredArgsConstructor
public class PullRequestSearchRequest {

    private final String pullRequestUrl;

}
