package com.nictas.reviews.domain;

import java.util.UUID;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class PullRequestReview {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    @ManyToOne
    private final Developer developer;
    private final double score;
    @ManyToOne
    private final Multiplier multiplier;
    private final String pullRequestUrl;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private final PullRequestFileDetails pullRequestFileDetails;

}
