package com.nictas.reviews.service.github.settings;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@Builder
@RequiredArgsConstructor
public class GitHubSettings {

    private final String url;
    private final String api;
    @ToString.Exclude
    private final String token;

}
