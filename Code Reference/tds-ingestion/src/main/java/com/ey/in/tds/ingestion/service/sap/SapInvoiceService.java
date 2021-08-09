package com.ey.in.tds.ingestion.service.sap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SapInvoiceService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Get List of SAP Invoice Mis Matches based on Batch Upload Id
	 * 
	 * @param batchId
	 * @return
	 */
/*	public List<InvoiceMismatchByBatchIdDTO> getSAPInvoiceMismatchByBatchUploadIDForExcel(UUID batchId, String tan,
			String processedFrom, Pagination pagination) {
		logger.info("REST request of BatchID and Tan to get List of SAP InvoiceMismatches : {} , {}", batchId, tan);
		List<InvoiceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RMM-AM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RMM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RMM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RM-AM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSap(tan, "NAD", batchId, pagination, 0, 0, processedFrom));

		return listMisMatchBybatchDTO;
	}  */

	// tds mismatches all
/*	public List<InvoiceMismatchByBatchIdDTO> getSAPInvoiceMismatchAll(String tan, int year, int month,
			String processedFrom, Pagination pagination) {
		logger.info("REST request of Tan to get List of SAP InvoiceMismatches : {}", tan);
		List<InvoiceMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();

		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RMM-AM", null, pagination, year, month, processedFrom));
				listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SM-RMM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RMM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "SMM-RM-AM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSap(tan, "NAD", null, pagination, year, month, processedFrom));

		return listMisMatchAllDTO;
	}

	/**
	 * Get List of Invoice Mis Matches based on Batch Upload Id, Mis matches for SAP
	 * 
	 * @param batchId
	 * @param mismatchCategory
	 * @return
	 */
/*	public PagedData<InvoiceLineItem> getSAPInvoiceMismatchByBatchUploadIDMismatchCategory(UUID batchId,
			String mismatchCategory, String tan, String processedFrom, Pagination pagination) {
		logger.info("REST request of batchId ,mismatchCategory and tan for SAP : {} , {} ", batchId, mismatchCategory,
				tan);

		PagedData<InvoiceLineItem> listMisMatch = invoiceLineItemRepository
				.getSAPInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(batchId,
						mismatchCategory, tan, processedFrom, pagination);

		logger.info("Response : {}", listMisMatch);

		return listMisMatch;
	}  */

	/**
	 * get the Mismatches based on the Category
	 * 
	 * @param mismatchCategory
	 * @return
	 */
/*	public PagedData<InvoiceLineItem> getSAPInvoiceMismatchBasedOnMismatchCategory(String mismatchCategory, String tan,
			int year, int challanMonth, String processedFrom, Pagination pagination) {

		PagedData<InvoiceLineItem> listMisMatch = invoiceLineItemRepository
				.getSAPInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(year, challanMonth, tan,
						mismatchCategory, processedFrom, pagination);

		logger.info("List of SAP Invoice mismatch based on mismatch category ,: {}", listMisMatch);
		return listMisMatch;
	}  */

	/**
	 * Update InvoiceMismatchUpadteDTO for On screen Report
	 * 
	 * @param invoiceMismatchUpdateDTO
	 * @throws CustomException
	 */
/*	public UpdateOnScreenDTO updateSAPMismatchByAction(String tan, UpdateOnScreenDTO invoiceMismatchUpdateDTO,
			String processedFrom) {
		//TODO id need to be assigned
		InvoiceLineItem.Key lineItemKey = new InvoiceLineItem.Key(invoiceMismatchUpdateDTO.getAssessmentYear(), tan,
				invoiceMismatchUpdateDTO.getDocumentPostingDate(), null); //invoiceMismatchUpdateDTO.getId()
		Optional<InvoiceLineItem> getLineItem = invoiceLineItemRepository.findById(lineItemKey);
		if (getLineItem.isPresent()) {
			getLineItem.get().setFinalReason(invoiceMismatchUpdateDTO.getReason());
			getLineItem.get().setFinalTdsSection(invoiceMismatchUpdateDTO.getFinalSection());

			BigDecimal finalAmount = invoiceMismatchUpdateDTO.getTaxableValue()
					.multiply(invoiceMismatchUpdateDTO.getFinalRate()).divide(BigDecimal.valueOf(100));

			getLineItem.get().setFinalTdsAmount(finalAmount);
			getLineItem.get().setActionType(invoiceMismatchUpdateDTO.getActionType());
			getLineItem.get().setFinalTdsRate(invoiceMismatchUpdateDTO.getFinalRate());
			getLineItem.get().setIsMismatch(false);
			getLineItem.get().setModifiedDate(new Date());
			getLineItem.get().setActive(true);

			logger.info("final Object Update --- : {}", getLineItem.get());

			invoiceLineItemRepository.save(getLineItem.get());
		} else {
			logger.error("No Record for Invoice Line item to Update for SAP");
			throw new CustomException("No Record for Invoice Line item to Update for SAP",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Update mismatch by action  : {}", invoiceMismatchUpdateDTO);
		return invoiceMismatchUpdateDTO;
	}   */

	/**
	 * To get the summary of mismatches based on batchId for SAP
	 * 
	 * @param type
	 * @param id
	 * @return
	 */
