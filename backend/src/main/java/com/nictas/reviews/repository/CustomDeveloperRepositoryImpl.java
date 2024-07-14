package com.nictas.reviews.repository;

import java.util.List;
import java.util.Optional;

import com.nictas.reviews.domain.Developer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class CustomDeveloperRepositoryImpl implements CustomDeveloperRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Developer> findWithLowestScore(List<String> loginExclusionList) {
        @SuppressWarnings("unchecked")
        List<Developer> developers = entityManager
                .createQuery("SELECT d FROM Developer d WHERE d.login NOT IN :loginExclusionList ORDER BY d.score ASC")
                .setParameter("loginExclusionList", loginExclusionList)
                .setMaxResults(1)
                .getResultList();
        if (developers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(developers.get(0));
    }

}
