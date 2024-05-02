package com.nictas.reviews.domain;

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
public class Developer {

    private final String login;
    private final String email;
    private final double score;

    public Developer(String login, String email) {
        this(login, email, 0.);
    }

}
