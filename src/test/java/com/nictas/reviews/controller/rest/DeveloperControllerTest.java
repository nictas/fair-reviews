package com.nictas.reviews.controller.rest;

import static com.nictas.reviews.TestUtils.assertJsonsMatch;
import static com.nictas.reviews.TestUtils.getResourceAsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.service.DeveloperService;
import com.nictas.reviews.service.PullRequestReviewService;

@WebMvcTest(DeveloperController.class)
class DeveloperControllerTest {

    private static final Developer DEVELOPER_FOO = Developer.builder()
            .login("foo")
            .email("foo@example.com")
            .score(87.8)
            .build();

    private static final Developer DEVELOPER_BAR = Developer.builder()
            .login("bar")
            .email("bar@example.com")
            .score(3.3)
            .build();

    private static final Developer DEVELOPER_BAZ = Developer.builder()
            .login("baz")
            .email("baz@example.com")
            .score(0.0)
            .build();

    private static final List<PullRequestReview> DEVELOPER_FOO_HISTORY = List.of(//
            PullRequestReview.builder()
                    .id(UUID.fromString("bf6eb647-4809-4643-96e3-be8ade25afbd"))
                    .pullRequestUrl("https://github.com/foo/bar/pull/87")
                    .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                            ChangedFile.builder()
                                    .name("foo.java")
                                    .additions(15)
                                    .deletions(11)
                                    .build())))
                    .score(20.7)
                    .build(),
            PullRequestReview.builder()
                    .id(UUID.fromString("406d6cd4-6801-48ae-bcfa-649d4986bdf8"))
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
                    .build(),
            PullRequestReview.builder()
                    .id(UUID.fromString("636c02b9-0675-4aa1-84d1-4f1c5690a259"))
                    .pullRequestUrl("https://github.com/foo/bar/pull/91")
                    .pullRequestFileDetails(new PullRequestFileDetails(List.of(//
                            ChangedFile.builder()
                                    .name("foo.java")
                                    .additions(7)
                                    .deletions(0)
                                    .build())))
                    .score(7.0)
                    .build());

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeveloperService developerService;

    @MockBean
    private PullRequestReviewService pullRequestReviewService;

    @Test
    void testGetDevelopers() throws Exception {
        List<Developer> developers = List.of(DEVELOPER_FOO, DEVELOPER_BAR, DEVELOPER_BAZ);
        Pageable pageable = PageRequest.of(0, 20);
        when(developerService.getAllDevelopers(pageable))
                .thenReturn(new PageImpl<>(developers, pageable, developers.size()));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/developers"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "developers-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetDeveloper() throws Exception {
        when(developerService.getDeveloper(DEVELOPER_FOO.getLogin())).thenReturn(DEVELOPER_FOO);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/developers/foo"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "developer-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetDeveloperNotFound() throws Exception {
        when(developerService.getDeveloper(DEVELOPER_FOO.getLogin()))
                .thenThrow(new NotFoundException("Could not find developer with login: foo"));

        mockMvc.perform(MockMvcRequestBuilders.get("/developers/foo"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Could not find developer with login: foo"));
    }

    @Test
    void testGetDeveloperInternalServerError() throws Exception {
        Exception e = new IllegalStateException("Unable to connect to DB");
        when(developerService.getDeveloper(DEVELOPER_FOO.getLogin())).thenThrow(e);

        mockMvc.perform(MockMvcRequestBuilders.get("/developers/foo"))
                .andExpect(MockMvcResultMatchers.status()
                        .is(500))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(e.getMessage()));
    }

    @Test
    void testGetDeveloperHistory() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        when(pullRequestReviewService.getReviewsByDeveloperLogin(DEVELOPER_FOO.getLogin(), pageable))
                .thenReturn(new PageImpl<>(DEVELOPER_FOO_HISTORY, pageable, DEVELOPER_FOO_HISTORY.size()));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/developers/foo/history"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "developer-history-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testDeleteDeveloper() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/developers/foo"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent());
    }

    @Test
    void testDeleteDeveloperNotFound() throws Exception {
        doThrow(new NotFoundException("Could not find developer with login: foo")).when(developerService)
                .deleteDeveloper(DEVELOPER_FOO.getLogin());

        mockMvc.perform(MockMvcRequestBuilders.delete("/developers/foo"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Could not find developer with login: foo"));
    }

}
