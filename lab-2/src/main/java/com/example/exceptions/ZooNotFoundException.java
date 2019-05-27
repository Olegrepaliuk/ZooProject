package com.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ZooNotFoundException extends RuntimeException{
    public ZooNotFoundException(Integer id){
        super("Could not find zoo with ID: " + id);
    }
}
