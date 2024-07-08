package com.nictas.reviews.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.nictas.reviews.FairReviewsPostgreSQLContainer;
import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;

@Testcontainers
@DataJpaTest
@ContextConfiguration(initializers = {DeveloperRepositoryTest.Initializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeveloperRepositoryTest {

    @Container
    public static final FairReviewsPostgreSQLContainer POSTGRESQL_CONTAINER = FairReviewsPostgreSQLContainer
            .getInstance();

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues
                    .of("spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                            "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
                            "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword())
                    .applyTo(configurableApplicationContext.getEnvironment());
        }

    }

    private static final Developer DEVELOPER_FOO = Developer.builder()
            .login("foo")
            .email("foo@example.com")
            .score(87.8)
            .build();

    private static final Developer DEVELOPER_BAR = Developer.builder()
            .login("baz")
            .email("baz@example.com")
            .score(0.0)
            .build();

    private static final Developer DEVELOPER_BAZ = Developer.builder()
            .login("bar")
            .email("bar@example.com")
            .score(3.3)
            .build();

    private static final PullRequestReview REVIEW_1 = PullRequestReview.builder()
            .id(UUID.fromString("91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
            .pullRequestUrl("https://github.com/foo/bar/pull/87")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(15)
                            .deletions(11)
                            .build())))
            .score(20.7)
            .developer(DEVELOPER_FOO)
            .build();

    private static final PullRequestReview REVIEW_2 = PullRequestReview.builder()
            .id(UUID.fromString("dcb724e6-d2cb-4e63-a1ab-d5bc59e5cfdc"))
            .pullRequestUrl("https://github.com/foo/bar/pull/90")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(10)
                            .deletions(22)
                            .build(),
                    ChangedFile.builder()
                            .name("bar.java")
                            .additions(1)
                            .deletions(3)
                            .build())))
            .score(60.1)
            .developer(DEVELOPER_FOO)
            .build();

    @Autowired
    private DeveloperRepository developerRepository;
    @Autowired
    private PullRequestReviewRepository pullRequestReviewRepository;

    @Test
    void testSaveAndFindById() {
        developerRepository.save(DEVELOPER_FOO);

        Developer developer = developerRepository.findById(DEVELOPER_FOO.getLogin())
                .get();
        assertEquals(DEVELOPER_FOO, developer);
    }

    @Test
    void testFindByIdWithZeroDevelopers() {
        Optional<Developer> developer = developerRepository.findById(DEVELOPER_FOO.getLogin());
        assertTrue(developer.isEmpty());
    }

    @Test
    void testFindAll() {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR, DEVELOPER_BAZ);
        developers.forEach(developerRepository::save);

        Pageable pageable = Pageable.unpaged();
        Page<Developer> developersPage = developerRepository.findAll(pageable);
        Page<Developer> expectedDevelopersPage = new PageImpl<>(developers, pageable, developers.size());
        assertEquals(expectedDevelopersPage, developersPage);
    }

    @Test
    void testFindAllWithPagination() {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR, DEVELOPER_BAZ);
        developers.forEach(developerRepository::save);

        Pageable pageable = PageRequest.of(1, 1);
        Page<Developer> developersPage = developerRepository.findAll(pageable);
        Page<Developer> expectedDevelopersPage = new PageImpl<>(List.of(DEVELOPER_BAR), pageable, developers.size());
        assertEquals(expectedDevelopersPage, developersPage);
    }

    @Test
    void testFindWithLowestScore() {
        developerRepository.save(DEVELOPER_FOO);
        developerRepository.save(DEVELOPER_BAR);
        developerRepository.save(DEVELOPER_BAZ);

        Developer developerWithLowestScore = developerRepository.findWithLowestScore(Collections.emptyList())
                .get();
        assertEquals(DEVELOPER_BAR, developerWithLowestScore);
    }

    @Test
    void testFindWithLowestScoreWithLoginExclusionList() {
        developerRepository.save(DEVELOPER_FOO);
        developerRepository.save(DEVELOPER_BAR);
        developerRepository.save(DEVELOPER_BAZ);

        Developer developerWithLowestScore = developerRepository.findWithLowestScore(List.of(DEVELOPER_BAR.getLogin()))
                .get();
        assertEquals(DEVELOPER_BAZ, developerWithLowestScore);
    }

    @Test
    void testFindWithLowestScoreWithZeroDevelopers() {
        Optional<Developer> developer = developerRepository.findWithLowestScore(Collections.emptyList());
        assertTrue(developer.isEmpty());
    }

    @Test
    void testUpdate() {
        developerRepository.save(DEVELOPER_FOO);
        developerRepository.save(DEVELOPER_FOO.withScore(DEVELOPER_FOO.getScore() + 1.));

        Developer developer = developerRepository.findById(DEVELOPER_FOO.getLogin())
                .get();
        assertEquals(88.8, developer.getScore());
    }

    @Test
    void testDeleteById() {
        developerRepository.save(DEVELOPER_FOO);
        pullRequestReviewRepository.save(REVIEW_1);
        pullRequestReviewRepository.save(REVIEW_2);

        Page<PullRequestReview> reviews = pullRequestReviewRepository.findByDeveloperLogin(DEVELOPER_FOO.getLogin(),
                Pageable.unpaged());
        assertEquals(2, reviews.getSize());

        developerRepository.deleteById(DEVELOPER_FOO.getLogin());

        Optional<Developer> developer = developerRepository.findById(DEVELOPER_FOO.getLogin());
        assertTrue(developer.isEmpty());

        Page<PullRequestReview> reviewsAfterDeletion = pullRequestReviewRepository
                .findByDeveloperLogin(DEVELOPER_FOO.getLogin(), Pageable.unpaged());
        assertEquals(0, reviewsAfterDeletion.getSize());
    }

}
