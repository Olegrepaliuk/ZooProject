package com.example.controllers;

import com.example.Animal;
import com.example.Zoo;
import com.example.assemblers.AnimalResourcesAssembler;
import com.example.assemblers.ZooResourcesAssembler;
import com.example.exceptions.*;
import com.example.services.AnimalService;
import com.example.services.ZooService;
import lombok.experimental.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/zoos")
public class ZooController {
    @Autowired
    private ZooService zooService;
    @Autowired
    private AnimalService animalService;

    private final ZooResourcesAssembler assembler;
    private final AnimalResourcesAssembler animalAssembler;

    public ZooController(ZooResourcesAssembler assembler, AnimalResourcesAssembler animalResourcesAssembler) {
        this.assembler = assembler;
        this.animalAssembler = animalResourcesAssembler;
    }

    @GetMapping
    public Resources<Resource<Zoo>> getZoos() {
        List<Resource<Zoo>> list = zooService.getAll().stream()
                .map(assembler::toResource)
                .collect(Collectors.toList());
        return new Resources<>(
                list,
                linkTo(methodOn(ZooController.class).getZoos()).withSelfRel()
        );
    }

    @GetMapping(value = "/{zooId}", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ResourceSupport> getZoo(@PathVariable Integer zooId) {
        Zoo zoo = zooService.getObjectById(zooId);
        return ResponseEntity.ok(assembler.toResource(zoo));
    }

    @PostMapping
    public ResponseEntity<?> createZoo(@Valid @RequestBody Zoo newZoo) throws URISyntaxException {
        Resource<Zoo> resource = assembler.toResource(zooService.saveObject(newZoo));
        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @PutMapping(value = "/{zooId}", consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> updateZoo(@Valid @RequestBody Zoo updatedZoo, @PathVariable Integer zooId) throws URISyntaxException {
        Zoo updatedObj = zooService.updateObject(updatedZoo, zooId);

        Resource<Zoo> resource = assembler.toResource(updatedObj);
        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @DeleteMapping("/{zooId}/delAll")
    public ResponseEntity<?> deleteZooAndAnimals(@PathVariable Integer zooId) {
        zooService.deleteObject(zooId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{zooId}")
    public ResponseEntity<?> deleteZooAndSaveAnimals(@PathVariable Integer zooId) {
        Zoo zoo = zooService.getObjectById(zooId);
        Animal[] animals = new Animal[zoo.getAnimals().size()];
        animals = zoo.getAnimals().toArray(animals);
        for (Animal stud :
                animals) {
            Animal animal = animalService.getObjectById(stud.getId());
            animal.setZoo(null);
            zoo.removeAnimal(animal);
            zoo.setAnimals(zoo.getAnimals());
            animalService.saveObject(animal);
            zooService.saveObject(zoo);
        }
        return deleteZooAndAnimals(zooId);
    }

    @GetMapping("/{zooId}/animals")
    public Resources<Resource<Animal>> getAnimalsOfZoo(@PathVariable Integer zooId) {
        Zoo zoo = zooService.getObjectById(zooId);
        List<Resource<Animal>> list = zoo.getAnimals()
                .stream()
                .map(animalAssembler::toResource)
                .collect(Collectors.toList());
        return new Resources<>(
                list,
                linkTo(methodOn(ZooController.class).getZoo(zooId)).withSelfRel(),
                linkTo(methodOn(AnimalController.class).getAnimals()).withSelfRel()
        );
    }

    @DeleteMapping("/{zooId}/animals/{animalId}")
    public Resources<Resource<Animal>> removeAnimalFromZoo(@PathVariable Integer zooId, @PathVariable Integer animalId) {
        Animal animal = animalService.getObjectById(animalId);
                //.orElseThrow(() -> new AnimalNotFoundException(animalId));
        Zoo zoo = zooService.getObjectById(zooId);
                //.orElseThrow(() -> new ZooNotFoundException(zooId));
        if (!zoo.getAnimals().contains(animal))
            throw new ZooNotHaveAnimalsException(zoo, animal);
        else {
            animal.setZoo(null);
            zoo.removeAnimal(animal);
            zoo.setAnimals(zoo.getAnimals());
            zooService.saveObject(zoo);
            animalService.saveObject(animal);
        }
        return getAnimalsOfZoo(zooId);
    }

    /*
    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ValidationError handleException(MethodArgumentNotValidException exception) {
        return createValidationError(exception);
    }

    private ValidationError createValidationError(MethodArgumentNotValidException e) {
        return ValidationErrorBuilder.fromBindingErrors(e.getBindingResult());
    }
    */
}