package com.ey.in.tds.ingestion.service.provision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SapProvisionService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Get List of SAP Provision Mis Matches based on Batch Upload Id
	 * 
	 * @param batchId
	 * @return
	 */
/*	public List<ProvisionMismatchByBatchIdDTO> getSAPProvisionMismatchByBatchUploadIDForExcel(UUID batchId, String tan,
			String processedFrom, Pagination pagination) {
		logger.info("REST request of BatchID and Tan to get List of SAP ProvisionMismatches : {} , {}", batchId, tan);
		List<ProvisionMismatchByBatchIdDTO> listMisMatchBybatchDTO = new ArrayList<>();

		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SM-RM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SM-RMM-AM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SM-RMM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SMM-RMM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SMM-RM-AMM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SMM-RM-AM", batchId, pagination, 0, 0, processedFrom));
		listMisMatchBybatchDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "NAD", batchId, pagination, 0, 0, processedFrom));

		return listMisMatchBybatchDTO;
	}// end of method  */

/*	public List<ProvisionMismatchByBatchIdDTO> getSAPProvisionInvoiceMismatchAll(String tan, int year, int month,
			String processedFrom, Pagination pagination) {

		List<ProvisionMismatchByBatchIdDTO> listMisMatchAllDTO = new ArrayList<>();

		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SM-RM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO.add(
				groupMismatchesByTypeForSapProvision(tan, "SM-RMM-AM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SM-RMM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SMM-RMM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO.add(
				groupMismatchesByTypeForSapProvision(tan, "SMM-RM-AMM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "SMM-RM-AM", null, pagination, year, month, processedFrom));
		listMisMatchAllDTO
				.add(groupMismatchesByTypeForSapProvision(tan, "NAD", null, pagination, year, month, processedFrom));

		return listMisMatchAllDTO;

	}   */

	/**
	 * 
	 * @param batchId
	 * @param mismatchcategory
	 * @param tan
	 * @param processedFrom
	 * @return
	 */
/*	public PagedData<Provision> getSAPProvisionMismatchByBatchUploadIDMismatchCategory(UUID batchId,
			String mismatchcategory, String tan, String processedFrom, Pagination pagination) {

		PagedData<Provision> listMisMatch = provisionRepository
				.getAllProvisionSapMisMatchListByTanYearMismatchCategoryBatchId(processedFrom, tan, mismatchcategory,
						batchId, pagination);
		logger.info("SAP Provision by mismatch Id- : {}", listMisMatch);
		return listMisMatch;

	}  */

	/**
	 * TO get SAP Provision processed files
	 * 
	 * @param mismatchcategory
	 * @param tan
	 * @param year
	 * @param month
	 * @param processedFrom
	 * @return
	 */
/*	public PagedData<Provision> getSAPProvisionnMismatchBasedOnMismatchCategory(String mismatchcategory, String tan,
			int year, int month, String processedFrom, Pagination pagination) {
		PagedData<Provision> listMisMatch = provisionRepository.getAllSAPProvisionMisMatchListByTanYearMismatch(tan,
				year, month, mismatchcategory, processedFrom, pagination);
		logger.info("All provisions mismatch : {}", listMisMatch);
		return listMisMatch;
	}  */

