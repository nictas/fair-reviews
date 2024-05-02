package com.nictas.reviews.controller.rest.dto;

import java.util.List;

import com.nictas.reviews.domain.Developer;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@RequiredArgsConstructor
public class PullRequestAssignResponse {

    private final List<Developer> assignees;

}
