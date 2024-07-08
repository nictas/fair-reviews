package com.nictas.reviews.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.util.PaginationUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InMemoryDeveloperRepository implements DeveloperRepository {

    private final List<Developer> developers = new ArrayList<>();
    private final PullRequestReviewRepository pullRequestReviewRepository;

    @Autowired
    public InMemoryDeveloperRepository(PullRequestReviewRepository pullRequestReviewRepository) {
        this.pullRequestReviewRepository = pullRequestReviewRepository;
    }

    @Override
    public Page<Developer> getAll(Pageable pageable) {
        return PaginationUtils.applyPagination(developers, pageable);
    }

    @Override
    public Optional<Developer> get(String login) {
        return developers.stream()
                .filter(developer -> login.equals(developer.getLogin()))
                .findFirst();
    }

    @Override
    public Optional<Developer> getWithLowestScore(List<String> loginExclusionList) {
        return developers.stream()
                .filter(developer -> !loginExclusionList.contains(developer.getLogin()))
                .min((dev1, dev2) -> Double.compare(dev1.getScore(), dev2.getScore()));
    }

    @Override
    public void create(Developer developer) {
        developers.add(developer);
    }

    @Override
    public void update(Developer developer) {
        String login = developer.getLogin();
        developers.removeIf(candidate -> login.equals(candidate.getLogin()));
        developers.add(developer);
    }

    @Override
    public int delete(String login) {
        var deleted = developers.removeIf(candidate -> login.equals(candidate.getLogin()));
        if (deleted) {
            deleteAssociatedReviews(login);
            return 1;
        }
        return 0;
    }

    private void deleteAssociatedReviews(String login) {
        Page<PullRequestReview> reviews = pullRequestReviewRepository.getByDeveloperLogin(login, Pageable.unpaged());
        log.info("Deleting reviews for developer {}: {}", login, getIds(reviews));
        reviews.stream()
                .map(PullRequestReview::getId)
                .forEach(pullRequestReviewRepository::delete);
    }

    private List<UUID> getIds(Page<PullRequestReview> reviews) {
        return reviews.map(PullRequestReview::getId)
                .toList();
    }

}
