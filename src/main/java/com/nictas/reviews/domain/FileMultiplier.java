package com.nictas.reviews.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@With
@Builder
@Jacksonized
@AllArgsConstructor
@Entity
public class FileMultiplier {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    private final String fileExtension;
    private final double additionsMultiplier;
    private final double deletionsMultiplier;

}
