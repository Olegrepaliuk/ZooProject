package com.example.listeners.events;

import com.example.AnimalMessage;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class AnimalEvent extends ApplicationEvent {

    private AnimalMessage msg;

    public AnimalEvent(Object source, AnimalMessage msg) {
        super(source);
        this.msg = msg;
    }

    public AnimalMessage getMessage() {
        return msg;
    }

}