/*	private ProvisionMismatchByBatchIdDTO groupMismatchesByTypeForSapProvision(String tan, String category,
			UUID batchId, Pagination pagination, int year, int month, String processedFrom) {

		PagedData<Provision> listMisMatch = null;
		if (batchId != null) {
			listMisMatch = provisionRepository.getAllSAPProvisionMisMatchListByBatchIdType(processedFrom, tan, batchId,
					pagination, category);
		} else {
			listMisMatch = provisionRepository.getAllSAPProvisionMisMatchListByType(processedFrom, tan, year, month,
					pagination, category);
		}
		ProvisionMismatchByBatchIdDTO provisionMismatchByBatchIdDTO = new ProvisionMismatchByBatchIdDTO();

		//TODO commented out for compilation errors
		//provisionMismatchByBatchIdDTO.setId(batchId);
		provisionMismatchByBatchIdDTO.setMismatchcategory(category);

		Long i = 0L;
		BigDecimal records = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal lowDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal excessDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal tdsSystemAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal tdsClientAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);
		BigDecimal provisionValue = new BigDecimal(0).setScale(2, RoundingMode.UP);

		for (Provision provisionMismatch : listMisMatch.getData()) {

			if (provisionMismatch.getMismatchCategory().equalsIgnoreCase(category)) {
				records = records.add(BigDecimal.valueOf(i++));
				if (provisionMismatch.getProvisionalAmount() != null) {
					provisionValue = provisionValue.add(provisionMismatch.getProvisionalAmount());
				}
				if (provisionMismatch.getDerivedTdsAmount() != null) {
					tdsClientAmount = tdsClientAmount.add(provisionMismatch.getDerivedTdsAmount());
				}
				if (provisionMismatch.getDerivedTdsAmount() != null) {
					tdsSystemAmount = tdsSystemAmount.add(provisionMismatch.getDerivedTdsAmount());
				}
				BigDecimal val = new BigDecimal(0);
				if (provisionMismatch.getDerivedTdsAmount() != null) {
					val = val.add(
							provisionMismatch.getDerivedTdsAmount().subtract(provisionMismatch.getDerivedTdsAmount()));
					if (provisionMismatch.getDerivedTdsAmount()
							.compareTo(provisionMismatch.getDerivedTdsAmount()) < 0) {
						excessDeductionAmount = excessDeductionAmount.add(val);
						if (logger.isInfoEnabled()) {
							logger.info("Excess Deduction Amount is {}", excessDeductionAmount);
						}
					} else {
						lowDeductionAmount = lowDeductionAmount.add(val);
						if (logger.isInfoEnabled()) {
							logger.info("Low Deduction Amount is {}", lowDeductionAmount);
						}
					}
				}
			} // if type comparision
		} // end for loop

		provisionMismatchByBatchIdDTO.setTotalRecords(BigDecimal.valueOf(i++));
		provisionMismatchByBatchIdDTO.setInvoiceValue(provisionValue);
		provisionMismatchByBatchIdDTO.setTdsSystemAmount(tdsSystemAmount);
		provisionMismatchByBatchIdDTO.setTdsClientAmount(tdsClientAmount);
		provisionMismatchByBatchIdDTO.setExcessDeduction(excessDeductionAmount.abs());
		provisionMismatchByBatchIdDTO.setShortDeduction(lowDeductionAmount.abs());

		return provisionMismatchByBatchIdDTO;
	}   */

/*	public UpdateOnScreenDTO updateSAPProvisionMismatchByAction(String tan,
			UpdateOnScreenDTO provisionMismatchUpdateDTO, String processedFrom) throws RecordNotFoundException {
        //TODO id need to be assigned
		Provision.Key provisionKey = new Provision.Key(provisionMismatchUpdateDTO.getAssessmentYear(),
				 tan, provisionMismatchUpdateDTO.getDocumentPostingDate(),
				null);//   provisionMismatchUpdateDTO.getId()
		Optional<Provision> getLineItem = provisionRepository.findById(provisionKey);

		if (getLineItem.isPresent()) {
//			getLineItem.get().setF(invoiceMismatchUpdateDTO.getReason());
			getLineItem.get().setFinalTdsSection(provisionMismatchUpdateDTO.getFinalSection());
			BigDecimal finalAmount = provisionMismatchUpdateDTO.getTaxableValue()
					.multiply(provisionMismatchUpdateDTO.getFinalRate()).divide(BigDecimal.valueOf(100));

			getLineItem.get().setFinalTdsAmount(finalAmount);
			getLineItem.get().setFinalTdsRate(provisionMismatchUpdateDTO.getFinalRate());
			getLineItem.get().setMismatch(true);
			getLineItem.get().setModifiedDate(new Date());
			getLineItem.get().setActive(true);
			getLineItem.get().setWithholdingSection(provisionMismatchUpdateDTO.getSection());
			getLineItem.get().setReason(provisionMismatchUpdateDTO.getReason());
			logger.info("final Object Update --- : {}", getLineItem.get());
			provisionRepository.save(getLineItem.get());
		} else {
			logger.error("No Record for Provision Line item to Update");
			throw new CustomException("No Record for Provision Line item to Update", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Update mismatch by action  : {}", provisionMismatchUpdateDTO);
		return provisionMismatchUpdateDTO;
	}   */

}
