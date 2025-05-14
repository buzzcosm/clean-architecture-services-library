package com.buzzcosm.cleanarchitecture.services.library.demo.controller;

import com.buzzcosm.cleanarchitecture.services.library.demo.service.TimeService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@WebMvcTest(TimeController.class)
class TimeControllerTest {

    private static final String TIME_PATH = "/api/v1/time";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeService timeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCurrentTime() throws Exception {
        String mockedTime = "12:30:45";
        given(timeService.getCurrentTime()).willReturn(mockedTime);

        MvcResult mvcResult = mockMvc.perform(get(TIME_PATH)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTime").value(mockedTime))
                .andReturn();

        verify(timeService).getCurrentTime();

        log.debug("Debug MVC Result: {}", mvcResult.getResponse().getContentAsString());
    }

}