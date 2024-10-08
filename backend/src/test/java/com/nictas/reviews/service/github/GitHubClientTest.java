package com.nictas.reviews.service.github;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequest;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.service.github.settings.GitHubSettingsProvider;

@ExtendWith(MockitoExtension.class)
class GitHubClientTest {

    @Nested
    class GetDeveloperEmailsTests {

        private static final String ORGANIZATION_NAME = "baz";
        private static final String TEAM_NAME = "qux";
        private static final String USER_FOO_LOGIN = "foo";
        private static final String USER_BAR_LOGIN = "bar";
        private static final String USER_FOO_EMAIL = "foo@example.com";
        private static final String USER_BAR_EMAIL = "bar@example.com";

        @Mock
        private GitHub delegate;

        @Mock
        private GHOrganization organization;

        @Mock
        private GHTeam team;

        @Mock
        private PagedIterable<GHUser> userIterable;

        @Mock
        private GHUser userFoo;

        @Mock
        private GHUser userBar;

        @InjectMocks
        private GitHubClient client;

        @Test
        void testGetDevelopers() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenReturn(organization);
            when(organization.getTeamByName(TEAM_NAME)).thenReturn(team);
            when(team.listMembers()).thenReturn(userIterable);
            when(userIterable.toList()).thenReturn(List.of(userFoo, userBar));

            when(userFoo.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(userFoo.getEmail()).thenReturn(USER_FOO_EMAIL);

            when(userBar.getLogin()).thenReturn(USER_BAR_LOGIN);
            when(userBar.getEmail()).thenReturn(USER_BAR_EMAIL);

            List<Developer> developers = client.getDevelopers(ORGANIZATION_NAME, TEAM_NAME);
            List<Developer> expectedDevelopers = List.of(new Developer(USER_FOO_LOGIN, USER_FOO_EMAIL),
                    new Developer(USER_BAR_LOGIN, USER_BAR_EMAIL));

            assertEquals(expectedDevelopers, developers);
        }

