package com.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AnimalNotFoundException extends RuntimeException {
    public AnimalNotFoundException(Integer id){
        super("Could not find animal with ID: " + id);
    }
}
