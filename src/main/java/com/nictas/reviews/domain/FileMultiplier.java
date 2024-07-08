package com.nictas.reviews.domain;

import java.util.UUID;

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
public class FileMultiplier {

    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String fileExtension;
    private double additionsMultiplier;
    private double deletionsMultiplier;

}
