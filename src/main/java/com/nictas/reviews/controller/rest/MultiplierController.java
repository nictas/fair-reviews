package com.nictas.reviews.controller.rest;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nictas.reviews.domain.Multiplier;
import com.nictas.reviews.service.MultiplierService;
import com.nictas.reviews.service.scheduled.MultiplierApplierService;

@RestController
@RequestMapping("/multipliers")
public class MultiplierController {

    private final MultiplierService multiplierService;
    private final MultiplierApplierService multiplierApplierService;
    private final TaskScheduler taskScheduler;

    @Autowired
    public MultiplierController(MultiplierService developerService, MultiplierApplierService multiplierApplierService,
                                TaskScheduler taskScheduler) {
        this.multiplierService = developerService;
        this.multiplierApplierService = multiplierApplierService;
        this.taskScheduler = taskScheduler;
    }

    @GetMapping
    public Page<Multiplier> getAllMultipliers(Pageable pageable) {
        return multiplierService.getAllMultipliers(pageable);
    }

    @GetMapping("/{id}")
    public Multiplier getMultiplier(@PathVariable("id") UUID id) {
        return multiplierService.getMultiplier(id);
    }

    @GetMapping("/latest")
    public Multiplier getLatestMultiplier() {
        return multiplierService.getLatestMultiplier();
    }

    @PostMapping("/latest/apply")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void applyLatestMultiplier() {
        taskScheduler.schedule(multiplierApplierService::applyLatestMultiplier, Instant.now());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Multiplier createMultiplier(@RequestBody Multiplier multiplier) {
        return multiplierService.createMultiplier(multiplier);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMultiplier(@PathVariable("id") UUID id) {
        multiplierService.deleteMultiplier(id);
    }

}
