package com.nictas.reviews.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;

class InMemoryDeveloperRepositoryTest {

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

    private DeveloperRepository developerRepository = new InMemoryDeveloperRepository();

    @Test
    void testCreateAndGet() {
        developerRepository.create(DEVELOPER_FOO);

        Developer developer = developerRepository.get(DEVELOPER_FOO.getLogin())
                .get();
        assertEquals(DEVELOPER_FOO, developer);
    }

    @Test
    void testGetWithZeroDevelopers() {
        Optional<Developer> developer = developerRepository.get(DEVELOPER_FOO.getLogin());
        assertTrue(developer.isEmpty());
    }

    @Test
    void testGetAll() {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR, DEVELOPER_BAZ);
        developers.forEach(developerRepository::create);

        Pageable pageable = Pageable.unpaged();
        Page<Developer> developersPage = developerRepository.getAll(pageable);
        Page<Developer> expectedDevelopersPage = new PageImpl<>(developers, pageable, developers.size());
        assertEquals(expectedDevelopersPage, developersPage);
    }

    @Test
    void testGetAllWithPagination() {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR, DEVELOPER_BAZ);
        developers.forEach(developerRepository::create);

        Pageable pageable = PageRequest.of(1, 1);
        Page<Developer> developersPage = developerRepository.getAll(pageable);
        Page<Developer> expectedDevelopersPage = new PageImpl<>(List.of(DEVELOPER_BAR), pageable, developers.size());
        assertEquals(expectedDevelopersPage, developersPage);
    }

    @Test
    void testGetWithLowestScore() {
        developerRepository.create(DEVELOPER_FOO);
        developerRepository.create(DEVELOPER_BAR);
        developerRepository.create(DEVELOPER_BAZ);

        Developer developerWithLowestScore = developerRepository.getWithLowestScore(Collections.emptyList())
                .get();
        assertEquals(DEVELOPER_BAR, developerWithLowestScore);
    }

    @Test
    void testGetWithLowestScoreWithLoginExclusionList() {
        developerRepository.create(DEVELOPER_FOO);
        developerRepository.create(DEVELOPER_BAR);
        developerRepository.create(DEVELOPER_BAZ);

        Developer developerWithLowestScore = developerRepository.getWithLowestScore(List.of(DEVELOPER_BAR.getLogin()))
                .get();
        assertEquals(DEVELOPER_BAZ, developerWithLowestScore);
    }

    @Test
    void testGetWithLowestScoreWithZeroDevelopers() {
        Optional<Developer> developer = developerRepository.getWithLowestScore(Collections.emptyList());
        assertTrue(developer.isEmpty());
    }

    @Test
    void testUpdate() {
        developerRepository.create(DEVELOPER_FOO);
        developerRepository.update(DEVELOPER_FOO.withScore(DEVELOPER_FOO.getScore() + 1.));

        Developer developer = developerRepository.get(DEVELOPER_FOO.getLogin())
                .get();
        assertEquals(88.8, developer.getScore());
    }

    @Test
    void testDelete() {
        developerRepository.create(DEVELOPER_FOO);

        int deleted = developerRepository.delete(DEVELOPER_FOO.getLogin());
        assertEquals(1, deleted);

        Optional<Developer> developer = developerRepository.get(DEVELOPER_FOO.getLogin());
        assertTrue(developer.isEmpty());
    }

}
