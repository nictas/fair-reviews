package com.nictas.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import com.nictas.reviews.service.github.settings.GitHubSettings;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;
import com.nictas.reviews.service.scheduled.DeveloperSyncService;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(initializers = {FairReviewsApplicationTests.Initializer.class})
class FairReviewsApplicationTests {

    private static final String SETTINGS_FOO_URL = "https://foo.example.com";
    private static final String SETTINGS_BAR_URL = "https://bar.example.com";

    public static final FairReviewsPostgreSQLContainer POSTGRESQL_CONTAINER = FairReviewsPostgreSQLContainer
            .getInstance();

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            POSTGRESQL_CONTAINER.start();
            TestPropertyValues
                    .of("spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                            "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                            "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword())
                    .applyTo(configurableApplicationContext.getEnvironment());
        }

    }

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
