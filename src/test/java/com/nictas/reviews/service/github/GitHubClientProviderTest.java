package com.nictas.reviews.service.github;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Function;

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
    private Function<GitHubSettings, GitHub> delegateConstructor;
    @InjectMocks
    private GitHubClientProvider clientProvider;

    @Test
    void testGetClientForUrl() {
        when(settingsProvider.getSettingsForUrl(SETTINGS_FOO.getUrl())).thenReturn(SETTINGS_FOO);
        when(settingsProvider.getSettingsForUrl(SETTINGS_BAR.getUrl())).thenReturn(SETTINGS_BAR);
        when(delegateConstructor.apply(SETTINGS_FOO)).thenReturn(delegateFoo);
        when(delegateConstructor.apply(SETTINGS_BAR)).thenReturn(delegateBar);

        GitHubClient clientFoo = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        assertSame(delegateFoo, clientFoo.getDelegate());
        GitHubClient clientBar = clientProvider.getClientForUrl(SETTINGS_BAR.getUrl());
        assertSame(delegateBar, clientBar.getDelegate());

        verify(delegateConstructor, times(1)).apply(SETTINGS_FOO);
        verify(delegateConstructor, times(1)).apply(SETTINGS_BAR);
    }

    @Test
    void testGetClientForUrlCaching() {
        when(settingsProvider.getSettingsForUrl(SETTINGS_FOO.getUrl())).thenReturn(SETTINGS_FOO);
        when(delegateConstructor.apply(SETTINGS_FOO)).thenReturn(delegateFoo);

        GitHubClient clientFoo1 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        // Get the same client a few more times to check whether the caching is working
        GitHubClient clientFoo2 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        GitHubClient clientFoo3 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        GitHubClient clientFoo4 = clientProvider.getClientForUrl(SETTINGS_FOO.getUrl());
        assertSame(clientFoo1, clientFoo2);
        assertSame(clientFoo2, clientFoo3);
        assertSame(clientFoo3, clientFoo4);

        verify(delegateConstructor, times(1)).apply(SETTINGS_FOO);
    }

}
