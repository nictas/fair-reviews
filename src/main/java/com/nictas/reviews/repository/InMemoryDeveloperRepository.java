package com.nictas.reviews.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.util.PaginationUtils;

@Component
public class InMemoryDeveloperRepository implements DeveloperRepository {

    private final List<Developer> developers = new ArrayList<>();

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
        delete(developer.getLogin());
        create(developer);
    }

    @Override
    public int delete(String login) {
        var deleted = developers.removeIf(candidate -> login.equals(candidate.getLogin()));
        return deleted ? 1 : 0;
    }

}
