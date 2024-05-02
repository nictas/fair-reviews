package com.nictas.reviews.service.github.settings;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class GitHubSettings {

    private final String url;
    private final String api;
    private final String token;

}
