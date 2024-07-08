package com.nictas.reviews.domain;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
public class Multiplier {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    private final double defaultAdditionsMultiplier;
    private final double defaultDeletionsMultiplier;
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMultiplier> fileMultipliers = Collections.emptyList();
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

}
