package com.ey.in.tds.ingestion.web.rest.batchupload;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.ingestion.util.TestUtil;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BatchUploadMultiTenantControllerE2ETest {
  
  TestRestTemplate restTemplate;
  HttpHeaders headers;
  
  @Before
  public void init(){
    restTemplate = new TestRestTemplate();
    headers = TestUtil.getMandatoryHeaders();
    headers.add("X-TENANT-ID", "client1");
  }
  
  
  @Test
  public void testInvalidTenantId() {
    headers.set("X-TENANT-ID", "invalid-tenant");
    BatchUpload randomBatchUpload = TestUtil.getRandomBatchUpload();
    randomBatchUpload.setBatchUploadID(null);
    assertNull(randomBatchUpload.getBatchUploadID());
  
    HttpEntity<BatchUpload> entity = new HttpEntity<>(randomBatchUpload, headers);
  
    ResponseEntity<BatchUpload> response = restTemplate.exchange(
      TestUtil.getE2EURL("/batchupload"), HttpMethod.POST, entity, BatchUpload.class);
    assertEquals(500, response.getStatusCodeValue());
  }
  
  @Test
  public void createBatchUpload() {
    
    BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
    
    createAndAssert(batchUpload);
    
  }
  
  private BatchUpload createAndAssert(BatchUpload batchUpload) {
    // While random value can be used in other tests, the REST test should not pass id values.
    // Its the server that will generate it
    batchUpload.setBatchUploadID(null);
    assertNull(batchUpload.getBatchUploadID());
  
    HttpEntity<BatchUpload> entity = new HttpEntity<>(batchUpload, headers);
  
    ResponseEntity<BatchUpload> response = restTemplate.exchange(
      TestUtil.getE2EURL("/batchupload"), HttpMethod.POST, entity, BatchUpload.class);
  
    assertEquals(200, response.getStatusCodeValue());
  
    BatchUpload created = response.getBody();
  
    assertNotNull(created.getBatchUploadID());
    return created;
  }
  
  private BatchUpload createAndAssert() {
    BatchUpload batchUpload = TestUtil.getRandomBatchUpload();
    return createAndAssert(batchUpload);
  }
  
  @Test
  public void updateBatchUpload() {
    
    BatchUpload batchUpload = createAndAssert();
    
//    batchUpload.setFailedCount(1000);
  
    HttpEntity<BatchUpload> putEntity = new HttpEntity<BatchUpload>(batchUpload, headers);
  
    UriComponentsBuilder builder = getUriComponentBuilder(batchUpload);
  
    assertNotNull(builder);
    
    ResponseEntity<BatchUpload> putResponse = restTemplate.exchange(
      TestUtil.getE2EURL("/batchupload"), HttpMethod.PUT, putEntity, BatchUpload.class);
  
    assertEquals(200, putResponse.getStatusCodeValue());
  
    BatchUpload created = putResponse.getBody();
  
    assertNotNull(created.getBatchUploadID());
    
//    assertEquals(1000, putResponse.getBody().getFailedCount());
    
  }
  
  private ResponseEntity<BatchUpload> getBatchUploadResponseEntity(BatchUpload batchUpload) {
    
    UriComponentsBuilder builder = getUriComponentBuilder(batchUpload);
    
    return restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), BatchUpload.class);
  }
  
  private UriComponentsBuilder getUriComponentBuilder(BatchUpload batchUpload) {
    return UriComponentsBuilder.fromHttpUrl(TestUtil.getE2EURL("/batchupload"))
      .queryParam("assessmentYear", batchUpload.getAssessmentYear())
      .queryParam("month", batchUpload.getAssessmentMonth())
      .queryParam("type", batchUpload.getUploadType())
      .queryParam("status", batchUpload.getStatus())
      .queryParam("sha256sum", batchUpload.getSha256sum())
      .queryParam("id", batchUpload.getBatchUploadID());
  }
  
  @Test
  public void getBatchUpload() {
    
    BatchUpload batchUpload = createAndAssert();
    
    ResponseEntity<BatchUpload> getResponse = getBatchUploadResponseEntity(batchUpload);
    
    assertEquals(200, getResponse.getStatusCodeValue());
    
  }
  
  
  @Test
  public void deleteBatchUpload() {
    
    BatchUpload batchUpload = createAndAssert();
    
    HttpEntity<BatchUpload> deleteEntity = new HttpEntity<BatchUpload>(batchUpload, headers);
    
    UriComponentsBuilder builder = getUriComponentBuilder(batchUpload);
    
    ResponseEntity<Void> deleteResponse = restTemplate.exchange(
      builder.toUriString(),
      HttpMethod.DELETE, deleteEntity, Void.class);
    
    assertEquals(200, deleteResponse.getStatusCodeValue());
    
  }
  
}
