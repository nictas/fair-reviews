package com.nictas.reviews.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nictas.reviews.domain.Multiplier;

@Repository
public interface MultiplierRepository extends JpaRepository<Multiplier, UUID>, CustomMultiplierRepository {

}
