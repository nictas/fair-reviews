package com.nictas.reviews.controller.rest;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nictas.reviews.configuration.UserRoles;
import com.nictas.reviews.controller.rest.dto.PullRequestReviewWithoutDeveloper;
import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.PullRequestReviewService;
import com.nictas.reviews.service.scheduled.DeveloperSyncService;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    private final DeveloperService developerService;
    private final DeveloperSyncService developerSyncService;
    private final PullRequestReviewService pullRequestReviewService;
    private final TaskScheduler taskScheduler;

    @Autowired
    public DeveloperController(DeveloperService developerService, DeveloperSyncService developerSyncService,
                               PullRequestReviewService pullRequestReviewService, TaskScheduler taskScheduler) {
        this.developerService = developerService;
        this.developerSyncService = developerSyncService;
        this.pullRequestReviewService = pullRequestReviewService;
        this.taskScheduler = taskScheduler;
    }

    @GetMapping
    public Page<Developer> getAllDevelopers(Pageable pageable) {
        return developerService.getAllDevelopers(pageable);
    }

    @GetMapping("/{login}")
    public Developer getDeveloper(@PathVariable("login") String login) {
        return developerService.getDeveloper(login);
    }

    @GetMapping("/{login}/history")
    public Page<PullRequestReviewWithoutDeveloper> getDeveloperHistory(@PathVariable("login") String login,
                                                                       Pageable pageable) {
        return pullRequestReviewService.getReviewsByDeveloperLogin(login, pageable)
                .map(PullRequestReviewWithoutDeveloper::from);
    }

    @DeleteMapping("/{login}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured(UserRoles.ROLE_ADMIN)
    public void deleteDeveloper(@PathVariable("login") String login) {
        developerService.deleteDeveloper(login);
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Secured(UserRoles.ROLE_ADMIN)
    public void syncDevelopers() {
        taskScheduler.schedule(developerSyncService::fetchAndUpdateDevelopers, Instant.now());
    }

}
