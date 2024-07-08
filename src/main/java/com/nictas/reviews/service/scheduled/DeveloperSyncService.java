package com.nictas.reviews.service.scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.repository.DeveloperRepository;
import com.nictas.reviews.service.github.GitHubClientProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeveloperSyncService {

    private final GitHubClientProvider clientProvider;
    private final DeveloperRepository developerRepository;
    private final String developersUrl;
    private final String developersOrg;
    private final String developersTeam;

    @Autowired
    public DeveloperSyncService(GitHubClientProvider clientProvider, DeveloperRepository developerRepository,
                                @Value("${developers.github.url}") String developersUrl,
                                @Value("${developers.github.org}") String developersOrg,
                                @Value("${developers.github.team}") String developersTeam) {
        this.clientProvider = clientProvider;
        this.developerRepository = developerRepository;
        this.developersUrl = developersUrl;
        this.developersOrg = developersOrg;
        this.developersTeam = developersTeam;
    }

    @Scheduled(initialDelay = 0, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void fetchAndUpdateDevelopers() {
        log.info("Synchronizing developers with GitHub {} organization {} and team {}", developersUrl, developersOrg,
                developersTeam);
        var client = clientProvider.getClientForUrl(developersUrl);
        var developers = client.getDevelopers(developersOrg, developersTeam);
        log.info("Fetched {} developers from GitHub: {}", developers.size(), getLogins(developers));
        updateDevelopers(developers);
    }

    private List<String> getLogins(List<Developer> developers) {
        return developers.stream()
                .map(Developer::getLogin)
                .toList();
    }

    private void updateDevelopers(List<Developer> developers) {
        List<Developer> existingDevelopers = new ArrayList<>();
        List<Developer> nonExistingDevelopers = new ArrayList<>();
        for (Developer developer : developers) {
            var existingDeveloper = developerRepository.findById(developer.getLogin());
            if (existingDeveloper.isPresent()) {
                existingDevelopers.add(existingDeveloper.get());
            } else {
                nonExistingDevelopers.add(developer);
            }
        }
        log.info("Existing developers: {}", existingDevelopers);
        log.info("Non-existing developers: {}", nonExistingDevelopers);
        if (!nonExistingDevelopers.isEmpty()) {
            createNonExistingDevelopers(nonExistingDevelopers, existingDevelopers);
        }
    }

    private void createNonExistingDevelopers(List<Developer> nonExistingDevelopers,
                                             List<Developer> existingDevelopers) {
        double startingScore = computeStartingScore(existingDevelopers);
        log.info("Starting score for non-existing developers: {}", startingScore);
        nonExistingDevelopers.stream()
                .map(developer -> developer.withScore(startingScore))
                .forEach(developer -> {
                    log.info("Creating developer: {}", developer);
                    developerRepository.save(developer);
                });
    }

    private double computeStartingScore(List<Developer> existingDevelopers) {
        return existingDevelopers.stream()
                .collect(Collectors.averagingDouble(Developer::getScore));
    }

}
