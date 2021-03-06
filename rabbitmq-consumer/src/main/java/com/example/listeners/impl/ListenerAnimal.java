package com.example.listeners.impl;

import com.example.AnimalMessage;
import com.example.listeners.RabbitListener;
import com.example.listeners.events.EventsPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerAnimal implements RabbitListener<AnimalMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerAnimal.class);

    @Autowired
    private EventsPublisher publisher;

    @Override
    public void receiveMessage(AnimalMessage message) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            LOGGER.debug("Receive Message Animal: \n"+json);

            publisher.publishAnimal(message);

        } catch (JsonProcessingException e) {
            LOGGER.error("Error: ", e);
        }

    }



}
