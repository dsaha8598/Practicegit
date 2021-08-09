package com.ey.in.tds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.PersonResponsible;

@Repository
public interface PersonResponsibleRepository extends JpaRepository<PersonResponsible, Long>{

}
