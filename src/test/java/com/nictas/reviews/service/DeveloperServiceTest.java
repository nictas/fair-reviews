package com.nictas.reviews.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.DeveloperRepository;

@ExtendWith(MockitoExtension.class)
class DeveloperServiceTest {

    private static final Developer DEVELOPER_FOO = new Developer("foo", "foo@example.com");
    private static final Developer DEVELOPER_BAR = new Developer("foo", "foo@example.com");

    @Mock
    private DeveloperRepository developerRepository;

    @InjectMocks
    private DeveloperService developerService;

    @Test
    void testGetAllDevelopers() {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Developer> developersPage = new PageImpl<>(developers, pageable, developers.size());
        when(developerRepository.getAll(pageable)).thenReturn(developersPage);

        Page<Developer> actualDevelopersPage = developerService.getAllDevelopers(pageable);

        assertSame(developersPage, actualDevelopersPage);
    }

    @Test
    void testGetDeveloper() {
        when(developerRepository.get(DEVELOPER_FOO.getLogin())).thenReturn(Optional.of(DEVELOPER_FOO));

        Developer developer = developerService.getDeveloper(DEVELOPER_FOO.getLogin());

        assertEquals(DEVELOPER_FOO, developer);
    }

    @Test
    void testGetDeveloperNotFound() {
        String login = DEVELOPER_FOO.getLogin();
        when(developerRepository.get(login)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> developerService.getDeveloper(login));

        assertEquals("Could not find developer with login: " + login, exception.getMessage());
    }

    @Test
    void testCreateDeveloper() {
        developerService.createDeveloper(DEVELOPER_FOO);

        verify(developerRepository).create(DEVELOPER_FOO);
    }

    @Test
    void testUpdateDeveloper() {
        developerService.updateDeveloper(DEVELOPER_FOO);

        verify(developerRepository).update(DEVELOPER_FOO);
    }

    @Test
    void testGetDeveloperWithLowestScore() {
        List<String> loginExclusionList = Collections.emptyList();
        when(developerRepository.getWithLowestScore(loginExclusionList)).thenReturn(Optional.of(DEVELOPER_FOO));

        Developer developer = developerService.getDeveloperWithLowestScore(loginExclusionList);

        assertSame(DEVELOPER_FOO, developer);
    }

    @Test
    void testGetDeveloperWithLowestScoreNotFound() {
        List<String> loginExclusionList = Collections.emptyList();
        when(developerRepository.getWithLowestScore(loginExclusionList)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> developerService.getDeveloperWithLowestScore(loginExclusionList));

        assertEquals("Could not find developer with lowest score", exception.getMessage());
    }

    @Test
    void testDeleteDeveloper() {
        String login = DEVELOPER_FOO.getLogin();
        when(developerRepository.delete(login)).thenReturn(1);

        assertDoesNotThrow(() -> developerService.deleteDeveloper(login));

        verify(developerRepository).delete(login);
    }

    @Test
    void testDeleteDeveloperNotFound() {
        String login = DEVELOPER_FOO.getLogin();
        when(developerRepository.delete(login)).thenReturn(0);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> developerService.deleteDeveloper(login));

        assertEquals("Could not find developer with login: " + login, exception.getMessage());
    }

}
