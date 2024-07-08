package com.nictas.reviews.repository;

import java.util.List;
import java.util.Optional;

import com.nictas.reviews.domain.Developer;

public interface CustomDeveloperRepository {

    Optional<Developer> findWithLowestScore(List<String> loginExclusionList);

}
