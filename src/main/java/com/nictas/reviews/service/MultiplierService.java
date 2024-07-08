package com.nictas.reviews.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.MultiplierRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MultiplierService {

    public static final Multiplier DEFAULT_MULTIPLIER = Multiplier.builder()
            .id(UUID.fromString("a49eb29b-d727-4493-a27b-17b2a8d15104"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .build();

    private final MultiplierRepository repository;

    @Autowired
    public MultiplierService(MultiplierRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    @Transactional
    public void saveDefaultMultiplier() {
        List<Multiplier> multipliers = repository.findAll();
        if (multipliers.isEmpty()) {
            saveMultiplier(DEFAULT_MULTIPLIER);
        }
    }

    public Page<Multiplier> getAllMultipliers(Pageable pageable) {
        log.info("Getting all multipliers");
        return repository.findAll(pageable);
    }

    public Multiplier getMultiplier(UUID id) {
        log.info("Getting multiplier {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Could not find multiplier with ID: " + id));
    }

    public Multiplier getLatestMultiplier() {
        log.info("Getting latest multiplier");
        return repository.findLatest()
                .orElseThrow(() -> new NotFoundException("Could not find latest multiplier"));
    }

    public Multiplier saveMultiplier(Multiplier multiplier) {
        log.info("Saving multiplier: {}", multiplier);
        return repository.save(multiplier);
    }

    public void deleteMultiplier(UUID id) {
        log.info("Deleting multiplier {}", id);
        if (repository.findById(id)
                .isEmpty()) {
            throw new NotFoundException("Could not find multiplier with ID: " + id);
        }
        repository.deleteById(id);
    }

}
