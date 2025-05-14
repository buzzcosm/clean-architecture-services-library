package com.buzzcosm.cleanarchitecture.services.library.demo.controller;

import com.buzzcosm.cleanarchitecture.services.library.demo.dto.CurrentTimeDto;
import com.buzzcosm.cleanarchitecture.services.library.demo.service.TimeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/time")
@AllArgsConstructor
public class TimeController {

    private final TimeService timeService;

    @GetMapping
    public ResponseEntity<CurrentTimeDto> getCurrentTime() {
        CurrentTimeDto currentTimeDto = CurrentTimeDto.builder()
                .currentTime(timeService.getCurrentTime())
                .build();

        return ResponseEntity.ok(currentTimeDto);
    }
}
