package com.buzzcosm.cleanarchitecture.services.library.demo.service.impl;

import com.buzzcosm.cleanarchitecture.services.library.demo.service.TimeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Primary
public class SimpleTimeService implements TimeService {

    @Override
    public String getCurrentTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");

        String formattedCurrentTime = currentDateTime.format(dateTimeFormatter);

        return formattedCurrentTime;
    }
}
