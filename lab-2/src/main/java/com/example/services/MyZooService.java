package com.example.services;

import com.example.Zoo;
import com.example.exceptions.ZooNotFoundException;
import com.example.repositories.ZooRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MyZooService implements ZooService {
    @Autowired
    private ZooRepository zooRepository;

    @Override
    public List<Zoo> getAll() {
        List<Zoo> allZoos = zooRepository.findAll();
        List<Zoo> notDeletedZoo = new ArrayList<>();
        for( Zoo zoo : allZoos){
            if (!zoo.getDeleted())
                notDeletedZoo.add(zoo);
        }
        return notDeletedZoo;
    }

    @Override
    public Zoo getObjectById(Integer integer)
    {
        Optional<Zoo> foundZoo = zooRepository.findById(integer);

        if (foundZoo.isPresent()) {
            if (!foundZoo.get().getDeleted())
                return foundZoo.get();
        }
        throw new ZooNotFoundException(integer);
    }

    @Override
    public Zoo saveObject(Zoo newObject) {
        return zooRepository.save(newObject);
    }

    @Override
    public void deleteObject(Integer integer) {
        Zoo foundZoo = zooRepository.getOne(integer);

        if (foundZoo.getDeleted())
            throw new ZooNotFoundException(foundZoo.getId());

        foundZoo.delete();
        zooRepository.save(foundZoo);
    }

    @Override
    public Zoo updateObject(Zoo newObject, Integer integer) {
        Optional<Zoo> foundZoo = zooRepository.findById(integer);
        if (foundZoo.get().getDeleted())
            throw new ZooNotFoundException(integer);

        if (foundZoo.isPresent()) {
            foundZoo
                    .map(zoo -> {
                        zoo.setName(newObject.getName());
                        zoo.setAnimals(newObject.getAnimals());
                        zoo.setFoundationYear(newObject.getFoundationYear());
                        zoo.setId(newObject.getId());
                        return zooRepository.save(zoo);
                    })
                    .orElseGet(() -> {
                        newObject.setId(integer);
                        return zooRepository.save(newObject);
                    });
        }
        throw new ZooNotFoundException(integer);
    }
}
