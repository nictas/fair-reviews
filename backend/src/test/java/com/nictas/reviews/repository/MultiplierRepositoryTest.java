package com.nictas.reviews.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;

@Testcontainers
@DataJpaTest
@ContextConfiguration(initializers = {MultiplierRepositoryTest.Initializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MultiplierRepositoryTest {

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

    private static final Multiplier MULTIPLIER_3 = Multiplier.builder()
            .id(UUID.fromString("4c393c93-75ac-487c-a241-b6bfbd0b91f8"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .fileExtension(".java")
                            .additionsMultiplier(2.5)
                            .deletionsMultiplier(0.4)
                            .build(), //
                    FileMultiplier.builder()
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.5)
                            .deletionsMultiplier(0.2)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 4, 25, 12, 22, 20, 0, ZoneOffset.UTC))
            .build();

    @Autowired
    private MultiplierRepository multiplierRepository;

    @Test
    void testSaveAndFindById() {
        Multiplier createdMultiplier = multiplierRepository.save(MULTIPLIER_1);
        assertEquals(MULTIPLIER_1, createdMultiplier);

        Multiplier multiplier = multiplierRepository.findById(MULTIPLIER_1.getId())
                .get();
        assertEquals(MULTIPLIER_1, multiplier);
    }

    @Test
    void testFindByIdWithZeroMultipliers() {
        Optional<Multiplier> multiplier = multiplierRepository.findById(MULTIPLIER_1.getId());
        assertTrue(multiplier.isEmpty());
    }

    @Test
    void testFindLatest() {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2, MULTIPLIER_3);
        multipliers.forEach(multiplierRepository::save);

        Multiplier multiplier = multiplierRepository.findLatest()
                .get();
        assertEquals(MULTIPLIER_2, multiplier);
    }

    @Test
    void testFindLatestWithZeroMultipliers() {
        Optional<Multiplier> multiplier = multiplierRepository.findLatest();
        assertTrue(multiplier.isEmpty());
    }

    @Test
    void testFindAll() {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2, MULTIPLIER_3);
        multipliers.forEach(multiplierRepository::save);

        Pageable pageable = Pageable.unpaged();
        Page<Multiplier> multipliersPage = multiplierRepository.findAll(pageable);
        Page<Multiplier> expectedMultipliersPage = new PageImpl<>(multipliers, pageable, multipliers.size());
        assertEquals(expectedMultipliersPage, multipliersPage);
    }

    @Test
    void testFindAllWithPagination() {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2, MULTIPLIER_3);
        multipliers.forEach(multiplierRepository::save);

        Pageable pageable = PageRequest.of(1, 1);
        Page<Multiplier> multipliersPage = multiplierRepository.findAll(pageable);
        Page<Multiplier> expectedMultipliersPage = new PageImpl<>(List.of(MULTIPLIER_2), pageable, multipliers.size());
        assertEquals(expectedMultipliersPage, multipliersPage);
    }

    @Test
    void testUpdate() {
        multiplierRepository.save(MULTIPLIER_1);
        Multiplier mutiplier = multiplierRepository.save(MULTIPLIER_1.withDefaultAdditionsMultiplier(5.0));

        assertEquals(5.0, mutiplier.getDefaultAdditionsMultiplier());
        assertEquals(0.2, mutiplier.getDefaultDeletionsMultiplier());
    }

    @Test
    void testDeleteById() {
        multiplierRepository.save(MULTIPLIER_1);

        multiplierRepository.deleteById(MULTIPLIER_1.getId());

        Optional<Multiplier> multiplier = multiplierRepository.findById(MULTIPLIER_1.getId());
        assertTrue(multiplier.isEmpty());
    }

}
