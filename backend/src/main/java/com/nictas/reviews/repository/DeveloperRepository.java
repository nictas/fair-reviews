package com.nictas.reviews.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nictas.reviews.domain.Developer;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, String>, CustomDeveloperRepository {

}
