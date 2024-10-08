package com.nictas.reviews.controller.rest;

import static com.nictas.reviews.TestUtils.assertJsonsMatch;
import static com.nictas.reviews.TestUtils.getResourceAsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nictas.reviews.configuration.GitHubOpaqueTokenIntrospector;
import com.nictas.reviews.configuration.SecurityConfiguration;
import com.nictas.reviews.domain.FileMultiplier;
import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.scheduled.MultiplierApplierService;

@WebMvcTest(MultiplierController.class)
@Import(SecurityConfiguration.class)
class MultiplierControllerTest {

    private static final Multiplier MULTIPLIER_1 = Multiplier.builder()
            .id(UUID.fromString("2f7fc3e6-b54f-4593-aaca-98aeed3d6d02"))
            .defaultAdditionsMultiplier(1.0)
            .defaultDeletionsMultiplier(0.2)
            .fileMultipliers(List.of( //
                    FileMultiplier.builder()
                            .id(UUID.fromString("9672f226-c1a2-4b78-872f-f0558041e10d"))
                            .fileExtension(".java")
                            .additionsMultiplier(2.0)
                            .deletionsMultiplier(0.4)
                            .build(), //
                    FileMultiplier.builder()
                            .id(UUID.fromString("428a6e1b-9d36-4478-96cb-591981fd7e4c"))
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
                            .id(UUID.fromString("6f94d113-62ed-4f06-a8dc-2a24074df93a"))
                            .fileExtension(".java")
                            .additionsMultiplier(3.0)
                            .deletionsMultiplier(0.2)
                            .build(), //
                    FileMultiplier.builder()
                            .id(UUID.fromString("14310f4e-d6e4-41da-a0ae-5be1ceb5e9cf"))
                            .fileExtension(".yaml")
                            .additionsMultiplier(0.2)
                            .deletionsMultiplier(0.1)
                            .build() //
            ))
            .createdAt(OffsetDateTime.of(2024, 5, 13, 6, 0, 0, 0, ZoneOffset.UTC))
            .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MultiplierService multiplierService;

    @MockBean
    private MultiplierApplierService multiplierApplierService;

    @MockBean
    private TaskScheduler taskScheduler;

    @MockBean
    private GitHubOpaqueTokenIntrospector introspector;

    @Test
    void testGetMultipliers() throws Exception {
        List<Multiplier> multipliers = List.of(MULTIPLIER_1, MULTIPLIER_2);
        Pageable pageable = PageRequest.of(0, 20);
        when(multiplierService.getAllMultipliers(pageable))
                .thenReturn(new PageImpl<>(multipliers, pageable, multipliers.size()));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/multipliers")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "multipliers-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetMultipliersUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/multipliers"))
                .andExpect(MockMvcResultMatchers.status()
                        .isUnauthorized());
    }

    @Test
    void testGetMultiplier() throws Exception {
        when(multiplierService.getMultiplier(MULTIPLIER_1.getId())).thenReturn(MULTIPLIER_1);

        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                        .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                                .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "multiplier-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetMultiplierNotFound() throws Exception {
        NotFoundException e = new NotFoundException(
                "Could not find multiplier with ID: 2f7fc3e6-b54f-4593-aaca-98aeed3d6d02");
        when(multiplierService.getMultiplier(MULTIPLIER_1.getId())).thenThrow(e);

        mockMvc.perform(MockMvcRequestBuilders.get("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(e.getMessage()));
    }

    @Test
    void testGetMultiplierInternalServerError() throws Exception {
        Exception e = new IllegalStateException("Unable to connect to DB");
        when(multiplierService.getMultiplier(MULTIPLIER_1.getId())).thenThrow(e);

        mockMvc.perform(MockMvcRequestBuilders.get("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .is(500))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(e.getMessage()));
    }

    @Test
    void testGetLatestMultiplier() throws Exception {
        when(multiplierService.getLatestMultiplier()).thenReturn(MULTIPLIER_1);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/multipliers/latest")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "multiplier-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testCreateMultiplier() throws Exception {
        when(multiplierService.saveMultiplier(MULTIPLIER_1)).thenReturn(MULTIPLIER_1);

        String multiplierJson = objectMapper.writeValueAsString(MULTIPLIER_1);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/multipliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(multiplierJson)
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_ADMIN)))
                .andExpect(MockMvcResultMatchers.status()
                        .isCreated())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "multiplier-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testCreateMultiplierForbidden() throws Exception {
        String multiplierJson = objectMapper.writeValueAsString(MULTIPLIER_1);
        mockMvc.perform(MockMvcRequestBuilders.post("/multipliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(multiplierJson)
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isForbidden());
    }

    @Test
    void testDeleteMultiplier() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_ADMIN)))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent());
    }

    @Test
    void testDeleteMultiplierForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isForbidden());
    }

    @Test
    void testDeleteMultiplierNotFound() throws Exception {
        NotFoundException e = new NotFoundException(
                "Could not find multiplier with ID: 2f7fc3e6-b54f-4593-aaca-98aeed3d6d02");
        doThrow(e).when(multiplierService)
                .deleteMultiplier(MULTIPLIER_1.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/multipliers/2f7fc3e6-b54f-4593-aaca-98aeed3d6d02")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_ADMIN)))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(e.getMessage()));
    }

    @Test
    void testApplyLatestMultiplier() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/multipliers/latest/apply")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_ADMIN)))
                .andExpect(MockMvcResultMatchers.status()
                        .isAccepted());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(taskScheduler).schedule(runnableCaptor.capture(), instantCaptor.capture());

        Runnable runnable = runnableCaptor.getValue();
        runnable.run();
        verify(multiplierApplierService).applyLatestMultiplier();

        Instant instant = instantCaptor.getValue();
        Instant now = Instant.now();
        assertTrue(instant.isBefore(now) || instant.equals(now));
    }

    @Test
    void testApplyLatestMultiplierForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/multipliers/latest/apply")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isForbidden());
    }

}
