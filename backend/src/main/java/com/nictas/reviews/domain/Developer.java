package com.nictas.reviews.domain;

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
public class Developer {

    @Id
    private String login;
    private String email;
    private double score;

    protected Developer() {
        // Required by JPA.
    }

    public Developer(String login, String email) {
        this(login, email, 0.);
    }

}
