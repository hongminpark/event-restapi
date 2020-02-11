package com.navercorp.restapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator {

    public void validate(EventDto eventDto, Errors errors) {
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0) {
            // Field Error
            errors.rejectValue("basePrice", "wrongValue", "BasePrice is invalid");
            errors.rejectValue("maxPrice", "wrongValue", "MaxPrice is invalid");

            // Global Error
            errors.reject("wrongPrices", "Price is invalid");
        }

        if (eventDto.getEndEventDateTime().isBefore(eventDto.getBeginEventDateTime()) ||
                eventDto.getEndEventDateTime().isBefore(eventDto.getCloseEnrollmentDateTime()) ||
                eventDto.getEndEventDateTime().isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue("endEventDateTime", "wrongValue", "EndEventDateTime is invalid");
        }
        // TODO beginEventDateTime
        // TODO CloseEnrollmentDateTime
    }
}
