package com.example.repositories;

import com.example.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface AnimalRepository extends JpaRepository<Animal, Integer> {
}
