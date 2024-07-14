package com.nictas.reviews.controller.rest.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class PullRequestAssignRequestTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final PullRequestAssignRequest REQUEST = new PullRequestAssignRequest(
            "https://github.com/foo/bar/pull/123", List.of("foo", "bar"), List.of("baz"));
    private static final String REQUEST_JSON = "{\"pullRequestUrl\":\"https://github.com/foo/bar/pull/123\",\"assigneeList\":[\"foo\",\"bar\"],\"assigneeExclusionList\":[\"baz\"]}";

    @Test
    void testSerialization() throws JsonProcessingException {
        String actualJson = OBJECT_MAPPER.writeValueAsString(REQUEST);
        assertEquals(REQUEST_JSON, actualJson);
    }

    @Test
    void testDeserialization() throws JsonProcessingException {
        PullRequestAssignRequest actualRequest = OBJECT_MAPPER.readValue(REQUEST_JSON, PullRequestAssignRequest.class);
        assertEquals(REQUEST, actualRequest);
    }

}
