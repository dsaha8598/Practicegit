package com.ey.in.tds.ingestion.repository.advance;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class AdvanceRepositoryTest {
  
  
  @Autowired
  AdvanceDAO advanceRepository;
  
  @Test
  public void getAllAdvanceMismatches() {
    Assert.assertNotNull(advanceRepository.getAllAdvanceMismatches("ABCDE1234F", 2020, 1));
  }
}