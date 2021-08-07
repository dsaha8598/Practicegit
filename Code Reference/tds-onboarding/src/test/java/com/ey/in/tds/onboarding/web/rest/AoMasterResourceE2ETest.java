package com.ey.in.tds.onboarding.web.rest;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AoMasterResourceE2ETest {
  
/*  
  TestRestTemplate restTemplate = new TestRestTemplate();
  HttpHeaders headers = new HttpHeaders();
  
  private AoMaster createAndAssert(AoMasterDTO aoMasterDTO) {
    headers.add("TAN-NUMBER", aoMasterDTO.getDeductorTan());
    HttpEntity<AoMasterDTO> entity = new HttpEntity<>(aoMasterDTO, headers);
    
    ResponseEntity<AoMaster> response = restTemplate.exchange(
      TestUtil.getE2EURL("/aomaster"), HttpMethod.POST, entity, AoMaster.class);
    
    assertEquals(200, response.getStatusCodeValue());
    
    AoMaster created = response.getBody();
    
    assertNotNull(created.getKey().getId());
    return created;
  }
  
  @Test
  public void create() {
    AoMasterDTO aoMasterDTO = TestUtil.getRandomAoMasterDTO();
    
    createAndAssert(aoMasterDTO);
  }  */
}