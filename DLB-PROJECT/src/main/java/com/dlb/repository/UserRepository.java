package com.dlb.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.dlb.entity.UserEntity;
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer> {

	
}
