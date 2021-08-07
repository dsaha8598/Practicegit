package com.ey.in.tds.ingestion.repository.fieldspecification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class FieldSpecificationRepositoryTest {
 
  
  @Test  //TODO NEED TO CHANGE FOR SQL
  public void insert() {
   // assertNotNull(fieldSpecificationRepository.insert(TestUtil.getRandomFieldSpecification()));
  }
  
  @Test  //TODO NEED TO CHANGE FOR SQL
  public void get() {
    //FieldSpecification fieldSpecification = fieldSpecificationRepository.insert(TestUtil.getRandomFieldSpecification());
   // assertNotNull(fieldSpecificationRepository.findById(fieldSpecification.getKey()));
  }
  
  @Test   //TODO NEED TO CHANGE FOR SQL
  public void update() {
    //FieldSpecification fieldSpecification = TestUtil.getRandomFieldSpecification();
    
    //fieldSpecificationRepository.insert(fieldSpecification);
    //fieldSpecification.setCreatedBy("Test User");
   // fieldSpecificationRepository.insert(fieldSpecification);
   // assertEquals("Test User", fieldSpecificationRepository.findById(fieldSpecification.getKey()).get().getCreatedBy());
  }
  
}
