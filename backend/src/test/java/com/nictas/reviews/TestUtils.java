package com.nictas.reviews;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TestUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestUtils() {
        throw new UnsupportedOperationException();
    }

    public static String getResourceAsString(Class<?> classOfTest, String resource) {
        InputStream resourceInputStream = classOfTest.getResourceAsStream(resource);
        try {
            return new String(resourceInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource input stream: " + e.getMessage(), e);
        }
    }

    public static void assertJsonsMatch(String expectedJson, String json) {
        try {
            JsonNode expectedTree = OBJECT_MAPPER.readTree(expectedJson);
            JsonNode tree = OBJECT_MAPPER.readTree(json);
            assertEquals(expectedTree, tree);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON: " + e.getMessage(), e);
        }
    }

}
