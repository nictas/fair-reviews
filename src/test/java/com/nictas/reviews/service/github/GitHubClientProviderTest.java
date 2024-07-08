package com.nictas.reviews.service.github;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.service.github.settings.GitHubSettings;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;

@ExtendWith(MockitoExtension.class)
class GitHubClientProviderTest {

    private static final GitHubSettings SETTINGS_FOO = new GitHubSettings("https://foo.example.com",
            "https://foo.example.com/api/v3", "token");
    private static final GitHubSettings SETTINGS_BAR = new GitHubSettings("https://bar.example.com",
            "https://bar.example.com/api/v3", "token");

    @Mock
    private GitHubSettingsProvider settingsProvider;
    @Mock
    private GitHub delegateFoo;
    @Mock
    private GitHub delegateBar;
    @Mock
    private BiFunction<String, String, GitHub> delegateConstructor;
    @InjectMocks
    private GitHubClientProvider clientProvider;

    @Test
    void testGetClientForUrl() {
        when(settingsProvider.getSettingsForUrl(SETTINGS_FOO.getUrl())).thenReturn(SETTINGS_FOO);
        when(settingsProvider.getSettingsForUrl(SETTINGS_BAR.getUrl())).thenReturn(SETTINGS_BAR);
        when(delegateConstructor.apply(SETTINGS_FOO.getApi(), SETTINGS_FOO.getToken())).thenReturn(delegateFoo);
        when(delegateConstructor.apply(SETTINGS_BAR.getApi(), SETTINGS_BAR.getToken())).thenReturn(delegateBar);

        GitHubClient clientFoo = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        assertSame(delegateFoo, clientFoo.getDelegate());
        GitHubClient clientBar = clientProvider.getClientForUrl(SETTINGS_BAR.getUrl());
        assertSame(delegateBar, clientBar.getDelegate());

        verify(delegateConstructor).apply(SETTINGS_FOO.getApi(), SETTINGS_FOO.getToken());
        verify(delegateConstructor).apply(SETTINGS_BAR.getApi(), SETTINGS_BAR.getToken());
    }

    @Test
    void testGetClientForUrlCaching() {
        when(settingsProvider.getSettingsForUrl(SETTINGS_FOO.getUrl())).thenReturn(SETTINGS_FOO);
        when(delegateConstructor.apply(SETTINGS_FOO.getApi(), SETTINGS_FOO.getToken())).thenReturn(delegateFoo);

        GitHubClient clientFoo1 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        // Get the same client a few more times to check whether the caching is working
        GitHubClient clientFoo2 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        GitHubClient clientFoo3 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        GitHubClient clientFoo4 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        assertSame(clientFoo1, clientFoo2);
        assertSame(clientFoo2, clientFoo3);
        assertSame(clientFoo3, clientFoo4);

        verify(delegateConstructor).apply(SETTINGS_FOO.getApi(), SETTINGS_FOO.getToken());
    }

    @Test
    void testGetClientForUrlWithToken() {
        String token = "test";
        when(settingsProvider.getSettingsForUrl(SETTINGS_FOO.getUrl())).thenReturn(SETTINGS_FOO);
        when(delegateConstructor.apply(SETTINGS_FOO.getApi(), token)).thenReturn(delegateFoo);

        GitHubClient clientFoo = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl(), token);
        assertSame(delegateFoo, clientFoo.getDelegate());

        verify(delegateConstructor).apply(SETTINGS_FOO.getApi(), token);
    }

}
