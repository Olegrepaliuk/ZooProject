package com.example;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "zoos")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(allowGetters = true)
public class Zoo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zoo_id")
    private int id;

    @NotNull(message = "Zoo needs name")
    private String name;

    @NotNull(message = "Zoo has a year of foundation")
    private int foundationYear;

    private boolean isDeleted;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<Animal> animals = new ArrayList<>();

    public Zoo(){

    }
    public Zoo(@NotNull String name){
        setName(name);
        foundationYear = 2018;
    }
    //public void addAnimal(Animal animal){
    //animals.add(animal);
    //}

    public int getId() {
        return id;
    }
    public void setId(Integer id){
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getFoundationYear() {
        return foundationYear;
    }
    public void setFoundationYear(Integer year)
    {
        if(year < 0 || year > 2019)
            System.out.println("Incorrect year of foundation");
        else
            this.foundationYear = year;
    }

    public List<Animal> getAnimals(){
        return animals;
    }
    public void setAnimals(List<Animal> animals){
        this.animals = animals;
    }
    public boolean removeAnimal(Animal animal){
        return animals.contains(animal) && animals.remove(animal);

    }
    public boolean removeAnimal(Integer id){
        for(int i = 0; i < animals.size(); ++i){
            if(animals.get(i).getId() == id)
                return animals.remove(animals.get(i));
        }
        return false;
    }

    public List<Animal> addAnimal(Animal animal){
        if(animals.contains(animal))
            throw new RuntimeException("Animal already in this zoo!");
        animals.add(animal);
        return animals;
    }

    public void delete(){
        isDeleted = true;
    }
    public boolean getDeleted(){
        return isDeleted;
    }
}

