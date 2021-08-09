package com.ey.in.tds.ingestion.service.advance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class SapAdvanceService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Update UpdateOnScreenDTO for remidiation report
	 * 
	 * @param advanceMismatchUpdateDTO
	 * @throws RecordNotFoundException
	 */
/*	public UpdateOnScreenDTO updateMismatchByAction(String processedFrom, String tan,
			UpdateOnScreenDTO advanceMismatchUpdateDTO, String userName) throws RecordNotFoundException {

		//TODO advance id hardcoded
		Advance.Key advanceKey = new Advance.Key(advanceMismatchUpdateDTO.getAssessmentYear(),
				 tan, advanceMismatchUpdateDTO.getDocumentPostingDate(),
				1);
		Optional<Advance> getLineItem = advanceRepository.findById(advanceKey);

		if (getLineItem.isPresent()) {
			Advance advance = getLineItem.get();
			advance.setFinalTdsSection(advanceMismatchUpdateDTO.getFinalSection());
			BigDecimal finalAmount = advanceMismatchUpdateDTO.getTaxableValue()
					.multiply(advanceMismatchUpdateDTO.getFinalRate()).divide(BigDecimal.valueOf(100));
			advance.setFinalTdsAmount(finalAmount);
			advance.setFinalTdsRate(advanceMismatchUpdateDTO.getFinalRate());
			advance.setMismatch(true);
			advance.setModifiedBy(userName);
			advance.setModifiedDate(new Date());
			advance.setActive(true);
			advance.setWithholdingSection(advanceMismatchUpdateDTO.getSection());
			advance.setReason(advanceMismatchUpdateDTO.getReason());
			if (logger.isDebugEnabled()) {
				logger.debug("final Object Update --- : {}", advance);
			}
			advanceRepository.save(advance);
		} else {
			logger.error("No Record for Advance Line item to Update");
			throw new CustomException("No Record for Advance Line item to Update", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Update mismatch by action  : {}", advanceMismatchUpdateDTO);
		}
		return advanceMismatchUpdateDTO;
	}  */

	// Service for getting Advance mismatches summary by TAN and ID;
