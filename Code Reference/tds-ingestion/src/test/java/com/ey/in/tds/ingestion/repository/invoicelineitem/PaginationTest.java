package com.ey.in.tds.ingestion.repository.invoicelineitem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringRunner;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.ingestion.TdsIntegrationTest;
import com.ey.in.tds.ingestion.util.TestUtil;

@TdsIntegrationTest
@RunWith(SpringRunner.class)
public class PaginationTest {

	@Test
	public void defaultPagination() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

	//	invoiceLineItemRepository.saveAll(invoiceLineItems);

	//	assertEquals("Result should contain default pagination size.", Pagination.DEFAULT_PAGE_SIZE,
	//			invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
	//					Arrays.asList(TestUtil.TAN_NUMBER), false, false, Pagination.DEFAULT).getData().size());
	}

	@Test
	public void noPagination() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

		//invoiceLineItemRepository.saveAll(invoiceLineItems);

	//	assertEquals("Result should contain all rows.", 11,
	//			invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
	//					Arrays.asList(TestUtil.TAN_NUMBER), false, false, Pagination.UNPAGED).getData().size());
	}

	@Test
	public void forwardFewPages() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

		//invoiceLineItemRepository.saveAll(invoiceLineItems);

//		assertEquals("Result should contain remaining 2 items.", 2,
	//			invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
	//					Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(4, 3)).getData().size());
	}

	@Test
	public void usingPagingState() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 14; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

	//	invoiceLineItemRepository.saveAll(invoiceLineItems);

	/*	PagedData<InvoiceLineItem> firstPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(false, 5, null));

		assertEquals("Result should contain specified number of rows.", 5, firstPage.getData().size());

		PagedData<InvoiceLineItem> secondPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, firstPage.getPageSize(), firstPage.getPageStates()));

		assertEquals("Result should contain specified number of rows.", 5, secondPage.getData().size());

		PagedData<InvoiceLineItem> thirdPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, secondPage.getPageSize(), secondPage.getPageStates()));

		assertEquals("Result should contain remaining 1 item.", 4, thirdPage.getData().size());
	*/}

	@Test
	public void navigatePage1Page2Page1Page2Page3() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

		//invoiceLineItemRepository.saveAll(invoiceLineItems);

		// Get first page
	/*	PagedData<InvoiceLineItem> firstPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(false, 2, null));
		List<UUID> firstPageIds = new ArrayList<>();
		firstPage.getData().stream().forEach(ili -> firstPageIds.add(ili.getKey().getId()));
		assertEquals("Result should contain specified number of rows.", 2, firstPage.getData().size());

		// Get second Page
		PagedData<InvoiceLineItem> secondPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, firstPage.getPageSize(), firstPage.getPageStates()));
		List<UUID> secondPageIds = new ArrayList<>();
		secondPage.getData().stream().forEach(ili -> secondPageIds.add(ili.getKey().getId()));
		assertEquals("Result should contain specified number of rows.", 2, secondPage.getData().size());

		// Get prev page
		firstPage = invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
				Arrays.asList(TestUtil.TAN_NUMBER), false,
			false, new Pagination(false, secondPage.getPageSize(), secondPage.getPageStates()));

		assertEquals("Result should contain specified number of rows.", 2, firstPage.getData().size());
		List<UUID> firstSetofIds = new ArrayList<>();
		firstPage.getData().stream().forEach(ili -> firstSetofIds.add(ili.getKey().getId()));

		assertTrue("Result should contain same ids that of first page", firstPageIds.equals(firstSetofIds));

		// Get next page
		secondPage = invoiceLineItemRepository.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
				Arrays.asList(TestUtil.TAN_NUMBER), false,
			false, new Pagination(true, firstPage.getPageSize(), firstPage.getPageStates()));

		assertEquals("Result should contain specified number of rows.", 2, secondPage.getData().size());
		List<UUID> secondSetOfIds = new ArrayList<>();
		secondPage.getData().stream().forEach(ili -> secondSetOfIds.add(ili.getKey().getId()));

		assertTrue("Result should contain same ids that of first page", secondPageIds.equals(secondSetOfIds));

		// Get next page
		PagedData<InvoiceLineItem> thirdPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, secondPage.getPageSize(), secondPage.getPageStates()));

		assertEquals("Result should contain remaining 1 item.", 1, thirdPage.getData().size());

	*/}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void navigatePage1Page1() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

		//invoiceLineItemRepository.saveAll(invoiceLineItems);

		// Get first page
/*		PagedData<InvoiceLineItem> firstPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(false, 2, null));
		assertEquals("Result should contain specified number of rows.", 2, firstPage.getData().size());

		// Get prev page
		PagedData<InvoiceLineItem> prevPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(false, firstPage.getPageSize(), firstPage.getPageStates()));
		assertTrue(prevPage != null);
	*/}

	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void navigateLastNext() {
		int year = new Random().nextInt(2000);
		int month = new Random().nextInt(12);
		String deductorTan = TestUtil.TAN_NUMBER;
		List<InvoiceLineItem> invoiceLineItems = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			InvoiceLineItem first = TestUtil.getRandomInvoiceLineItem();
			first.setAssessmentYear(year);
			first.setDeductorMasterTan(deductorTan);
			first.setChallanMonth(month);
			first.setActive(true);
			first.setChallanPaid(false);
			invoiceLineItems.add(first);
		}

		//invoiceLineItemRepository.saveAll(invoiceLineItems);

		// Get first page
	/*	PagedData<InvoiceLineItem> firstPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false, false, new Pagination(false, 2, null));
		assertEquals("Result should contain specified number of rows.", 2, firstPage.getData().size());
		// Get second page
		PagedData<InvoiceLineItem> secondPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, firstPage.getPageSize(), firstPage.getPageStates()));
		assertEquals("Result should contain specified number of rows.", 1, secondPage.getData().size());
		// Get third page
		PagedData<InvoiceLineItem> thirdPage = invoiceLineItemRepository
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(year, Arrays.asList(month),
						Arrays.asList(TestUtil.TAN_NUMBER), false,
					false, new Pagination(true, secondPage.getPageSize(), secondPage.getPageStates()));
		assertTrue(thirdPage != null);
	*/}
}
