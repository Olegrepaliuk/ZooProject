package com.example.listeners.impl;

import com.example.ZooMessage;
import com.example.listeners.RabbitListener;
import com.example.listeners.events.EventsPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerZoo implements RabbitListener<ZooMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerZoo.class);

    @Autowired
    private EventsPublisher publisher;

    @Override
    public void receiveMessage(ZooMessage message) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
            LOGGER.debug("Receive Message Zoo: \n"+json);

            publisher.publishZoo(message);

        } catch (JsonProcessingException e) {
            LOGGER.error("Error: ", e);
        }

    }
}
