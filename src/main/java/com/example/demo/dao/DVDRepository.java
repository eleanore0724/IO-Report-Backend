package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.DVD;

@Repository
public interface DVDRepository extends JpaRepository<DVD, Integer>{

}
