package com.udajahaja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udajahaja.entity.Coupons;

@Repository
public interface CouponRepository extends JpaRepository<Coupons, Integer>{

}
