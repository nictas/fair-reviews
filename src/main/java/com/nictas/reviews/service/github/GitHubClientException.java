package com.nictas.reviews.service.github;

public class GitHubClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GitHubClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubClientException(String message) {
        super(message);
    }

}
