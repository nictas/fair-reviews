package com.nictas.reviews.repository;

import java.util.Optional;

import com.nictas.reviews.domain.Multiplier;

public interface CustomMultiplierRepository {

    Optional<Multiplier> findLatest();

}
