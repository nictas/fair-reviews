package com.nictas.reviews.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.MultiplierRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MultiplierService {

    static final Multiplier DEFAULT_MULTIPLIER = Multiplier.builder()
            .id(UUID.fromString("a49eb29b-d727-4493-a27b-17b2a8d15104"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .build();

    private final MultiplierRepository repository;

    @Autowired
    public MultiplierService(MultiplierRepository repository) {
        this.repository = repository;
    }

    public Page<Multiplier> getAllMultipliers(Pageable pageable) {
        log.info("Getting all multipliers");
        return repository.getAll(pageable);
    }

    public Multiplier getMultiplier(UUID id) {
        log.info("Getting multiplier {}", id);
        return repository.get(id)
                .orElseThrow(() -> new NotFoundException("Could not find multiplier with ID: " + id));
    }

    public Multiplier getLatestMultiplier() {
        log.info("Getting latest multiplier");
        Optional<Multiplier> multiplier = repository.getLatest();
        if (multiplier.isPresent()) {
            return multiplier.get();
        }
        createMultiplier(DEFAULT_MULTIPLIER);
        return DEFAULT_MULTIPLIER;
    }

    public void createMultiplier(Multiplier multiplier) {
        log.info("Creating multiplier: {}", multiplier);
        repository.create(multiplier);
    }

    public void updateMultiplier(Multiplier multiplier) {
        log.info("Updating multiplier: {}", multiplier);
        repository.update(multiplier);
    }

    public void deleteMultiplier(UUID id) {
        log.info("Deleting multiplier {}", id);
        int deleted = repository.delete(id);
        if (deleted < 1) {
            throw new NotFoundException("Could not find multiplier with ID: " + id);
        }
    }

}
