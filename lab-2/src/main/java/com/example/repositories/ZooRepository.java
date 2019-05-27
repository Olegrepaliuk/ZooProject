package com.example.repositories;

import com.example.Zoo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface ZooRepository extends JpaRepository<Zoo, Integer> {
}
