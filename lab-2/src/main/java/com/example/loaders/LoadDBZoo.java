package com.example.loaders;


import com.example.Zoo;
import com.example.Zoo;
import com.example.repositories.ZooRepository;
import com.example.repositories.ZooRepository;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class LoadDBZoo {
    /*
    @Bean
    public CommandLineRunner initDbZoo(ZooRepository repository){
        return args ->{
            List<Zoo> checkList = repository.findAll();
            if(checkList.isEmpty()){
                log.info("Preloading zoo " + repository.save((new Zoo("Kiev zoo"))));
                log.info("Preloading zoo " + repository.save(new Zoo("Ascaniya-Nova")));
            }
            else{
                log.info("Current rows in table zoos: ");
                repository.findAll().forEach(zoo -> log.info(zoo.getId() + ":" + zoo.getName()));
            }
        };
    }
    */
}
