package com.task.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.task.entity.UserMasterEntity;

@Repository
public interface UserMasterRepo extends JpaRepository<UserMasterEntity,Serializable> {

	//@Query("select userId from")
	
}
