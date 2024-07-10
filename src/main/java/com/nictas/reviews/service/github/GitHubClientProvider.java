package com.nictas.reviews.service.github;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nictas.reviews.service.github.settings.GitHubSettings;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GitHubClientProvider {

    private final GitHubSettingsProvider settingsProvider;
    private final BiFunction<String, String, GitHub> delegateConstructor;
    private final Map<String, GitHubClient> clientCache;

    @Autowired
    public GitHubClientProvider(GitHubSettingsProvider settingsProvider) {
        this(settingsProvider, GitHubClientProvider::createDelegate);
    }

    GitHubClientProvider(GitHubSettingsProvider settingsProvider,
                         BiFunction<String, String, GitHub> delegateConstructor) {
        this.settingsProvider = settingsProvider;
        this.delegateConstructor = delegateConstructor;
        this.clientCache = new ConcurrentHashMap<>();
    }

    public GitHubClient getClientForUrl(String url) {
        GitHubSettings settings = settingsProvider.getSettingsForUrl(url);
        log.info("Received settings for URL: {}", settings);
        return clientCache.computeIfAbsent(settings.getUrl(),
                unused -> createClient(settings.getApi(), settings.getToken()));
    }

    public GitHubClient getClientForUrl(String url, String token) {
        GitHubSettings settings = settingsProvider.getSettingsForUrl(url);
        log.info("Received settings for URL: {}", settings);
        return createClient(settings.getApi(), token);
    }

    private GitHubClient createClient(String apiUrl, String token) {
        log.info("Creating GitHub client for URL: {}", apiUrl);
        return new GitHubClient(delegateConstructor.apply(apiUrl, token));
    }

    private static GitHub createDelegate(String apiUrl, String token) {
        try {
            return new GitHubBuilder().withEndpoint(apiUrl)
                    .withOAuthToken(token)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create GitHub client for URL: " + apiUrl);
        }
    }

}
