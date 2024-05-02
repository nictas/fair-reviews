package com.nictas.reviews.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;

public interface DeveloperRepository {

    Page<Developer> getAll(Pageable pageable);

    Optional<Developer> get(String login);

    Optional<Developer> getWithLowestScore(List<String> loginExclusionList);

    void create(Developer developer);

    void update(Developer developer);

    int delete(String login);

}
