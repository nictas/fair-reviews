package com.nictas.reviews.domain;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
public class Multiplier {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private final double defaultAdditionsMultiplier;
    private final double defaultDeletionsMultiplier;
    private final List<FileMultiplier> fileMultipliers;
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Data
    @With
    @Builder
    @Jacksonized
    @AllArgsConstructor
    public static class FileMultiplier {

        @Builder.Default
        private UUID id = UUID.randomUUID();
        private final String fileExtension;
        private final double additionsMultiplier;
        private final double deletionsMultiplier;

    }

}
