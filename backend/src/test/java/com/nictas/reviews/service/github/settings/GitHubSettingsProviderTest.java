package com.nictas.reviews.service.github.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GitHubSettingsProviderTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String GITHUB_URL = "https://github.com";
    private static final String GITHUB_TOKEN = "ghp_12345";
    private static final String ENTERPRISE_GITHUB_URL = "https://enterprise.github.com";
    private static final String ENTERPRISE_GITHUB_TOKEN = "ghp_67890";
    private static final Map<String, String> GITHUB_TOKENS = Map.of(GITHUB_URL, GITHUB_TOKEN, ENTERPRISE_GITHUB_URL,
            ENTERPRISE_GITHUB_TOKEN);

    private GitHubSettingsProvider settingsProvider;

    @BeforeEach
    void setUp() throws IOException {
        String gitHubTokensJson = OBJECT_MAPPER.writeValueAsString(GITHUB_TOKENS);

        settingsProvider = new GitHubSettingsProvider(OBJECT_MAPPER, gitHubTokensJson);
    }

    @Test
    void testGetSettingsForUrlGitHub() {
        GitHubSettings settings = settingsProvider.getSettingsForUrl(GITHUB_URL);
        assertNotNull(settings);
        assertEquals(GITHUB_URL, settings.getUrl());
        assertEquals(GITHUB_TOKEN, settings.getToken());
        assertEquals(GitHubSettingsProvider.GITHUB_API_URL, settings.getApi());
    }

    @Test
    void testGetSettingsForUrlGitHubEnterprise() {
        GitHubSettings settings = settingsProvider.getSettingsForUrl(ENTERPRISE_GITHUB_URL);
        assertNotNull(settings);
        assertEquals(ENTERPRISE_GITHUB_URL, settings.getUrl());
        assertEquals(ENTERPRISE_GITHUB_TOKEN, settings.getToken());
        assertEquals(ENTERPRISE_GITHUB_URL + "/api/v3", settings.getApi());
    }

    @Test
    void testGetSettingsForUrlWithUnknownUrl() {
        String unknownUrl = "https://unknown.url";
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> settingsProvider.getSettingsForUrl(unknownUrl));

        assertEquals("Unable to find GitHub token for URL: " + unknownUrl, exception.getMessage());
    }

    @Test
    void testConstructorWithInvalidJson() {
        assertThrows(IllegalStateException.class, () -> new GitHubSettingsProvider(OBJECT_MAPPER, "invalidJson"));
    }

}
