package com.nictas.reviews.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;

@Testcontainers
@DataJpaTest
@ContextConfiguration(initializers = {PullRequestReviewRepositoryTest.Initializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PullRequestReviewRepositoryTest {

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

    private static final Multiplier MULTIPLIER_1 = Multiplier.builder()
            .id(UUID.fromString("2f7fc3e6-b54f-4593-aaca-98aeed3d6d02"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(2.0)
                            .deletionsMultiplier(0.4)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.5)
                            .deletionsMultiplier(0.2)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 3, 3, 17, 15, 0, 0, ZoneOffset.UTC))
            .build();

    private static final Multiplier MULTIPLIER_2 = Multiplier.builder()
            .id(UUID.fromString("98626460-80e1-4acc-b2ea-b28e018ca6d2"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(3.0)
                            .deletionsMultiplier(0.2)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.2)
                            .deletionsMultiplier(0.1)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 5, 13, 6, 0, 0, 0, ZoneOffset.UTC))
            .build();

    private static final Developer DEVELOPER_FOO = Developer.builder()
            .login("foo")
            .email("foo@example.com")
            .score(80.8)
            .build();

    private static final Developer DEVELOPER_BAR = Developer.builder()
            .login("bar")
            .email("bar@example.com")
            .score(7.)
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
            .multiplier(MULTIPLIER_1)
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
            .multiplier(MULTIPLIER_1)
            .build();

    private static final PullRequestReview REVIEW_3 = PullRequestReview.builder()
            .id(UUID.fromString("ee2c8153-17a8-486f-93c1-78599eb7e5bf"))
            .pullRequestUrl("https://github.com/foo/bar/pull/91")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(7)
                            .deletions(0)
                            .build())))
            .score(7.0)
            .developer(DEVELOPER_BAR)
            .multiplier(MULTIPLIER_2)
            .build();

    private static final PullRequestReview REVIEW_4 = PullRequestReview.builder()
            .id(UUID.fromString("d5583fa3-630d-4eda-ba76-a27fa29ebaf2"))
            .pullRequestUrl("https://github.com/foo/bar/pull/91")
            .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                    ChangedFile.builder()
                            .name("foo.java")
                            .additions(7)
                            .deletions(0)
                            .build())))
            .score(7.0)
            .developer(DEVELOPER_FOO)
            .multiplier(MULTIPLIER_2)
            .build();

    @Autowired
    private DeveloperRepository developerRepository;
    @Autowired
    private MultiplierRepository multiplierRepository;
    @Autowired
    private PullRequestReviewRepository pullRequestReviewRepository;

    @BeforeEach
    void setUp() {
        developerRepository.save(DEVELOPER_FOO);
        developerRepository.save(DEVELOPER_BAR);
        multiplierRepository.save(MULTIPLIER_1);
        multiplierRepository.save(MULTIPLIER_2);
    }

    @Test
    void testSaveAndFindById() {
        pullRequestReviewRepository.save(REVIEW_1);

        PullRequestReview review = pullRequestReviewRepository.findById(REVIEW_1.getId())
                .get();
        assertEquals(REVIEW_1, review);
    }

    @Test
    void testFindByIdWithZeroReviews() {
        Optional<PullRequestReview> review = pullRequestReviewRepository.findById(REVIEW_1.getId());
        assertTrue(review.isEmpty());
    }

    @Test
    void testFindAll() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = Pageable.unpaged();
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository.findAll(pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(reviews, pageable, reviews.size());
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testFindAllWithPagination() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = PageRequest.of(1, 1);
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository.findAll(pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_2), pageable, reviews.size());
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testFindByPullRequestUrl() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3, REVIEW_4);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = Pageable.unpaged();
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository
                .findByPullRequestUrl(REVIEW_3.getPullRequestUrl(), pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_3, REVIEW_4), pageable, 2);
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testFindByPullRequestUrlWithPagination() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3, REVIEW_4);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = PageRequest.of(1, 1);
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository
                .findByPullRequestUrl(REVIEW_3.getPullRequestUrl(), pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_4), pageable, 2);
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testFindByDeveloperLogin() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3, REVIEW_4);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = Pageable.unpaged();
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository.findByDeveloperLogin(DEVELOPER_FOO.getLogin(),
                pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_1, REVIEW_2, REVIEW_4), pageable,
                3);
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void tesFindByDeveloperLoginWithPagination() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3, REVIEW_4);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = PageRequest.of(0, 2);
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository.findByDeveloperLogin(DEVELOPER_FOO.getLogin(),
                pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_1, REVIEW_2), pageable, 3);
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testFindWithDifferentMultiplierIds() {
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3, REVIEW_4);
        reviews.forEach(pullRequestReviewRepository::save);

        Pageable pageable = PageRequest.of(0, 1);
        Page<PullRequestReview> reviewsPage = pullRequestReviewRepository
                .findWithDifferentMultiplierIds(MULTIPLIER_1.getId(), pageable);
        Page<PullRequestReview> expectedReviewsPage = new PageImpl<>(List.of(REVIEW_3), pageable, 2);
        assertEquals(expectedReviewsPage, reviewsPage);
    }

    @Test
    void testUpdate() {
        pullRequestReviewRepository.save(REVIEW_1);
        pullRequestReviewRepository.save(REVIEW_1.withScore(REVIEW_1.getScore() + 1.));

        PullRequestReview review = pullRequestReviewRepository.findById(REVIEW_1.getId())
                .get();
        assertEquals(21.7, review.getScore());
    }

    @Test
    void testDeleteById() {
        pullRequestReviewRepository.save(REVIEW_1);

        pullRequestReviewRepository.deleteById(REVIEW_1.getId());

        Optional<PullRequestReview> review = pullRequestReviewRepository.findById(REVIEW_1.getId());
        assertTrue(review.isEmpty());
    }

}
