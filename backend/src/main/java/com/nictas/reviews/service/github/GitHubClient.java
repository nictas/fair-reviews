package com.nictas.reviews.service.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.github.GHMyself;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitHubClient {

    static final String ORGANIZATION_ROLE_ADMIN = "admin";

    private final GitHub delegate;

    public GitHubClient(GitHub delegate) {
        this.delegate = delegate;
    }

    GitHub getDelegate() {
        return delegate;
    }

    public PullRequestFileDetails getPullRequestInfo(PullRequest pullRequest) {
        log.info("Getting info for PR: {}/{}/{}", pullRequest.getOwner(), pullRequest.getRepository(),
                pullRequest.getNumber());
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

            int additions = ghPullRequest.getAdditions();
            int deletions = ghPullRequest.getDeletions();
            return new PullRequestFileDetails(additions, deletions, changedFiles);
        } catch (IOException e) {
            throw new GitHubClientException(String.format("Error while fetching info for PR %s/%s/%d: %s",
                    pullRequest.getOwner(), pullRequest.getRepository(), pullRequest.getNumber(), e.getMessage()), e);
        }
    }

    public List<Developer> getDevelopers(String organizationName, String teamName) {
        log.info("Getting developers for organization {} and team {}", organizationName, teamName);
        try {
            GHOrganization organization = delegate.getOrganization(organizationName);
            GHTeam team = organization.getTeamByName(teamName);

            if (team == null) {
                throw new GitHubClientException(
                        String.format("Unable to find team %s in organization: %s", teamName, organizationName));
            }

            return team.listMembers()
                    .toList()
                    .stream()
                    .map(this::toDeveloper)
                    .toList();
        } catch (IOException e) {
            throw new GitHubClientException(
                    String.format("Error while fetching GitHub users from organization %s and team %s: %s",
                            organizationName, teamName, e.getMessage()),
                    e);
        }
    }

    public List<Developer> getOrganizationAdmins(String organizationName) {
        try {
            GHOrganization organization = delegate.getOrganization(organizationName);
            return organization.listMembersWithRole(ORGANIZATION_ROLE_ADMIN)
                    .toList()
                    .stream()
                    .map(this::toDeveloper)
                    .toList();
        } catch (IOException e) {
            throw new GitHubClientException(String.format("Error while fetching GitHub users from organization %s: %s",
                    organizationName, e.getMessage()), e);
        }
    }

    public Developer getMyself() {
        try {
            GHMyself myself = delegate.getMyself();
            return toDeveloper(myself);
        } catch (IOException e) {
            throw new GitHubClientException(String.format("Error while fetching GitHub user: %s", e.getMessage()), e);
        }
    }

    private Developer toDeveloper(GHUser user) {
        try {
            return new Developer(user.getLogin(), user.getEmail());
        } catch (IOException e) {
            throw new GitHubClientException(
                    String.format("Error while fetching email of user %s: %s", user.getLogin(), e.getMessage()), e);
        }
    }

}
