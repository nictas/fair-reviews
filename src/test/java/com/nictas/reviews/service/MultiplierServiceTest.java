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

import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.repository.MultiplierRepository;

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

    @Mock
    private MultiplierRepository multiplierRepository;

    @InjectMocks
    private MultiplierService multiplierService;

    @Test
    void testGetAllMultipliers() {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Multiplier> multipliersPage = new PageImpl<>(multipliers, pageable, multipliers.size());
        when(multiplierRepository.getAll(pageable)).thenReturn(multipliersPage);

        Page<Multiplier> actualMultipliersPage = multiplierService.getAllMultipliers(pageable);

        assertSame(multipliersPage, actualMultipliersPage);
    }

    @Test
    void testGetMultiplier() {
        when(multiplierRepository.get(MULTIPLIER_1.getId())).thenReturn(Optional.of(MULTIPLIER_1));

        Multiplier multiplier = multiplierService.getMultiplier(MULTIPLIER_1.getId());

        assertEquals(MULTIPLIER_1, multiplier);
    }

    @Test
    void testGetMultiplierNotFound() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.get(id)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> multiplierService.getMultiplier(id));

        assertEquals("Could not find multiplier with ID: " + id, exception.getMessage());
    }

    @Test
    void testCreateMultiplier() {
        when(multiplierRepository.create(MULTIPLIER_1)).thenReturn(MULTIPLIER_1);
        Multiplier multiplier = multiplierService.createMultiplier(MULTIPLIER_1);

        assertEquals(MULTIPLIER_1, multiplier);
        verify(multiplierRepository).create(MULTIPLIER_1);
    }

    @Test
    void testUpdateMultiplier() {
        when(multiplierRepository.update(MULTIPLIER_1)).thenReturn(MULTIPLIER_1);
        Multiplier multiplier = multiplierService.updateMultiplier(MULTIPLIER_1);

        assertEquals(MULTIPLIER_1, multiplier);
        verify(multiplierRepository).update(MULTIPLIER_1);
    }

    @Test
    void testGetLatestMultiplier() {
        when(multiplierRepository.getLatest()).thenReturn(Optional.of(MULTIPLIER_1));

        Multiplier multiplier = multiplierService.getLatestMultiplier();

        assertSame(MULTIPLIER_1, multiplier);
    }

    @Test
    void testGetLatestMultiplierDefault() {
        when(multiplierRepository.getLatest()).thenReturn(Optional.empty());

        Multiplier multiplier = multiplierService.getLatestMultiplier();

        assertSame(MultiplierService.DEFAULT_MULTIPLIER, multiplier);
        verify(multiplierRepository).create(MultiplierService.DEFAULT_MULTIPLIER);
    }

    @Test
    void testDeleteDeveloper() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.delete(id)).thenReturn(1);

        assertDoesNotThrow(() -> multiplierService.deleteMultiplier(id));

        verify(multiplierRepository).delete(id);
    }

    @Test
    void testDeleteDeveloperNotFound() {
        UUID id = MULTIPLIER_1.getId();
        when(multiplierRepository.delete(id)).thenReturn(0);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> multiplierService.deleteMultiplier(id));

        assertEquals("Could not find multiplier with ID: " + id, exception.getMessage());
    }

}