/*	public InvoiceMismatchByBatchIdDTO groupMismatchesByTypeForSap(String tan, String type, UUID batchId,
			Pagination pagination, int year, int month, String processedFrom) {
		PagedData<InvoiceLineItem> listLineItemMisMatch = null;
		if (batchId != null) {
			listLineItemMisMatch = invoiceLineItemRepository.getSAPInvoiceMismatchesByBatchIdType(tan, batchId,
					processedFrom, pagination, type);
		} else {
			listLineItemMisMatch = invoiceLineItemRepository
					.getSAPInvoiceMismatchesByAssessmentYearAssessmentMonthTanType(year, month, tan, processedFrom,
							pagination, type);
		}

		InvoiceMismatchByBatchIdDTO invoiceMismatchByBatchIdDTO = new InvoiceMismatchByBatchIdDTO();

		invoiceMismatchByBatchIdDTO.setId(batchId);
		invoiceMismatchByBatchIdDTO.setMismatchcategory(type);

		Long i = 0L;
		BigDecimal records = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal lowDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal excessDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsSystemAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsClientAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal invoiceValue = new BigDecimal(0).setScale(2, RoundingMode.UP);

		// Need to change to Invoice LineItem
		for (InvoiceLineItem invoiceLineItem : listLineItemMisMatch.getData()) {

			if (invoiceLineItem.getMismatchCategory().equalsIgnoreCase(type)) {
				records = records.add(BigDecimal.valueOf(i++));

				if (invoiceLineItem.getInvoiceAmount() != null) {
					invoiceValue = invoiceValue.add(invoiceLineItem.getInvoiceAmount());
				}
				if (invoiceLineItem.getClientAmount() != null) {
					tdsClientAmount = tdsClientAmount.add(invoiceLineItem.getClientAmount());
				}
				if (invoiceLineItem.getDerivedTdsAmount() != null) {
					tdsSystemAmount = tdsSystemAmount.add(invoiceLineItem.getDerivedTdsAmount());
				}

				BigDecimal val = new BigDecimal(0);
				if (invoiceLineItem.getDerivedTdsAmount() != null && invoiceLineItem.getClientAmount() != null) {
					val = val.add(invoiceLineItem.getDerivedTdsAmount().subtract(invoiceLineItem.getClientAmount()));
					if (invoiceLineItem.getDerivedTdsAmount().compareTo(invoiceLineItem.getClientAmount()) < 0) {
						excessDeductionAmount = excessDeductionAmount.add(val);
					} else {
						lowDeductionAmount = lowDeductionAmount.add(val);
					}
				}
			} // if type comparision
		} // end for loop

		invoiceMismatchByBatchIdDTO.setTotalRecords(BigDecimal.valueOf(i++));
		invoiceMismatchByBatchIdDTO.setInvoiceValue(invoiceValue);
		invoiceMismatchByBatchIdDTO.setTdsSystemAmount(tdsSystemAmount);
		invoiceMismatchByBatchIdDTO.setTdsClientAmount(tdsClientAmount);
		invoiceMismatchByBatchIdDTO.setExcessDeduction(excessDeductionAmount.abs());
		invoiceMismatchByBatchIdDTO.setShortDeduction(lowDeductionAmount.abs());

		return invoiceMismatchByBatchIdDTO;
	}   */

}
