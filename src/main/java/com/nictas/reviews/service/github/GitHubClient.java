package com.nictas.reviews.service.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequest;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;

public class GitHubClient {

    private final GitHub delegate;

    public GitHubClient(GitHub delegate) {
        this.delegate = delegate;
    }

    GitHub getDelegate() {
        return delegate;
    }

    public PullRequestFileDetails getPullRequestInfo(PullRequest pullRequest) {
        try {
            GHRepository repository = delegate
                    .getRepository(pullRequest.getOwner() + "/" + pullRequest.getRepository());
            GHPullRequest ghPullRequest = repository.getPullRequest(pullRequest.getNumber());

            List<PullRequestFileDetails.ChangedFile> changedFiles = new ArrayList<>();
            for (GHPullRequestFileDetail file : ghPullRequest.listFiles()
                    .toList()) {
                var changedFile = ChangedFile.builder()
                        .name(file.getFilename())
                        .additions(file.getAdditions())
                        .deletions(file.getDeletions())
                        .build();
                changedFiles.add(changedFile);
            }

            return new PullRequestFileDetails(changedFiles);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Error while fetching info for PR %s/%s/%d: %s",
                    pullRequest.getOwner(), pullRequest.getRepository(), pullRequest.getNumber(), e.getMessage()), e);
        }
    }

    public List<Developer> getDevelopers(String organizationName, String teamName) {
        try {
            GHOrganization organization = delegate.getOrganization(organizationName);
            GHTeam team = organization.getTeamByName(teamName);

            if (team == null) {
                throw new IllegalStateException(
                        String.format("Unable to find team %s in organization: %s", teamName, organizationName));
            }

            return team.listMembers()
                    .toList()
                    .stream()
                    .map(this::toDeveloper)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Error while fetching GitHub users from organization %s and team %s: %s",
                            organizationName, teamName, e.getMessage()),
                    e);
        }
    }

    private Developer toDeveloper(GHUser user) {
        try {
            return new Developer(user.getLogin(), user.getEmail());
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Error while fetching email of user %s: %s", user.getLogin(), e.getMessage()), e);
        }
    }

}
