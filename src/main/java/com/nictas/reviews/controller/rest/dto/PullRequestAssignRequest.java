package com.nictas.reviews.controller.rest.dto;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
public class PullRequestAssignRequest {

    private String pullRequestUrl;
    @Builder.Default
    private List<String> assigneeList = Collections.emptyList();
    @Builder.Default
    private List<String> assigneeExclusionList = Collections.emptyList();

}
