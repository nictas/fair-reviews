package com.nictas.reviews.domain;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class Multiplier {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    private double defaultAdditionsMultiplier;
    private double defaultDeletionsMultiplier;
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMultiplier> fileMultipliers = Collections.emptyList();
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Multiplier() {
        // Required by JPA.
    }

}
