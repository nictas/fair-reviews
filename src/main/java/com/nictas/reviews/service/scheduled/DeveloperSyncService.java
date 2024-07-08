package com.nictas.reviews.service.scheduled;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.github.GitHubClientProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeveloperSyncService {

    private final GitHubClientProvider clientProvider;
    private final DeveloperService developerService;
    private final String developersUrl;
    private final String developersOrg;
    private final String developersTeam;

    @Autowired
    public DeveloperSyncService(GitHubClientProvider clientProvider, DeveloperService developerService,
                                @Value("${developers.github.url}") String developersUrl,
                                @Value("${developers.github.org}") String developersOrg,
                                @Value("${developers.github.team}") String developersTeam) {
        this.clientProvider = clientProvider;
        this.developerService = developerService;
        this.developersUrl = developersUrl;
        this.developersOrg = developersOrg;
        this.developersTeam = developersTeam;
    }

    @Scheduled(initialDelay = 0, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void fetchAndUpdateDevelopers() {
        var client = clientProvider.getClientForUrl(developersUrl);
        var developers = client.getDevelopers(developersOrg, developersTeam);
        log.info("Fetched {} developers: {}", developers.size(), getLogins(developers));
        updateDevelopers(developers);
    }

    private List<String> getLogins(List<Developer> developers) {
        return developers.stream()
                .map(Developer::getLogin)
                .toList();
    }

    private void updateDevelopers(List<Developer> developers) {
        for (Developer developer : developers) {
            boolean developerExists = developerService.existsDeveloper(developer.getLogin());
            if (!developerExists) {
                developerService.createDeveloper(developer);
            }
        }
    }

}
