package com.nictas.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.nictas.reviews.service.github.settings.GitHubSettings;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;
import com.nictas.reviews.service.scheduled.DeveloperSyncService;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class FairReviewsApplicationTests {

    private static final String SETTINGS_FOO_URL = "https://foo.example.com";
    private static final String SETTINGS_BAR_URL = "https://bar.example.com";

    @Autowired
    private GitHubSettingsProvider settingsProvider;

    @MockBean
    private DeveloperSyncService developerSyncService;

    @Test
    void contextLoads() {
        GitHubSettings settingsFoo = settingsProvider.getSettingsForUrl(SETTINGS_FOO_URL);
        assertEquals(SETTINGS_FOO_URL, settingsFoo.getUrl());
        assertEquals(SETTINGS_FOO_URL + "/api/v3", settingsFoo.getApi());
        assertEquals("tokenFoo", settingsFoo.getToken());

        GitHubSettings settingsBar = settingsProvider.getSettingsForUrl(SETTINGS_BAR_URL);
        assertEquals(SETTINGS_BAR_URL, settingsBar.getUrl());
        assertEquals(SETTINGS_BAR_URL + "/api/v3", settingsBar.getApi());
        assertEquals("tokenBar", settingsBar.getToken());
    }

}
