package com.example.assemblers;
import com.example.Animal;
import com.example.controllers.AnimalController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
@Component
public class AnimalResourcesAssembler implements ResourceAssembler<Animal, Resource<Animal>> {
    @Override
    public Resource<Animal> toResource(Animal animal) {
        return new Resource<>(
                animal,
                linkTo(methodOn(AnimalController.class).getAnimal(animal.getId())).withSelfRel(),
                linkTo(methodOn(AnimalController.class).getAnimals()).withRel("animals")
        );
    }
}
