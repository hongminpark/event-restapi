package com.navercorp.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest // Mock을 대신하여 통합테스트, 모든 bean 등록
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상 이벤트 요청")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API Dev with Spring boot")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,02,9,12,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,02,9,15,00))
                .beginEventDateTime(LocalDateTime.of(2020,02,19,12,00))
                .endEventDateTime(LocalDateTime.of(2020,02,19,15,00))
                .basePrice(10000)
                .maxPrice(20000)
                .limitOfEnrollment(100)
                .location("Nave D2")
                .build();

        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        ;
    }

    @Test
    @TestDescription("입력 파라미터를 초과한 경우 에러 발생 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("Rest API Dev with Spring boot")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,02,9,12,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,02,9,15,00))
                .beginEventDateTime(LocalDateTime.of(2020,02,19,12,00))
                .endEventDateTime(LocalDateTime.of(2020,02,19,15,00))
                .basePrice(10000)
                .maxPrice(20000)
                .limitOfEnrollment(100)
                .location("Nave D2")
                .free(true)
                .offline(false)
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있을 경우 에러 발생 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();
        this.mockMvc.perform(post("/api/events")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 올바르지 않을 경우 에러 발생 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("Rest API Dev with Spring boot")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,02,9,12,00))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,02,9,15,00))
                .beginEventDateTime(LocalDateTime.of(2020,02,2,12,00))
                .endEventDateTime(LocalDateTime.of(2020,02,2,15,00))
                .basePrice(10000)
                .maxPrice(2000)
                .limitOfEnrollment(100)
                .location("Nave D2")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
        ;
    }

}
