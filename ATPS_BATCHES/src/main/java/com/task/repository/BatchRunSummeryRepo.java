package com.task.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.task.entity.BatchRunSummeryEntity2;

@Repository
public interface BatchRunSummeryRepo  extends JpaRepository<BatchRunSummeryEntity2,Serializable>{

}
