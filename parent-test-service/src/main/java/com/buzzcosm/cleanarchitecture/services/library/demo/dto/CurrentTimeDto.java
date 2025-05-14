package com.buzzcosm.cleanarchitecture.services.library.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentTimeDto {
    private String currentTime;
}
