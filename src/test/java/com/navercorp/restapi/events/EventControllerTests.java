package com.navercorp.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.restapi.common.RestDocsConfiguration;
import com.navercorp.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest // Mock을 대신하여 통합테스트, 모든 bean 등록
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test") // Use application-test.properties with application.properties
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

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
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query event"),
                                linkWithRel("update-event").description("link to update event")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("Accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Content-Type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("Date time when enrollment begin"),
                                fieldWithPath("closeEnrollmentDateTime").description("Date time when enrollment close"),
                                fieldWithPath("beginEventDateTime").description("Date time when event begin"),
                                fieldWithPath("endEventDateTime").description("Date time when event end"),
                                fieldWithPath("location").description("Event location"),
                                fieldWithPath("basePrice").description("Base price of event"),
                                fieldWithPath("maxPrice").description("Max price of event"),
                                fieldWithPath("limitOfEnrollment").description("Max enrollment of event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("Response Content-Type")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("id").description("ID of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("Date time when enrollment begin"),
                                fieldWithPath("closeEnrollmentDateTime").description("Date time when enrollment close"),
                                fieldWithPath("beginEventDateTime").description("Date time when event begin"),
                                fieldWithPath("endEventDateTime").description("Date time when event end"),
                                fieldWithPath("location").description("Event location"),
                                fieldWithPath("basePrice").description("Base price of event"),
                                fieldWithPath("maxPrice").description("Max price of event"),
                                fieldWithPath("limitOfEnrollment").description("Max enrollment of event"),
                                fieldWithPath("free").description("True if event is free"),
                                fieldWithPath("offline").description("True if event has no location"),
                                fieldWithPath("eventStatus").description("Event status"),
                                fieldWithPath("_links.self.href").description("Link of self"),
                                fieldWithPath("_links.query-events.href").description("Link of query-events"),
                                fieldWithPath("_links.update-event.href").description("Link of update-event")
                                )
                ));
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
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception {
        // Given - 이벤트 30개
//        IntStream.range(0, 30).forEach(i -> {
//            this.generateEvent(i);
//        });
        IntStream.range(0, 30).forEach(this::generateEvent);

        // When
        this.mockMvc.perform(get("/api/events")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "name,DESC")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events"))
        ;
    }

    private void generateEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("Test event " + index)
                .build()
                ;

        this.eventRepository.save(event);
    }

}