        @Test
        void testGetDevelopersWithNonExistingTeam() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenReturn(organization);

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> client.getDevelopers(ORGANIZATION_NAME, TEAM_NAME));
            assertEquals(String.format("Unable to find team %s in organization: %s", TEAM_NAME, ORGANIZATION_NAME),
                    exception.getMessage());
        }

        @Test
        void testGetDevelopersWithConnectionError() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> client.getDevelopers(ORGANIZATION_NAME, TEAM_NAME));
            assertEquals(String.format(
                    "Error while fetching GitHub users from organization %s and team %s: Connection error",
                    ORGANIZATION_NAME, TEAM_NAME), exception.getMessage());
        }

        @Test
        void testGetDevelopersWithConnectionErrorWhenFetchingEmails() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenReturn(organization);
            when(organization.getTeamByName(TEAM_NAME)).thenReturn(team);
            when(team.listMembers()).thenReturn(userIterable);
            when(userIterable.toList()).thenReturn(List.of(userFoo, userBar));
            when(userFoo.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(userFoo.getEmail()).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> client.getDevelopers(ORGANIZATION_NAME, TEAM_NAME));
            assertEquals(String.format("Error while fetching email of user %s: %s", USER_FOO_LOGIN, "Connection error"),
                    exception.getMessage());
        }

    }

    @Nested
    class GetPullRequestInfoTests {

        private static final String OWNER = "baz";
        private static final String REPOSITORY = "qux";
        private static final int PR_NUMBER = 123;
        private static final String FOO_FILE_NAME = "foo.txt";
        private static final String BAR_FILE_NAME = "bar.txt";
        private static final int FOO_ADDITIONS = 10;
        private static final int FOO_DELETIONS = 15;
        private static final int BAR_ADDITIONS = 73;
        private static final int BAR_DELETIONS = 11;
        private static final int TOTAL_ADDITIONS = 83;
        private static final int TOTAL_DELETIONS = 26;

        @Mock
        private GitHubSettingsProvider settings;

        @Mock
        private GitHub delegate;

        @Mock
        private GHRepository repository;

        @Mock
        private GHPullRequest pullRequest;

        @Mock
        private PagedIterable<GHPullRequestFileDetail> fileDetailIterable;

        @Mock
        private GHPullRequestFileDetail fileDetailFoo;

        @Mock
        private GHPullRequestFileDetail fileDetailBar;

        @InjectMocks
        private GitHubClient gitHubClient;

        @Test
        void testGetPullRequestInfo() throws IOException {
            when(delegate.getRepository(String.format("%s/%s", OWNER, REPOSITORY))).thenReturn(repository);
            when(repository.getPullRequest(PR_NUMBER)).thenReturn(pullRequest);
            when(pullRequest.listFiles()).thenReturn(fileDetailIterable);
            when(pullRequest.getAdditions()).thenReturn(TOTAL_ADDITIONS);
            when(pullRequest.getDeletions()).thenReturn(TOTAL_DELETIONS);
            when(fileDetailIterable.toList()).thenReturn(Arrays.asList(fileDetailFoo, fileDetailBar));
            when(fileDetailFoo.getFilename()).thenReturn(FOO_FILE_NAME);
            when(fileDetailFoo.getAdditions()).thenReturn(FOO_ADDITIONS);
            when(fileDetailFoo.getDeletions()).thenReturn(FOO_DELETIONS);
            when(fileDetailBar.getFilename()).thenReturn(BAR_FILE_NAME);
            when(fileDetailBar.getAdditions()).thenReturn(BAR_ADDITIONS);
            when(fileDetailBar.getDeletions()).thenReturn(BAR_DELETIONS);
            PullRequest pullRequestFragments = new PullRequest(OWNER, REPOSITORY, PR_NUMBER);

            PullRequestFileDetails pullRequestInfo = gitHubClient.getPullRequestInfo(pullRequestFragments);
            PullRequestFileDetails expectedPullRequestInfo = new PullRequestFileDetails(TOTAL_ADDITIONS,
                    TOTAL_DELETIONS, List.of(//
                            ChangedFile.builder()
                                    .name(FOO_FILE_NAME)
                                    .additions(FOO_ADDITIONS)
                                    .deletions(FOO_DELETIONS)
                                    .build(),
                            ChangedFile.builder()
                                    .name(BAR_FILE_NAME)
                                    .additions(BAR_ADDITIONS)
                                    .deletions(BAR_DELETIONS)
                                    .build()));
            assertEquals(expectedPullRequestInfo, pullRequestInfo);
        }

        @Test
        void testGetPullRequestInfoWithConnectionError() throws IOException {
            PullRequest pullRequestFragments = new PullRequest(OWNER, REPOSITORY, PR_NUMBER);
            when(delegate.getRepository(String.format("%s/%s", OWNER, REPOSITORY)))
                    .thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> gitHubClient.getPullRequestInfo(pullRequestFragments));
            assertEquals("Error while fetching info for PR baz/qux/123: Connection error", exception.getMessage());
        }

    }

    @Nested
    class GetOrganizationAdminsTests {

        private static final String ORGANIZATION_NAME = "baz";
        private static final String USER_FOO_LOGIN = "foo";
        private static final String USER_BAR_LOGIN = "bar";
        private static final String USER_FOO_EMAIL = "foo@example.com";
        private static final String USER_BAR_EMAIL = "bar@example.com";

        @Mock
        private GitHub delegate;

        @Mock
        private GHOrganization organization;

        @Mock
        private PagedIterable<GHUser> userIterable;

        @Mock
        private GHUser userFoo;

        @Mock
        private GHUser userBar;

        @InjectMocks
        private GitHubClient client;

        @Test
        void testGetOrganizationAdmins() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenReturn(organization);
            when(organization.listMembersWithRole(GitHubClient.ORGANIZATION_ROLE_ADMIN)).thenReturn(userIterable);
            when(userIterable.toList()).thenReturn(List.of(userFoo, userBar));

            when(userFoo.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(userFoo.getEmail()).thenReturn(USER_FOO_EMAIL);

            when(userBar.getLogin()).thenReturn(USER_BAR_LOGIN);
            when(userBar.getEmail()).thenReturn(USER_BAR_EMAIL);

            List<Developer> developers = client.getOrganizationAdmins(ORGANIZATION_NAME);
            List<Developer> expectedDevelopers = List.of(new Developer(USER_FOO_LOGIN, USER_FOO_EMAIL),
                    new Developer(USER_BAR_LOGIN, USER_BAR_EMAIL));

            assertEquals(expectedDevelopers, developers);
        }

        @Test
        void testGetOrganizationAdminsWithConnectionError() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> client.getOrganizationAdmins(ORGANIZATION_NAME));
            assertEquals(String.format("Error while fetching GitHub users from organization %s: Connection error",
                    ORGANIZATION_NAME), exception.getMessage());
        }

        @Test
        void testGetOrganizationAdminsWithConnectionErrorWhenFetchingEmails() throws IOException {
            when(delegate.getOrganization(ORGANIZATION_NAME)).thenReturn(organization);
            when(organization.listMembersWithRole(GitHubClient.ORGANIZATION_ROLE_ADMIN)).thenReturn(userIterable);
            when(userIterable.toList()).thenReturn(List.of(userFoo, userBar));
            when(userFoo.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(userFoo.getEmail()).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class,
                    () -> client.getOrganizationAdmins(ORGANIZATION_NAME));
            assertEquals(String.format("Error while fetching email of user %s: %s", USER_FOO_LOGIN, "Connection error"),
                    exception.getMessage());
        }

    }

    @Nested
    class GetMyselfTests {

        private static final String USER_FOO_LOGIN = "foo";
        private static final String USER_FOO_EMAIL = "foo@example.com";

        @Mock
        private GitHub delegate;

        @Mock
        private GHMyself myself;

        @InjectMocks
        private GitHubClient client;

        @Test
        void testGetMyself() throws IOException {
            when(delegate.getMyself()).thenReturn(myself);

            when(myself.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(myself.getEmail()).thenReturn(USER_FOO_EMAIL);

            Developer developer = client.getMyself();
            Developer expectedDeveloper = new Developer(USER_FOO_LOGIN, USER_FOO_EMAIL);

            assertEquals(expectedDeveloper, developer);
        }

        @Test
        void testGetMyselfWithConnectionError() throws IOException {
            when(delegate.getMyself()).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class, () -> client.getMyself());
            assertEquals("Error while fetching GitHub user: Connection error", exception.getMessage());
        }

        @Test
        void testGetMyselfWithConnectionErrorWhenFetchingEmails() throws IOException {
            when(delegate.getMyself()).thenReturn(myself);

            when(myself.getLogin()).thenReturn(USER_FOO_LOGIN);
            when(myself.getEmail()).thenThrow(new IOException("Connection error"));

            Exception exception = assertThrows(GitHubClientException.class, () -> client.getMyself());
            assertEquals(String.format("Error while fetching email of user %s: %s", USER_FOO_LOGIN, "Connection error"),
                    exception.getMessage());
        }

    }

}
