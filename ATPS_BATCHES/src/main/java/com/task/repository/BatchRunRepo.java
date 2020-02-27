package com.task.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.task.entity.Batchrunentity;

@Repository
public interface BatchRunRepo extends JpaRepository<Batchrunentity,Serializable>{

}
