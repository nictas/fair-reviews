package com.nictas.reviews.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.DeveloperRepository;

@Service
public class DeveloperService {

    private final DeveloperRepository repository;

    @Autowired
    public DeveloperService(DeveloperRepository repository) {
        this.repository = repository;
    }

    public Page<Developer> getAllDevelopers(Pageable pageable) {
        return repository.getAll(pageable);
    }

    public Developer getDeveloper(String login) {
        return repository.get(login)
                .orElseThrow(() -> new NotFoundException("Could not find developer with login: " + login));
    }

    public Developer getDeveloperWithLowestScore(List<String> assigneeExclusionList) {
        return repository.getWithLowestScore(assigneeExclusionList)
                .orElseThrow(() -> new NotFoundException("Could not find developer with lowest score"));
    }

    public void createDeveloper(Developer developer) {
        repository.create(developer);
    }

    public void updateDeveloper(Developer developer) {
        repository.update(developer);
    }

    public void deleteDeveloper(String login) {
        int deleted = repository.delete(login);
        if (deleted < 1) {
            throw new NotFoundException("Could not find developer with login: " + login);
        }
    }

}
