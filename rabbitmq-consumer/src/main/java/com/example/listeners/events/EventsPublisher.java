package com.example.listeners.events;

import com.example.ZooMessage;
import com.example.AnimalMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class EventsPublisher implements ApplicationEventPublisherAware {

    protected ApplicationEventPublisher appPublisher;

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher appPublisher) {

        this.appPublisher = appPublisher;
    }

    public void publishZoo(ZooMessage message)
    {
        ZooEvent evt = new ZooEvent(this, message);
        appPublisher.publishEvent(evt);
    }

    public void publishAnimal(AnimalMessage message)
    {
        AnimalEvent evt = new AnimalEvent(this, message);
        appPublisher.publishEvent(evt);
    }
}
