package com.nictas.reviews.service.scheduled;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.github.GitHubClientProvider;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrganizationAdminsSyncService {

    private final GitHubClientProvider clientProvider;
    private final String developersUrl;
    private final String developersOrg;

    private AtomicReference<List<Developer>> organizationAdmins = new AtomicReference<>(Collections.emptyList());

    @Autowired
    public OrganizationAdminsSyncService(GitHubClientProvider clientProvider,
                                         @Value("${developers.github.url}") String developersUrl,
                                         @Value("${developers.github.org}") String developersOrg) {
        this.clientProvider = clientProvider;
        this.developersUrl = developersUrl;
        this.developersOrg = developersOrg;
    }

    public List<Developer> getOrganizationAdmins() {
        return organizationAdmins.get();
    }

    @Scheduled(initialDelay = 0, fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void fetchAndUpdateOrganizationAdmins() {
        log.info("Synchronizing organization admins with GitHub {} organization {}", developersUrl, developersOrg);
        var client = clientProvider.getClientForUrl(developersUrl);
        var developers = client.getOrganizationAdmins(developersOrg);
        log.info("Fetched {} organization admins from GitHub: {}", developers.size(), getLogins(developers));
        organizationAdmins.set(developers);
    }

    private List<String> getLogins(List<Developer> developers) {
        return developers.stream()
                .map(Developer::getLogin)
                .toList();
    }

}
