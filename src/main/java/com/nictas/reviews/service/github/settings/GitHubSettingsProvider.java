package com.nictas.reviews.service.github.settings;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GitHubSettingsProvider {

    static final String GITHUB_URL = "https://github.com";
    static final String GITHUB_API_URL = "https://api.github.com";
    static final String GITHUB_ENTERPRISE_API_ENDPOINT = "/api/v3";

    private ObjectMapper objectMapper;
    private Map<String, String> gitHubTokens;

    @Autowired
    public GitHubSettingsProvider(ObjectMapper objectMapper, @Value("${github.tokens}") String gitHubTokensJson) {
        this.objectMapper = objectMapper;
        this.gitHubTokens = deserializeGitHubToken(gitHubTokensJson);
    }

    private Map<String, String> deserializeGitHubToken(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize GitHub tokens JSON: " + e.getMessage(), e);
        }
    }

    public GitHubSettings getSettingsForUrl(String url) {
        for (Map.Entry<String, String> gitHubToken : gitHubTokens.entrySet()) {
            if (url.startsWith(gitHubToken.getKey())) {
                return buildGitHubSettings(gitHubToken.getKey(), gitHubToken.getValue());
            }
        }
        throw new IllegalStateException("Unable to find GitHub token for URL: " + url);
    }

    private GitHubSettings buildGitHubSettings(String url, String token) {
        String api = getApiForUrl(url);
        return GitHubSettings.builder()
                .token(token)
                .url(url)
                .api(api)
                .build();
    }

    private String getApiForUrl(String url) {
        if (GITHUB_URL.equals(url)) {
            return GITHUB_API_URL;
        }
        return url + GITHUB_ENTERPRISE_API_ENDPOINT;
    }

}
