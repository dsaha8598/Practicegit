package com.ey.in.tds.ingestion.service.fieldspecification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class FieldSpecificationServiceTest {

  @Autowired
  FieldSpecificationService fieldSpecificationService;

  @Test
//TODO NEED TO CHANGE FOR SQL
  public void createAndGet() {
  /*  FieldSpecification fieldSpecification = TestUtil.getRandomFieldSpecification();
    fieldSpecificationService.create(fieldSpecification);
    Assert.assertNotNull(fieldSpecificationService.get(fieldSpecification.getKey()));   */
  }

}