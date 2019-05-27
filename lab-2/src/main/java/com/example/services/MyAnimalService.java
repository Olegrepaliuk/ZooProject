package com.example.services;

import com.example.Animal;
import com.example.exceptions.AnimalNotFoundException;
import com.example.repositories.AnimalRepository;
import lombok.experimental.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MyAnimalService implements AnimalService {
    @Autowired
    private AnimalRepository animalRepository;

    @Override
    public List<Animal> getAll() {
        List<Animal> allAnimals = animalRepository.findAll();
        List<Animal> notDeletedAnimals = new ArrayList<>();
        for (Animal animal : allAnimals) {
            if(!animal.getDeleted())
                notDeletedAnimals.add(animal);
        }
        return notDeletedAnimals;
    }

    @Override
    public Animal getObjectById(Integer integer) {
        Optional<Animal> foundAnimal = animalRepository.findById(integer);
        if(foundAnimal.isPresent()){
            if (!foundAnimal.get().getDeleted())
                return foundAnimal.get();
        }
        throw new AnimalNotFoundException(integer);
    }

    @Override
    public Animal saveObject(Animal newObject) {
        return animalRepository.save(newObject);
    }

    @Override
    public void deleteObject(Integer integer) {
        Animal foundAnimal = animalRepository.getOne(integer);
        if (foundAnimal.getDeleted())
            throw new AnimalNotFoundException(foundAnimal.getId());
        foundAnimal.delete();
        animalRepository.save(foundAnimal);
    }

    @Override
    public Animal updateObject(Animal newObject, Integer integer) {
        Optional<Animal> foundAnimal = animalRepository.findById(integer);
        if (foundAnimal.get().getDeleted())
            throw new AnimalNotFoundException(integer);
        if(foundAnimal.isPresent())
            foundAnimal
                    .map(animal -> {
                        animal.setName(newObject.getName());
                        animal.setId(newObject.getId());
                        animal.setAge(newObject.getAge());
                        animal.setType(newObject.getType());
                        return animalRepository.save(animal);
                    })
                    .orElseGet(() -> {
                        newObject.setId(integer);
                        return animalRepository.save(newObject);
                    });

        throw new AnimalNotFoundException(integer);
    }
}
