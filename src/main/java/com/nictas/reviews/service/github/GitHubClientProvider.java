package com.nictas.reviews.service.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nictas.reviews.service.github.settings.GitHubSettings;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;

@Component
public class GitHubClientProvider {

    private final GitHubSettingsProvider settingsProvider;
    private final Function<GitHubSettings, GitHub> delegateConstructor;
    private final Map<String, GitHubClient> clientCache;

    @Autowired
    public GitHubClientProvider(GitHubSettingsProvider settingsProvider) {
        this(settingsProvider, GitHubClientProvider::createDelegate);
    }

    GitHubClientProvider(GitHubSettingsProvider settingsProvider,
                         Function<GitHubSettings, GitHub> delegateConstructor) {
        this.settingsProvider = settingsProvider;
        this.delegateConstructor = delegateConstructor;
        this.clientCache = new HashMap<>();
    }

    public GitHubClient getClientForUrl(String url) {
        GitHubSettings settings = settingsProvider.getSettingsForUrl(url);
        return clientCache.computeIfAbsent(settings.getUrl(), unused -> createClient(settings));
    }

    private GitHubClient createClient(GitHubSettings settings) {
        return new GitHubClient(delegateConstructor.apply(settings));
    }

    private static GitHub createDelegate(GitHubSettings settings) {
        try {
            return new GitHubBuilder().withEndpoint(settings.getApi())
                    .withOAuthToken(settings.getToken())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create GitHub client for URL: " + settings.getUrl());
        }
    }

}
