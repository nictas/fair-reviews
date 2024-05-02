package com.nictas.reviews.controller.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nictas.reviews.domain.Developer;

class PullRequestAssignResponseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Developer ASSIGNEE_FOO = Developer.builder()
            .login("foo")
            .email("foo@example.com")
            .score(87.8)
            .build();
    private static final Developer ASSIGNEE_BAR = Developer.builder()
            .login("bar")
            .email("bar@example.com")
            .score(7.)
            .build();
    private static final PullRequestAssignResponse RESPONSE = new PullRequestAssignResponse(
            List.of(ASSIGNEE_FOO, ASSIGNEE_BAR));
    private static final String RESPONSE_JSON = "{\"assignees\":[{\"login\":\"foo\",\"email\":\"foo@example.com\",\"score\":87.8},{\"login\":\"bar\",\"email\":\"bar@example.com\",\"score\":7.0}]}";

    @Test
    void testSerialization() throws JsonProcessingException {
        String actualJson = OBJECT_MAPPER.writeValueAsString(RESPONSE);
        assertEquals(RESPONSE_JSON, actualJson);
    }

    @Test
    void testDeserialization() throws JsonProcessingException {
        PullRequestAssignResponse actualResponse = OBJECT_MAPPER.readValue(RESPONSE_JSON,
                PullRequestAssignResponse.class);
        assertEquals(RESPONSE, actualResponse);
    }

}
