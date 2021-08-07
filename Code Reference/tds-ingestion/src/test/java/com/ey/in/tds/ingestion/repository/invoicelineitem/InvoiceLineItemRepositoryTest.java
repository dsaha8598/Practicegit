package com.ey.in.tds.ingestion.repository.invoicelineitem;

import static org.junit.Assert.assertNotNull;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.util.TestUtil;


@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class InvoiceLineItemRepositoryTest {
  
  @Autowired
  InvoiceLineItemDAO invoiceLineItemRepository;
  
  @Test
  public void insert() { //TODO NEED TO CHANGE FOR SQL
    //assertNotNull(invoiceLineItemRepository.insert(TestUtil.getRandomInvoiceLineItem()));
  }
  
  @Test
  public void get() {  //TODO NEED TO CHANGE FOR SQL
   // InvoiceLineItem invoiceLineItem = invoiceLineItemRepository.insert(TestUtil.getRandomInvoiceLineItem());
  //  assertNotNull(invoiceLineItemRepository.findById(invoiceLineItem.getKey()));
  }
  
  @Test
  public void update() {   //TODO NEED TO CHANGE FOR SQL
/*    InvoiceLineItemDto invoiceLineItem = TestUtil.getRandomInvoiceLineItem();
    
    invoiceLineItemRepository.insert(invoiceLineItem);
    invoiceLineItem.setCreatedBy("Test User");
    invoiceLineItemRepository.insert(invoiceLineItem);
    assertEquals("Test User", invoiceLineItemRepository.findById(invoiceLineItem.getKey()).get().getCreatedBy());
 */
  }
  
  @Test
  public void getInvoiceLineItemsByAssessmentYearChallanMonthDeductoTan() {
    int year = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
    String deductorTan = TestUtil.TAN_NUMBER;
    InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
    first.setAssessmentYear(year);
    first.setDeductorMasterTan(deductorTan);
    first.setChallanMonth(month);
    first.setActive(true);
    first.setChallanPaid(false);
    InvoiceLineItem second = TestUtil.getRandomInvoiceLineItem();
    second.setAssessmentYear(year);
    second.setDeductorMasterTan(deductorTan);
    second.setChallanMonth(month);
    second.setActive(true);
    second.setChallanPaid(false);
    
  //  invoiceLineItemRepository.saveAll(Arrays.asList(first, second));
    
  //  assertTrue(invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year,
  //  		Arrays.asList(month), Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(false, 1, null)).getData().size() > 0);
  }
  
  @Test
  public void getAllInvoiceMismatches() {
    assertNotNull(invoiceLineItemRepository.getAllInvoiceMismatches("ABCDE1234F", 2020, 1));
  }
  
  @Test
  public void getInvoiceMismatchSummary() {
 //   assertNotNull(invoiceLineItemRepository.getInvoiceMismatchSummary(2020, 11, "ABCDE1234P", UUID.randomUUID(), "TYPE"));
  
 //   assertNotNull(invoiceLineItemRepository.getInvoiceMismatchSummary(2020, 11, "ABCDE1234P", null, "TYPE"));
  }
}
