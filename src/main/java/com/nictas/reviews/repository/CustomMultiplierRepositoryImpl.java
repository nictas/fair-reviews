package com.nictas.reviews.repository;

import java.util.List;
import java.util.Optional;

import com.nictas.reviews.domain.Multiplier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class CustomMultiplierRepositoryImpl implements CustomMultiplierRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Multiplier> findLatest() {
        @SuppressWarnings("unchecked")
        List<Multiplier> multipliers = entityManager
                .createQuery("SELECT m FROM Multiplier m ORDER BY m.createdAt DESC")
                .setMaxResults(1)
                .getResultList();
        if (multipliers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(multipliers.get(0));
    }

}
