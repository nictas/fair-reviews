package com.nictas.reviews.service.scheduled;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.repository.DeveloperRepository;
import com.nictas.reviews.repository.PullRequestReviewRepository;
import com.nictas.reviews.service.github.GitHubClientProvider;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeveloperSyncService {

    static final Developer DUMMY_DEVELOPER = new Developer("dummy", "dummy@test.com");

    private final GitHubClientProvider clientProvider;
    private final DeveloperRepository developerRepository;
    private final PullRequestReviewRepository pullRequestReviewRepository;
    private final String developersUrl;
    private final String developersOrg;
    private final String developersTeam;

    @Autowired
    public DeveloperSyncService(GitHubClientProvider clientProvider, DeveloperRepository developerRepository,
                                PullRequestReviewRepository pullRequestReviewRepository,
                                @Value("${developers.github.url}") String developersUrl,
                                @Value("${developers.github.org}") String developersOrg,
                                @Value("${developers.github.team}") String developersTeam) {
        this.clientProvider = clientProvider;
        this.developerRepository = developerRepository;
        this.pullRequestReviewRepository = pullRequestReviewRepository;
        this.developersUrl = developersUrl;
        this.developersOrg = developersOrg;
        this.developersTeam = developersTeam;
    }

    @Scheduled(initialDelay = 0, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
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
        List<Developer> newDevelopers = new ArrayList<>();
        for (Developer developer : developers) {
            var existingDeveloper = developerRepository.findById(developer.getLogin());
            if (existingDeveloper.isPresent()) {
                existingDevelopers.add(existingDeveloper.get());
            } else {
                newDevelopers.add(developer);
            }
        }
        log.info("Existing developers: {}", existingDevelopers);
        log.info("New developers: {}", newDevelopers);
        if (!newDevelopers.isEmpty()) {
            createNewDevelopers(newDevelopers);
        }
    }

    private void createNewDevelopers(List<Developer> developers) {
        Developer startingPoint = developerRepository.findWithLowestScore(Collections.emptyList())
                .orElse(DUMMY_DEVELOPER);
        log.info("Replicating history of developer {} as a starting point for new developers", startingPoint);
        for (Developer developer : developers) {
            createNewDeveloper(developer, startingPoint);
            createNewDeveloperHistory(developer, startingPoint);
        }
    }

    private void createNewDeveloper(Developer developer, Developer startingPoint) {
        Developer developerWithStartingScore = developer.withScore(startingPoint.getScore());
        log.info("Creating developer: {}", developerWithStartingScore);
        developerRepository.save(developerWithStartingScore);
    }

    private void createNewDeveloperHistory(Developer developer, Developer startingPoint) {
        pullRequestReviewRepository.findByDeveloperLogin(startingPoint.getLogin(), Pageable.unpaged())
                .map(review -> review.withId(UUID.randomUUID())
                        .withCreatedAt(OffsetDateTime.now())
                        .withDeveloper(developer))
                .forEach(review -> {
                    log.info("Creating PR review: {}", review);
                    pullRequestReviewRepository.save(review);
                });
    }

}
