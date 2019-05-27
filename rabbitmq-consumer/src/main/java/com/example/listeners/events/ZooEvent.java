package com.example.listeners.events;

import com.example.ZooMessage;
import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class ZooEvent extends ApplicationEvent {

    private ZooMessage msg;

    public ZooEvent(Object source, ZooMessage msg) {
        super(source);
        this.msg = msg;
    }

    public ZooMessage getMessage() {
        return msg;
    }



}
