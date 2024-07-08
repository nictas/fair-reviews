package com.nictas.reviews.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Multiplier;

public interface MultiplierRepository {

    Page<Multiplier> getAll(Pageable pageable);

    Optional<Multiplier> get(UUID id);

    Optional<Multiplier> getLatest();

    Multiplier create(Multiplier multiplier);

    Multiplier update(Multiplier multiplier);

    int delete(UUID id);

}
