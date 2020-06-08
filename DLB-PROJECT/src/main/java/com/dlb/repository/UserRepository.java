package com.dlb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dlb.entity.UserEntity;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
	
	@Query(value ="select uid from UserEntity where userName=:userName")
	public int getUidbyUsername(String userName);

	@Query(value="from UserEntity where email=:userName")
	public UserEntity getByUserName(String userName);
	
	@Query(value="from UserEntity where email=:email and password=:password")
	public UserEntity getByEmailAndPassword(String email,String password);
}
