package com.nictas.reviews.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.MultiplierRepository;
import com.nictas.reviews.repository.PullRequestReviewRepository;

@ExtendWith(MockitoExtension.class)
class MultiplierServiceTest {

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

    private static final PullRequestReview REVIEW_1 = PullRequestReview.builder()
            .id(UUID.fromString("91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
            .pullRequestUrl("https://github.com/foo/bar/pull/87")
            .pullRequestFileDetails(new PullRequestFileDetails(15, 11, List.of(//
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
            .pullRequestFileDetails(new PullRequestFileDetails(11, 25, List.of(//
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

    @Mock
    private MultiplierRepository multiplierRepository;
    @Mock
    private PullRequestReviewRepository pullRequestReviewRepository;

    @InjectMocks
    private MultiplierService multiplierService;

    @Test
    void testGetAllMultipliers() {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Multiplier> multipliersPage = new PageImpl<>(multipliers, pageable, multipliers.size());
        when(multiplierRepository.findAll(pageable)).thenReturn(multipliersPage);

        Page<Multiplier> actualMultipliersPage = multiplierService.getAllMultipliers(pageable);

        assertSame(multipliersPage, actualMultipliersPage);
    }

    @Test
    void testGetMultiplier() {
        when(multiplierRepository.findById(MULTIPLIER_1.getId())).thenReturn(Optional.of(MULTIPLIER_1));

        Multiplier multiplier = multiplierService.getMultiplier(MULTIPLIER_1.getId());

        assertEquals(MULTIPLIER_1, multiplier);
    }

    @Test
    void testGetMultiplierNotFound() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> multiplierService.getMultiplier(id));

        assertEquals("Could not find multiplier with ID: " + id, exception.getMessage());
    }

    @Test
    void testCreateMultiplier() {
        when(multiplierRepository.save(MULTIPLIER_1)).thenReturn(MULTIPLIER_1);
        Multiplier multiplier = multiplierService.saveMultiplier(MULTIPLIER_1);

        assertEquals(MULTIPLIER_1, multiplier);
        verify(multiplierRepository).save(MULTIPLIER_1);
    }

    @Test
    void testGetLatestMultiplier() {
        when(multiplierRepository.findLatest()).thenReturn(Optional.of(MULTIPLIER_1));

        Multiplier multiplier = multiplierService.getLatestMultiplier();

        assertSame(MULTIPLIER_1, multiplier);
    }

    @Test
    void testGetLatestMultiplierWithNoMultipliers() {
        when(multiplierRepository.findLatest()).thenReturn(Optional.empty());
        when(multiplierRepository.save(MultiplierService.DEFAULT_MULTIPLIER))
                .thenReturn(MultiplierService.DEFAULT_MULTIPLIER);

        Multiplier multiplier = multiplierService.getLatestMultiplier();

        assertSame(MultiplierService.DEFAULT_MULTIPLIER, multiplier);
    }

    @Test
    void testDeleteMultiplier() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.findById(id)).thenReturn(Optional.of(MULTIPLIER_1));
        when(pullRequestReviewRepository.findByMultiplierId(MULTIPLIER_1.getId(), Pageable.unpaged()))
                .thenReturn(Page.empty());

        assertDoesNotThrow(() -> multiplierService.deleteMultiplier(id));

        verify(multiplierRepository).deleteById(id);
    }

    @Test
    void testDeleteMultiplierNotFound() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> multiplierService.deleteMultiplier(id));

        assertEquals("Could not find multiplier with ID: " + id, exception.getMessage());
    }

    @Test
    void testDeleteMultiplierStillInUse() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.findById(id)).thenReturn(Optional.of(MULTIPLIER_1));
        Pageable pageable = Pageable.unpaged();
        Page<PullRequestReview> reviewsPage = new PageImpl<>(List.of(REVIEW_1, REVIEW_2), pageable, 2);
        when(pullRequestReviewRepository.findByMultiplierId(MULTIPLIER_1.getId(), pageable)).thenReturn(reviewsPage);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> multiplierService.deleteMultiplier(id));

        assertEquals(String.format("Multiplier %s is still referenced in %d reviews", id, reviewsPage.getSize()),
                exception.getMessage());
    }

}
