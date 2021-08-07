/**
 * 
 */
package com.ey.in.tds.returns.services;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailComparator;

/**
 * @author rcherukuri
 *
 */
class FilingChallanDetailComparatorTest {

	FilingChallanDetailComparator filingChallanDetailComparator;
	List<FilingChallanDetailBean> filingChallanDetails;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		filingChallanDetailComparator= new FilingChallanDetailComparator();
		filingChallanDetails = new ArrayList<>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		
	}

	@Test
	public void testEqual() {
		FilingChallanDetailBean filingChallanDetailBean1 = new FilingChallanDetailBean();
		filingChallanDetailBean1.setChallanMonth(3);
		filingChallanDetailBean1.setChallanSection("3");
		FilingChallanDetailBean filingChallanDetailBean2 = new FilingChallanDetailBean();
		filingChallanDetailBean2.setChallanMonth(2);
		filingChallanDetailBean2.setChallanSection("2");
		FilingChallanDetailBean filingChallanDetailBean3 = new FilingChallanDetailBean();
		filingChallanDetailBean3.setChallanMonth(1);
		filingChallanDetailBean3.setChallanSection("1");
		
		filingChallanDetails.add(filingChallanDetailBean1);
		filingChallanDetails.add(filingChallanDetailBean2);
		filingChallanDetails.add(filingChallanDetailBean3);
		
		Collections.sort(filingChallanDetails, new FilingChallanDetailComparator());
		assertTrue(filingChallanDetails.get(0).getChallanMonth() == 1);
		assertTrue(filingChallanDetails.get(1).getChallanMonth() == 2);
		assertTrue(filingChallanDetails.get(2).getChallanMonth() == 3);
	}

}
