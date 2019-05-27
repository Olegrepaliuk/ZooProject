package com.example.loaders;


import com.example.Animal;
import com.example.Zoo;
import com.example.repositories.AnimalRepository;
import com.example.repositories.ZooRepository;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class LoadDBAnimal {
    /*
    @Bean
    public CommandLineRunner initDbAnimal(AnimalRepository repository){
        return args ->{
            List<Animal> checkList = repository.findAll();
            if(checkList.isEmpty()){
                log.info("Preloading animal " + repository.save((new Animal("Milka", 10, "Cow"))));
                log.info("Preloading animal " + repository.save(new Animal("Barbos", 8, "Dog")));
            }
            else{
                log.info("Current rows in table animals: ");
                repository.findAll().forEach(animal -> log.info(animal.getId() + ":" + animal.getName() + "from " + animal.getZoo()));
            }
        };
    }
    */
}
