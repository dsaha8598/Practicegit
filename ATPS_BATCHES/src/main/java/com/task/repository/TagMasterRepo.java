package com.task.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.task.entity.TagMasetrEntity;

@Repository
public interface TagMasterRepo extends JpaRepository<TagMasetrEntity,Serializable>{

	@Query(value="select userId from TagMasetrEntity where tagId=:tagId")
	public Integer getUserId(Integer tagId);
	
}
