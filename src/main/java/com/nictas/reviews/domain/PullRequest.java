package com.nictas.reviews.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class PullRequest {

    private static final String PR_REGEX = "https?://.*?/(?<owner>[\\w-]+)/(?<repository>[\\w-]+)/pull/(?<number>\\d+)";

    private final String owner;
    private final String repository;
    private final int number;

    public static PullRequest fromUrl(String pullRequestUrl) {
        Pattern pattern = Pattern.compile(PR_REGEX);
        Matcher matcher = pattern.matcher(pullRequestUrl);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid pull request URL: " + pullRequestUrl);
        }
        String owner = matcher.group("owner");
        String repository = matcher.group("repository");
        int number = Integer.parseInt(matcher.group("number"));
        return new PullRequest(owner, repository, number);
    }

}
