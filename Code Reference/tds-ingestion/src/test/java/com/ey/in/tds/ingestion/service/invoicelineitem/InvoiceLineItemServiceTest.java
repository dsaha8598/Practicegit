package com.ey.in.tds.ingestion.service.invoicelineitem;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.util.TestUtil;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class InvoiceLineItemServiceTest {
  
  @Autowired
  InvoiceLineItemService invoiceLineItemService;

  
  @Test
  public void createAndGet() {   //TODO NEED TO CHANGE FOR SQL
 //   InvoiceLineItem invoiceLineItem = TestUtil.getRandomInvoiceLineItem();
  //  invoiceLineItemService.create(invoiceLineItem);
   // assertNotNull(invoiceLineItemService.get(invoiceLineItem.getKey()));
  }
  
  @Test
  public void getInvoiceLineItemByAssessmentYear() {
    int assessmentYear = new Random().nextInt(2000);
    int month = new Random().nextInt(12);
    String deductorTan = TestUtil.TAN_NUMBER;
    //TODO NEED TO CHANGE FOR SQL
  //  InvoiceLineItem invoiceLineItem = TestUtil.getRandomInvoiceLineItem();
  //  invoiceLineItem.getKey().setAssessmentYear(assessmentYear);
  //  invoiceLineItem.getKey().setDeductorTan(deductorTan);
  //  invoiceLineItemService.create(invoiceLineItem);
    assertTrue(invoiceLineItemService
      .getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(assessmentYear, month, Arrays.asList(deductorTan), false, false, Pagination.DEFAULT).size() > 0);
  }
  
	@Test
	public void getInvoiceCountResults() {

		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String fDate = format.format(monthStartDate);
		String lDate = format.format(monthEndDate);
		String tans = "AHOY00000J";
		String type = "INVOICE_PDF";
		//assertTrue(invoiceLineItemRepository.getPdfTransactionStatus(2019, type, tans, fDate, lDate) >= 0);
	}	
	
	
	@Test
	public void getResult() {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStartDate = calendar.getTime();
		calendar.add(Calendar.MONTH, 1);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date monthEndDate = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String fDate = format.format(monthStartDate);
		String lDate = format.format(monthEndDate);
		boolean isMismatch = true;
		String tans = "AHOY00000J";
		//assertTrue(invoiceLineItemRepository.getTdsCalculationStatus(2019, tans, fDate, lDate, isMismatch)>=0);
	}
	
  
}