package com.nictas.reviews.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.util.PaginationUtils;

@Component
public class InMemoryMultiplierRepository implements MultiplierRepository {

    private final List<Multiplier> multipliers = new ArrayList<>();

    @Override
    public Page<Multiplier> getAll(Pageable pageable) {
        return PaginationUtils.applyPagination(multipliers, pageable);
    }

    @Override
    public Optional<Multiplier> get(UUID id) {
        return multipliers.stream()
                .filter(multiplier -> id.equals(multiplier.getId()))
                .findFirst();
    }

    @Override
    public Optional<Multiplier> getLatest() {
        return multipliers.stream()
                .sorted(Comparator.comparing(Multiplier::getCreatedAt)
                        .reversed())
                .findFirst();
    }

    @Override
    public void create(Multiplier multiplier) {
        multipliers.add(multiplier);
    }

    @Override
    public void update(Multiplier multiplier) {
        UUID id = multiplier.getId();
        multipliers.removeIf(candidate -> id.equals(candidate.getId()));
        multipliers.add(multiplier);
    }

    @Override
    public int delete(UUID id) {
        var deleted = multipliers.removeIf(candidate -> id.equals(candidate.getId()));
        return deleted ? 1 : 0;
    }

}
