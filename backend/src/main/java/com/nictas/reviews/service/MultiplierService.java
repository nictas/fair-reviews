package com.nictas.reviews.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.MultiplierRepository;
import com.nictas.reviews.repository.PullRequestReviewRepository;

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
    private final PullRequestReviewRepository pullRequestReviewRepository;

    @Autowired
    public MultiplierService(MultiplierRepository repository, PullRequestReviewRepository pullRequestReviewRepository) {
        this.repository = repository;
        this.pullRequestReviewRepository = pullRequestReviewRepository;
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

    @Transactional
    public Multiplier getLatestMultiplier() {
        log.info("Getting latest multiplier");
        Optional<Multiplier> multiplier = repository.findLatest();
        if (multiplier.isEmpty()) {
            return saveMultiplier(DEFAULT_MULTIPLIER);
        }
        return multiplier.get();
    }

    public Multiplier saveMultiplier(Multiplier multiplier) {
        log.info("Saving multiplier: {}", multiplier);
        return repository.save(multiplier);
    }

    @Transactional
    public void deleteMultiplier(UUID id) {
        log.info("Deleting multiplier {}", id);
        if (repository.findById(id)
                .isEmpty()) {
            throw new NotFoundException("Could not find multiplier with ID: " + id);
        }
        Page<PullRequestReview> reviews = pullRequestReviewRepository.findByMultiplierId(id, Pageable.unpaged());
        if (!reviews.isEmpty()) {
            List<UUID> reviewIds = reviews.map(PullRequestReview::getId)
                    .toList();
            throw new IllegalArgumentException(
                    String.format("Multiplier %s is still referenced in %d reviews", id, reviewIds.size()));
        }
        repository.deleteById(id);
    }

}