/*	public List<AdvanceMismatchByBatchIdDTO> getSAPAdvanceMismatchSummaryByTanAndId(String processedFrom,
			String deductorMasterTan, UUID batchUploadId, Pagination pagination) {

		List<AdvanceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		PagedData<Advance> listOfMismatchSummaryByTanAndId = advanceRepository
				.getSAPAdvanceDetailsByTanAndBatchId(processedFrom, deductorMasterTan, batchUploadId, pagination);
		logger.info("Advance List of Mismatch Summary : {}", listOfMismatchSummaryByTanAndId);


		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SM-RM-AMM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SM-RMM-AM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SM-RMM-AMM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SMM-RMM-AMM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SMM-RM-AMM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("SMM-RM-AM", listOfMismatchSummaryByTanAndId, batchUploadId));
		listMisMatchBybatchDTO
				.add(groupMismatchesSummaryByTanAndId("NAD", listOfMismatchSummaryByTanAndId, batchUploadId));
		if (logger.isDebugEnabled()) {
			logger.debug("Advance list of Mismatch by Tan and Id : {}", listMisMatchBybatchDTO);
		}

		return listMisMatchBybatchDTO;
	}

	// Service for getting All Advance mismatches summary by TAN and Active;
	public List<AdvanceMismatchByBatchIdDTO> getAllSAPAdvanceMismatchSummaryByTanAndActive(String processedFrom,
			String deductorMasterTan, int assessmentYear, int month, Pagination pagination) {

		List<AdvanceMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		// int assessmentYear = Calendar.getInstance().get(Calendar.YEAR);

		PagedData<Advance> listOfMismatchSummaryByTan = advanceRepository.getSAPAdvanceDetailsByTanAndActive(
				processedFrom, deductorMasterTan, assessmentYear, month, pagination);

		// listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SM-RM",
		// listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SM-RM-AMM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SM-RMM-AM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SM-RMM-AMM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SMM-RMM-AMM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SMM-RM-AMM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("SMM-RM-AM", listOfMismatchSummaryByTan));
		listMisMatchBybatchDTO.add(groupMismatchesSummaryByTan("NAD", listOfMismatchSummaryByTan));
		if (logger.isDebugEnabled()) {
			logger.debug("Advance Mismatch Summary by Tan and Active : {}", listMisMatchBybatchDTO);
		}

		return listMisMatchBybatchDTO;
	}

	// Service for getting Advance mismatches by TAN and ID;
	public PagedData<Advance> getSAPAdvanceMismatchByTanBatchIdMismatchCategory(String processedFrom,
			String deductorMasterTan, UUID batchUploadId, String mismatchCategory, Pagination pagination) {

		PagedData<Advance> listMisMatch = advanceRepository.getSAPAdvanceDetailsByTanBatchIdAndMismatchCategory(
				processedFrom, deductorMasterTan, mismatchCategory, batchUploadId, pagination);
		if (logger.isDebugEnabled()) {
			logger.debug("Advance list by ID and Mismatch Category : {}", listMisMatch);
		}

		return listMisMatch;
	}

	public PagedData<Advance> getAllSAPAdvanceMismatchByTanAndMismatchCategory(String processedFrom,
			String deductorMasterTan, String mismatchCategory, int assessmentYear, int month, Pagination pagination) {

		PagedData<Advance> listMisMatch = advanceRepository.getSAPAdvanceDetailsByTanActiveAndMismatchCategory(
				processedFrom, assessmentYear, month, deductorMasterTan, mismatchCategory, pagination);
		if (logger.isDebugEnabled()) {
			logger.debug("Advance list by Tan and Mismatch Category : {}", listMisMatch);
		}

		return listMisMatch;
	}

	// Method for Mismatches Summary by TAN and ID;
	private AdvanceMismatchByBatchIdDTO groupMismatchesSummaryByTanAndId(String type,
			PagedData<Advance> listOfMismatchSummaryByTanAndId, UUID batchUploadId) {

		AdvanceMismatchByBatchIdDTO advanceMismatchByBatchIdDTO = new AdvanceMismatchByBatchIdDTO();

		//TODO commented out batch upload
		//advanceMismatchByBatchIdDTO.setId(batchUploadId);
		advanceMismatchByBatchIdDTO.setMismatchcategory(type);

		Long i = 0L;
		BigDecimal records = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal lowDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal excessDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsSystemAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsClientAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal invoiceValue = new BigDecimal(0).setScale(2, RoundingMode.UP);

		for (Advance advanceMismatch : listOfMismatchSummaryByTanAndId.getData()) {

			if (advanceMismatch.getMismatchCategory().equalsIgnoreCase(type)) {
				records = records.add(BigDecimal.valueOf(i++));

				if (advanceMismatch.getAmount() != null) {
					invoiceValue = invoiceValue.add(advanceMismatch.getAmount());
				}
				if (advanceMismatch.getActualTdsAmount() != null) {
					tdsClientAmount = tdsClientAmount.add(advanceMismatch.getActualTdsAmount());
				}
				if (advanceMismatch.getDerivedTdsAmount() != null) {
					tdsSystemAmount = tdsSystemAmount.add(advanceMismatch.getDerivedTdsAmount());
				}
				BigDecimal val = new BigDecimal(0);
				if (advanceMismatch.getDerivedTdsAmount() != null && advanceMismatch.getActualTdsAmount() != null) {
					val = val.add(advanceMismatch.getDerivedTdsAmount().subtract(advanceMismatch.getActualTdsAmount()));
					if (advanceMismatch.getDerivedTdsAmount().compareTo(advanceMismatch.getActualTdsAmount()) < 0) {
						excessDeductionAmount = excessDeductionAmount.add(val);
					} else {
						lowDeductionAmount = lowDeductionAmount.add(val);
					}
				}

			} // if type comparision
		} // end for loop

		advanceMismatchByBatchIdDTO.setTotalRecords(BigDecimal.valueOf(i++));
		advanceMismatchByBatchIdDTO.setInvoiceValue(invoiceValue);
		advanceMismatchByBatchIdDTO.setTdsSystemAmount(tdsSystemAmount);
		advanceMismatchByBatchIdDTO.setTdsClientAmount(tdsClientAmount);
		advanceMismatchByBatchIdDTO.setExcessDeduction(excessDeductionAmount.abs());
		advanceMismatchByBatchIdDTO.setShortDeduction(lowDeductionAmount.abs());
		if (logger.isDebugEnabled()) {
			logger.debug("Returns Mismatch Summary list : {}", advanceMismatchByBatchIdDTO);
		}

		return advanceMismatchByBatchIdDTO;
	}

	// Method for All Mismatches Summary by TAN and ID;
	private AdvanceMismatchByBatchIdDTO groupMismatchesSummaryByTan(String type,
			PagedData<Advance> listOfMismatchSummaryByTan) {

		AdvanceMismatchByBatchIdDTO advanceMismatchByBatchIdDTO = new AdvanceMismatchByBatchIdDTO();

		advanceMismatchByBatchIdDTO.setMismatchcategory(type);

		Long i = 0L;
		BigDecimal records = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal lowDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal excessDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsSystemAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsClientAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal invoiceValue = new BigDecimal(0).setScale(2, RoundingMode.UP);

		for (Advance advanceMismatch : listOfMismatchSummaryByTan.getData()) {

			if (advanceMismatch.getMismatchCategory().equalsIgnoreCase(type)) {
				records = records.add(BigDecimal.valueOf(i++));

				if (advanceMismatch.getAmount() != null) {
					invoiceValue = invoiceValue.add(advanceMismatch.getAmount());
				}
				if (advanceMismatch.getActualTdsAmount() != null) {
					tdsClientAmount = tdsClientAmount.add(advanceMismatch.getActualTdsAmount());
				}
				if (advanceMismatch.getDerivedTdsAmount() != null) {
					tdsSystemAmount = tdsSystemAmount.add(advanceMismatch.getDerivedTdsAmount());
				}
				BigDecimal val = new BigDecimal(0);
				if (advanceMismatch.getDerivedTdsAmount() != null && advanceMismatch.getActualTdsAmount() != null) {
					val = val.add(advanceMismatch.getDerivedTdsAmount().subtract(advanceMismatch.getActualTdsAmount()));
					if (advanceMismatch.getDerivedTdsAmount().compareTo(advanceMismatch.getActualTdsAmount()) < 0) {
						excessDeductionAmount = excessDeductionAmount.add(val);
					} else {
						lowDeductionAmount = lowDeductionAmount.add(val);
					}
				}

			} // if type comparision
		} // end for loop

		advanceMismatchByBatchIdDTO.setTotalRecords(BigDecimal.valueOf(i++));
		advanceMismatchByBatchIdDTO.setInvoiceValue(invoiceValue);
		advanceMismatchByBatchIdDTO.setTdsSystemAmount(tdsSystemAmount);
		advanceMismatchByBatchIdDTO.setTdsClientAmount(tdsClientAmount);
		advanceMismatchByBatchIdDTO.setExcessDeduction(excessDeductionAmount.abs());
		advanceMismatchByBatchIdDTO.setShortDeduction(lowDeductionAmount.abs());
		if (logger.isDebugEnabled()) {
			logger.debug("Returns Mismatch Summary list by Tan : {}", advanceMismatchByBatchIdDTO);
		}

		return advanceMismatchByBatchIdDTO;
	}  */

}
