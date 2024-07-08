package com.nictas.reviews.domain;

import java.util.UUID;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class PullRequestReview {

    @Builder.Default
    @Id
    private UUID id = UUID.randomUUID();
    @ManyToOne
    private Developer developer;
    private double score;
    @ManyToOne
    private Multiplier multiplier;
    private String pullRequestUrl;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private PullRequestFileDetails pullRequestFileDetails;

    protected PullRequestReview() {
        // Required by JPA.
    }

}
