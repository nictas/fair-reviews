package com.nictas.reviews.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@With
@Setter(AccessLevel.NONE)
@Builder
@Jacksonized
@AllArgsConstructor
@Entity
public class FileMultiplier {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    private String fileExtension;
    private double additionsMultiplier;
    private double deletionsMultiplier;

    protected FileMultiplier() {
        // Required by JPA.
    }

}
