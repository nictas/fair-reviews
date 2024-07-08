package com.nictas.reviews.controller.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nictas.reviews.controller.rest.dto.PullRequestAssignRequest;
import com.nictas.reviews.controller.rest.dto.PullRequestSearchRequest;
import com.nictas.reviews.domain.PullRequestReview;
import com.nictas.reviews.service.PullRequestReviewService;

@RestController
@RequestMapping("/reviews")
public class PullRequestReviewController {

    private final PullRequestReviewService pullRequestService;

    @Autowired
    public PullRequestReviewController(PullRequestReviewService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    @GetMapping
    public Page<PullRequestReview> getAllReviews(Pageable pageable) {
        return pullRequestService.getAllReviews(pageable);
    }

    @GetMapping("/{id}")
    public PullRequestReview getReview(@PathVariable UUID id) {
        return pullRequestService.getReview(id);
    }

    @PostMapping("/search")
    public Page<PullRequestReview> search(@RequestBody PullRequestSearchRequest request, Pageable pageable) {
        return pullRequestService.getReviewsByUrl(request.getPullRequestUrl(), pageable);
    }

    @PostMapping("/assign")
    public List<PullRequestReview> assign(@RequestBody PullRequestAssignRequest request) {
        return pullRequestService.assign(request.getPullRequestUrl(), request.getAssigneeList(),
                request.getAssigneeExclusionList());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable UUID id) {
        pullRequestService.deleteReview(id);
    }

}
