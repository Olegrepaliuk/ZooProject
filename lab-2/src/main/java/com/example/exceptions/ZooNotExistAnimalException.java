package com.example.exceptions;

import com.example.Animal;
import com.example.Zoo;


public class ZooNotExistAnimalException extends RuntimeException {
    public ZooNotExistAnimalException(Zoo zoo, Animal animal){
        super("Zoo: " + zoo.toString() + " does not contain animal: " + animal.toString());
    }
    public ZooNotExistAnimalException(Integer zooId, Integer animalId) {
        super("Zoo with id: " + zooId + " does not contain animal with id: " + animalId);
    }
}
