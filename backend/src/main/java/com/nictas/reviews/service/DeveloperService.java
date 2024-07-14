package com.nictas.reviews.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.DeveloperRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeveloperService {

    private final DeveloperRepository repository;

    @Autowired
    public DeveloperService(DeveloperRepository repository) {
        this.repository = repository;
    }

    public Page<Developer> getAllDevelopers(Pageable pageable) {
        log.info("Getting all developers");
        return repository.findAll(pageable);
    }

    public Developer getDeveloper(String login) {
        log.info("Getting developer {}", login);
        return repository.findById(login)
                .orElseThrow(() -> new NotFoundException("Could not find developer with login: " + login));
    }

    public Developer getDeveloperWithLowestScore(List<String> loginExclusionList) {
        log.info("Getting developer with lowest score with exclusion list: {}", loginExclusionList);
        return repository.findWithLowestScore(loginExclusionList)
                .orElseThrow(() -> new NotFoundException("Could not find developer with lowest score"));
    }

    public void saveDeveloper(Developer developer) {
        log.info("Saving developer: {}", developer);
        repository.save(developer);
    }

    public void deleteDeveloper(String login) {
        log.info("Deleting developer {}", login);
        if (repository.findById(login)
                .isEmpty()) {
            throw new NotFoundException("Could not find developer with login: " + login);
        }
        repository.deleteById(login);
    }

}
