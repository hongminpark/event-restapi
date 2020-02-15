package com.navercorp.restapi.events;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.ControllerLinkBuilder.linkTo;

public class EventResource extends EntityModel<Event> {

    public EventResource(Event event, Link... links) {
        super(event, links);
        //Add Self Link
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }

}