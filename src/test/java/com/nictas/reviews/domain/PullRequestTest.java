package com.nictas.reviews.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PullRequestTest {

    @Test
    void testFromUrl() {
        String pullRequestUrl = "https://github.com/foo/bar/pull/123";
        PullRequest pullRequest = PullRequest.fromUrl(pullRequestUrl);
        assertEquals("foo", pullRequest.getOwner());
        assertEquals("bar", pullRequest.getRepository());
        assertEquals(123, pullRequest.getNumber());
    }

    @Test
    void testFromUrlWithInvalidUrl() {
        String invalidUrl = "https://invalid-url.com";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PullRequest.fromUrl(invalidUrl));
        assertEquals("Invalid pull request URL: " + invalidUrl, exception.getMessage());
    }

}
