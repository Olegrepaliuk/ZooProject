package com.example;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "animal")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(allowGetters = true)
public class Animal {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_id")
    private int id;

    @NotNull
    @Column(name = "animal_name")
    @NotBlank(message ="NAME can not be blank")
    private String name;

    @NotNull(message = "Age can not be empty")
    private int age;

    @NotBlank(message = "Each animal has type")
    private String type;

    public int getId() {
        return id;
    }
    public void setId(Integer id){
        this.id = id;
    }

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "zoo_id")
    private Zoo zoo;

    private boolean isDeleted;

    public Animal()
    {
    }
    public Animal(@NotBlank String name){
        this.name = name;
    }

    public Animal(@NotBlank String name, @NotBlank int age, @NotBlank String type){
        this(name);
        setType(type);
        setAge(age);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Zoo getZoo() {
        return zoo;
    }
    public void setZoo(Zoo zoo) {
        this.zoo = zoo;
    }

    public void delete(){
        isDeleted = true;
    }
    public boolean getDeleted(){
        return isDeleted;
    }
}

