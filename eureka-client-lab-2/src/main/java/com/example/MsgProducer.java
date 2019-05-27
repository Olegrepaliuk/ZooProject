package com.example;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
public class MsgProducer {

    @Autowired
    @Qualifier("rabbitTemplateZoo")
    private RabbitTemplate rabbitZoo;

    @Autowired
    @Qualifier("rabbitTemplateAnimal")
    private RabbitTemplate rabbitAnimal;

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgProducer.class);

    public void sendZooMsg(ZooMessage msg)
    {
        try {
            LOGGER.debug("<<<<<< SENDING MESSAGE");
            rabbitZoo.convertAndSend(msg);
            LOGGER.debug(MessageFormat.format("MESSAGE SENT TO {0} >>>>>>", rabbitZoo.getRoutingKey()));

        } catch (AmqpException e) {
            LOGGER.error("Error sending Customer: ",e);
        }
    }

    public void sendAnimalMsg(AnimalMessage msg)
    {
        try {
            LOGGER.debug("<<<<< SENDING MESSAGE");
            rabbitAnimal.convertAndSend(msg);
            LOGGER.debug(MessageFormat.format("MESSAGE SENT TO {0} >>>>>>", rabbitAnimal.getRoutingKey()));
        } catch (AmqpException e) {
            LOGGER.error("Error sending Shop: ",e);
        }
    }

    public ObjectNode info()
    {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode root = factory.objectNode();
        root.put("host", rabbitZoo.getConnectionFactory().getHost());
        root.put("port", rabbitZoo.getConnectionFactory().getPort());
        root.put("Zoo UUID", rabbitZoo.getUUID());
        root.put("Animal UUID", rabbitAnimal.getUUID());
        root.put("queueZoo", rabbitZoo.getRoutingKey());
        root.put("queueAnimal", rabbitAnimal.getRoutingKey());

        return root;
    }
}
