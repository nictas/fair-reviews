package com.nictas.reviews.controller.rest;

import static com.nictas.reviews.TestUtils.assertJsonsMatch;
import static com.nictas.reviews.TestUtils.getResourceAsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nictas.reviews.controller.rest.dto.PullRequestAssignRequest;
import com.nictas.reviews.controller.rest.dto.PullRequestSearchRequest;
import com.nictas.reviews.domain.Developer;
import com.nictas.reviews.domain.PullRequestFileDetails;
import com.nictas.reviews.domain.PullRequestFileDetails.ChangedFile;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.error.NotFoundException;
import com.nictas.reviews.service.PullRequestReviewService;

@WebMvcTest(PullRequestReviewController.class)
class PullRequestReviewControllerTest {

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
            .build();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PullRequestReviewService pullRequestReviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetReviews() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        List<PullRequestReview> reviews = List.of(REVIEW_1, REVIEW_2, REVIEW_3);
        when(pullRequestReviewService.getAllReviews(pageable))
                .thenReturn(new PageImpl<>(reviews, pageable, reviews.size()));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/reviews"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "reviews-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetReview() throws Exception {
        when(pullRequestReviewService.getReview(REVIEW_1.getId())).thenReturn(REVIEW_1);

        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/reviews/91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "review-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testGetReviewNotFound() throws Exception {
        when(pullRequestReviewService.getReview(REVIEW_1.getId())).thenThrow(
                new NotFoundException("Could not find review with ID: 91a8bdeb-8457-4905-bd08-9d2a46f27b92"));

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Could not find review with ID: 91a8bdeb-8457-4905-bd08-9d2a46f27b92"));
    }

    @Test
    void testGetReviewInternalServerError() throws Exception {
        Exception e = new IllegalStateException("Unable to connect to DB");
        when(pullRequestReviewService.getReview(REVIEW_1.getId())).thenThrow(e);

        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .andExpect(MockMvcResultMatchers.status()
                        .is(500))
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(e.getMessage()));
    }

    @Test
    void testSearchReviews() throws Exception {
        PullRequestSearchRequest request = new PullRequestSearchRequest("https://github.com/foo/bar/pull/87");
        String requestBody = objectMapper.writeValueAsString(request);

        Pageable pageable = PageRequest.of(0, 20);
        List<PullRequestReview> reviews = List.of(REVIEW_1);
        when(pullRequestReviewService.getReviewsByUrl(request.getPullRequestUrl(), pageable))
                .thenReturn(new PageImpl<>(reviews, pageable, reviews.size()));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/reviews/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "reviews-search-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testAssignReviewer() throws Exception {
        PullRequestAssignRequest request = new PullRequestAssignRequest("https://github.com/foo/bar/pull/87",
                List.of(DEVELOPER_FOO.getLogin(), DEVELOPER_BAR.getLogin()), Collections.emptyList());
        String requestBody = objectMapper.writeValueAsString(request);

        when(pullRequestReviewService.assign(request.getPullRequestUrl(), request.getAssigneeList(),
                request.getAssigneeExclusionList())).thenReturn(List.of(DEVELOPER_FOO, DEVELOPER_BAR));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/reviews/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse()
                .getContentAsString();
        String expectedResponseBody = getResourceAsString(getClass(), "reviews-assign-response.json");
        assertJsonsMatch(expectedResponseBody, responseBody);
    }

    @Test
    void testDeleteReview() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/reviews/91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNoContent());
    }

    @Test
    void testDeleteReviewNotFound() throws Exception {
        doThrow(new NotFoundException("Could not find review with ID: 91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .when(pullRequestReviewService)
                .deleteReview(REVIEW_1.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/reviews/91a8bdeb-8457-4905-bd08-9d2a46f27b92"))
                .andExpect(MockMvcResultMatchers.status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Could not find review with ID: 91a8bdeb-8457-4905-bd08-9d2a46f27b92"));
    }

}
