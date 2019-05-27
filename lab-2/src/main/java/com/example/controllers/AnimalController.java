package com.example.controllers;

import com.example.Animal;
import com.example.Zoo;
import com.example.assemblers.AnimalResourcesAssembler;
import com.example.assemblers.ZooResourcesAssembler;
import com.example.exceptions.AnimalNotFoundException;
import com.example.exceptions.ValidationError;
import com.example.exceptions.ValidationErrorBuilder;
import com.example.exceptions.ZooNotFoundException;
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
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/animals")
public class AnimalController {

    @Autowired
    private AnimalService animalService;
    @Autowired
    ZooService zooService;
    private final AnimalResourcesAssembler assembler;
    private final ZooResourcesAssembler zooAssembler;

    public AnimalController(AnimalResourcesAssembler assembler, ZooResourcesAssembler zooResourcesAssembler) {
        this.assembler = assembler;
        this.zooAssembler = zooResourcesAssembler;
    }

    @GetMapping
    public Resources<Resource<Animal>> getAnimals() {
        List<Resource<Animal>> list = animalService.getAll().stream()
                .map(assembler::toResource)
                .collect(Collectors.toList());
        return new Resources<>(
                list,
                linkTo(methodOn(AnimalController.class).getAnimals()).withSelfRel()
        );
    }

    @GetMapping("/{animalId}")
    public ResponseEntity<ResourceSupport> getAnimal(@PathVariable Integer animalId) {
        Animal animal = animalService.getObjectById(animalId);
        return ResponseEntity.ok(assembler.toResource(animal));
    }

    @PostMapping
    public ResponseEntity<?> createAnimal(@Valid @RequestBody Animal newAnimal) throws URISyntaxException {
        Resource<Animal> resource = assembler.toResource(animalService.saveObject(newAnimal));
        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @PutMapping("/{animalId}")
    public ResponseEntity<?> updateAnimal(@Valid @RequestBody Animal updatedAnimal, @PathVariable Integer animalId) throws URISyntaxException {
        Animal updatedObj = animalService.updateObject(updatedAnimal, animalId);
        Resource<Animal> resource = assembler.toResource(updatedObj);
        return ResponseEntity
                .created(new URI(resource.getId().expand().getHref()))
                .body(resource);
    }

    @DeleteMapping("/{animalId}")
    public ResponseEntity<?> deleteAnimal(@PathVariable Integer animalId) {
        try {
            changeAnimalZoo(animalId, -2);
            animalService.deleteObject(animalId);
        } catch (Exception ex) {
            System.out.println("ATTENTION " + ex.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{animalId}/zoo")
    public ResponseEntity<ResourceSupport> getZooOfAnimal(@PathVariable Integer animalId) {
        Animal animal = animalService.getObjectById(animalId);
        if (animal.getZoo() == null)
            return ResponseEntity.noContent().build();
        Zoo zoo = zooService.getObjectById(animal.getZoo().getId());
        return  ResponseEntity
                .ok(zooAssembler.toResource(zoo));
    }


    @PostMapping("/{animalId}/zoo/{newZooId}")
    public Resources<Resource<Animal>> changeAnimalZoo(@PathVariable Integer animalId, @PathVariable Integer newZooId) {
        Animal animal = animalService.getObjectById(animalId);
                //.orElseThrow(() -> new AnimalNotFoundException(animalId));
        removeAnimalFromZoo(animal);
        return addAnimalToZoo(animal, newZooId);
    }

    private void removeAnimalFromZoo(Animal animal) {
        if (animal.getZoo() != null) {
            Zoo animalZoo = zooService.getObjectById(animal.getZoo().getId());
                    //.orElseThrow(() -> new IllegalArgumentException("Delete this zoo ID"));
            animalZoo.removeAnimal(animal);
            animalZoo.setAnimals(animalZoo.getAnimals());
            zooService.saveObject(animalZoo);
            animalService.saveObject(animal);
        }
    }

    private Resources<Resource<Animal>> addAnimalToZoo(Animal animal, Integer zooId) {
        Animal anim = animalService.getObjectById(animal.getId());
                //orElseThrow(() -> new IllegalArgumentException("Something wrong with animal: " + animal));
        if (zooId == -2)
            return null;
        Zoo zoo = zooService.getObjectById(zooId);
                //.orElseThrow(() -> new ZooNotFoundException(zooId));
        anim.setZoo(zoo);
        zoo.addAnimal(anim);
        zoo.setAnimals(zoo.getAnimals());
        zooService.saveObject(zoo);
        animalService.saveObject(anim);
        return getAnimalsOfZoo(zooId);
    }

    private Resources<Resource<Animal>> getAnimalsOfZoo(Integer zooId) {
        Zoo zoo = zooService.getObjectById(zooId);
        List<Resource<Animal>> list = zoo.getAnimals()
                .stream()
                .map(assembler::toResource)
                .collect(Collectors.toList());
        return new Resources<>(
                list,
                linkTo(methodOn(ZooController.class).getZoo(zooId)).withSelfRel(),
                linkTo(methodOn(AnimalController.class).getAnimals()).withSelfRel()
        );
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

