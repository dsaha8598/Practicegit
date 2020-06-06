package com.dlb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dlb.entity.UserEntity;
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer> {
	
	@Query(value ="select uid from UserEntity where userName=:userName")
	public int getUidbyUsername(String userName);

	
}
