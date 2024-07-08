package com.nictas.reviews.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Data
@With
@Builder
@Jacksonized
@RequiredArgsConstructor
@Entity
public class Developer {

    @Id
    private final String login;
    private final String email;
    private final double score;

    public Developer(String login, String email) {
        this(login, email, 0.);
    }

}
