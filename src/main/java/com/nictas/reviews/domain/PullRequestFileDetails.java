package com.nictas.reviews.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@RequiredArgsConstructor
public class PullRequestFileDetails {

    private final List<ChangedFile> changedFiles;

    @Data
    @Builder
    @Jacksonized
    public static class ChangedFile {

        private final String name;
        private final int additions;
        private final int deletions;

    }

}
