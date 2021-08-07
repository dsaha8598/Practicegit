package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.domain.ThresholdGroupAndNopMapping;
import com.ey.in.tds.common.domain.ThresholdLimitGroupMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.AoMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.LdcMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.DeducteeMasterNonResidentialDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.AdditionalSectionsDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterResidentDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeNopGroup;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.ErrorReportService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.CommonValidationsCassandra;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeducteeMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeNopGroupDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.KYCDetailsDAO;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesDTO;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesNonResidentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@Service
public class DeducteeMasterService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private DeducteeMasterNonResidentialDAO deducteeMasterNonResidentialDAO;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private AoMasterDAO aoMasterDAO;

	@Autowired
	private DeducteeBulkService deducteeBulkService;

	@Autowired
	private DeducteeNopGroupDAO deducteeNopGroupDAO;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private KYCDetailsDAO kycDetailsDAO;

	@Autowired
	private LdcMasterDAO ldcMasterDAO;

	@Autowired
	private ErrorReportService errorReportService;

	Map<String, String> deducteeNonResidentialExcelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -1561948417139713925L;
		{
			put("ISTRC AVAILABLE", "N");
			put("IS TEN F AVAILABLE", "Q");
			put("WEATHER PE INDIA", "T");
			put("No PE DOCUMENT AVAILABLE", "W");
			put("IS PEOM AVAILABLE", "X");
			put("DEDUCTEE NAME", "J");
			put("DEDUCTEE PAN", "K");
			put("DEDUCTEE RESIDENTIAL STATUS", "H");
		}
	};

	Map<String, String> deducteeResidentialExcelHeaderMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -1725324378395307583L;
		{
			put("DEDUCTEE RESIDENTIAL STATUS", "G");
			put("DEDUCTEE NAME", "I");
			put("DEDUCTEE PAN", "J");
		}
	};

	ObjectMapper objectMapper = new ObjectMapper();

	public DeducteeMasterResidential createResident(DeducteeMasterResidentDTO deducteeMasterResidentialDTO,
			String deductorPan, String userName, String deductorTan) throws JsonProcessingException, ParseException {
		logger.info("REST request to save a deductee residential Record : {}", deducteeMasterResidentialDTO);

		DeducteeMasterResidential deducteeMasterResidential = new DeducteeMasterResidential();

		// deducteee key
		if (StringUtils.isNotBlank(deducteeMasterResidentialDTO.getDeducteeCode())) {
			deducteeMasterResidential.setDeducteeKey(deducteeMasterResidentialDTO.getDeducteeCode().trim());
		} else {
			String name = deducteeMasterResidentialDTO.getDeducteeName().trim().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			if (StringUtils.isNotBlank(deducteeMasterResidentialDTO.getDeducteePAN())) {
				deducteeMasterResidential
						.setDeducteeKey(name.concat(deducteeMasterResidentialDTO.getDeducteePAN().trim()));
			} else {
				deducteeMasterResidential.setDeducteeKey(name);
			}
		}
		// get old recodes based on deductee key and deductor pan
		List<DeducteeMasterResidential> deducteeDbDetails = deducteeMasterResidentialDAO
				.findAllByDeducteeKey(deductorPan, deducteeMasterResidential.getDeducteeKey());
		// deductee creation validation check
		if (!deducteeDbDetails.isEmpty()) {
			CommonValidationsCassandra.validateApplicableFields(deducteeDbDetails.get(0).getApplicableTo(),
					deducteeMasterResidentialDTO.getApplicableFrom());
		}
		deducteeMasterResidential.setDeductorPan(deductorPan);
		deducteeMasterResidential.setDeducteePAN(deducteeMasterResidentialDTO.getDeducteePAN());
		deducteeMasterResidential.setDeducteeCode(StringUtils.isNotBlank(deducteeMasterResidentialDTO.getDeducteeCode())
				? deducteeMasterResidentialDTO.getDeducteeCode().trim()
				: "");
		deducteeMasterResidential.setDeducteeName(deducteeMasterResidentialDTO.getDeducteeName().trim());
		String deducteeName = deducteeMasterResidential.getDeducteeName().toLowerCase();
		deducteeName = deducteeName.replaceAll("[^a-z0-9 ]", "");
		deducteeMasterResidential.setModifiedName(deducteeName);
		deducteeMasterResidential.setDeducteePAN(StringUtils.isNotBlank(deducteeMasterResidentialDTO.getDeducteePAN())
				? deducteeMasterResidentialDTO.getDeducteePAN().trim()
				: "");
		deducteeMasterResidential.setDeducteeStatus(deducteeMasterResidentialDTO.getDeducteeStatus());
		deducteeMasterResidential
				.setDeducteeResidentialStatus(deducteeMasterResidentialDTO.getDeducteeResidentialStatus());
		deducteeMasterResidential.setDeducteeAadharNumber(deducteeMasterResidentialDTO.getDeducteeAadharNumber());
		deducteeMasterResidential.setDefaultRate(BigDecimal.valueOf(deducteeMasterResidentialDTO.getDefaultRate()));
		deducteeMasterResidential.setEmailAddress(deducteeMasterResidentialDTO.getEmailAddress());
		deducteeMasterResidential.setPhoneNumber(
				deducteeMasterResidentialDTO.getPhoneNumber() != null ? deducteeMasterResidentialDTO.getPhoneNumber()
						: "");
		deducteeMasterResidential.setFlatDoorBlockNo(deducteeMasterResidentialDTO.getAddress().getFlatDoorBlockNo());
		deducteeMasterResidential
				.setNameBuildingVillage(deducteeMasterResidentialDTO.getAddress().getNameBuildingVillage());
		deducteeMasterResidential
				.setRoadStreetPostoffice(deducteeMasterResidentialDTO.getAddress().getRoadStreetPostoffice());
		deducteeMasterResidential.setAreaLocality(deducteeMasterResidentialDTO.getAddress().getAreaLocality());
		deducteeMasterResidential.setTownCityDistrict(deducteeMasterResidentialDTO.getAddress().getTownCityDistrict());
		deducteeMasterResidential.setState(deducteeMasterResidentialDTO.getAddress().getStateName());
		deducteeMasterResidential.setCountry(deducteeMasterResidentialDTO.getAddress().getCountryName());
		deducteeMasterResidential.setPinCode(deducteeMasterResidentialDTO.getAddress().getPinCode());
		deducteeMasterResidential.setCreatedDate(new Timestamp(new Date().getTime()));
		deducteeMasterResidential.setCreatedBy(userName);
		deducteeMasterResidential.setModifiedBy(userName);
		deducteeMasterResidential.setModifiedDate(new Timestamp(new Date().getTime()));
		deducteeMasterResidential.setActive(true);
		deducteeMasterResidential
				.setIsThresholdLimitApplicable(deducteeMasterResidentialDTO.getIsThresholdLimitApplicable());
		deducteeMasterResidential.setSectionCode(deducteeMasterResidentialDTO.getSectionCode());
		deducteeMasterResidential.setTdsExcemptionFlag(deducteeMasterResidentialDTO.getTdsExcemptionFlag());
		deducteeMasterResidential.setTdsExcemptionReason(deducteeMasterResidentialDTO.getTdsExcemptionReason());
		deducteeMasterResidential
				.setDeducteeMasterBalancesOf194q(deducteeMasterResidentialDTO.getDeducteeMasterBalancesOf194q());
		deducteeMasterResidential.setAdvanceBalancesOf194q(deducteeMasterResidentialDTO.getAdvanceBalancesOf194q());
		deducteeMasterResidential.setProvisionBalancesOf194q(deducteeMasterResidentialDTO.getProvisionBalancesOf194q());
		deducteeMasterResidential.setAdvancesAsOfMarch(deducteeMasterResidentialDTO.getAdvancesAsOfMarch());
		deducteeMasterResidential.setProvisionsAsOfMarch(deducteeMasterResidentialDTO.getProvisionsAsOfMarch());
		deducteeMasterResidential.setCurrentBalanceMonth(deducteeMasterResidentialDTO.getCurrentBalanceMonth());
		deducteeMasterResidential.setCurrentBalanceYear(deducteeMasterResidentialDTO.getCurrentBalanceYear());
		deducteeMasterResidential.setPreviousBalanceMonth(deducteeMasterResidentialDTO.getPreviousBalanceMonth());
		deducteeMasterResidential.setPreviousBalanceYear(deducteeMasterResidentialDTO.getPreviousBalanceYear());
		deducteeMasterResidential.setDeductorTan(deductorTan);
		deducteeMasterResidential
				.setTdsApplicabilityUnderSection(deducteeMasterResidentialDTO.getTdsApplicabilityUnderSection());
		deducteeMasterResidential.setDeducteeTan(deducteeMasterResidentialDTO.getDeducteeTan());
		deducteeMasterResidential
				.setOpeningBalanceCreditNote(deducteeMasterResidentialDTO.getOpeningBalanceCreditNote());
		deducteeMasterResidential.setTdsExemptionNumber(deducteeMasterResidentialDTO.getTdsExemptionNumber());
		deducteeMasterResidential.setDeducteeGSTIN(deducteeMasterResidentialDTO.getDeducteeGSTIN());
		deducteeMasterResidential.setGrOrIRIndicator(deducteeMasterResidentialDTO.getGrOrIRIndicator());
		// Deductee Enrichment Key
		if (StringUtils.isNotBlank(deducteeMasterResidentialDTO.getDeducteeCode())) {
			deducteeMasterResidential.setDeducteeEnrichmentKey(deducteeMasterResidentialDTO.getDeducteeCode());
		} else {
			String name = deducteeMasterResidentialDTO.getDeducteeName().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			deducteeMasterResidential.setDeducteeEnrichmentKey(name);
		}

		// set section and rate
		if (StringUtils.isEmpty(deducteeMasterResidentialDTO.getSection())) {
			deducteeMasterResidential.setRate(BigDecimal.ZERO);
		} else if (deducteeMasterResidentialDTO.getRate() != 0) {
			deducteeMasterResidential.setRate(BigDecimal.valueOf(deducteeMasterResidentialDTO.getRate()));
		} else {
			deducteeMasterResidential.setRate(BigDecimal.ZERO);
		}
		deducteeMasterResidential.setSection(deducteeMasterResidentialDTO.getSection());
		// nature of payment
		if (StringUtils.isBlank(deducteeMasterResidentialDTO.getNatureOfPayment())) {
			String nop = getNatureOfPaymentBasedOnHightRateRES(deducteeMasterResidentialDTO.getSection(),
					deducteeMasterResidentialDTO);
			deducteeMasterResidential.setNatureOfPayment(nop);
		} else {
			deducteeMasterResidential.setNatureOfPayment(deducteeMasterResidentialDTO.getNatureOfPayment());
		}
		deducteeMasterResidential
				.setIsDeducteeHasAdditionalSections(deducteeMasterResidentialDTO.getIsDeducteeHasAdditionalSections());
		Map<String, Float> additionalSection = new HashMap<>();
		boolean isAdditionalSection = false;
		Map<String, Float> newSections = new HashMap<>();

		// to pick the section and section code from additional section object
		Map<String, String> newSectionAndSectionCode = new HashMap<>();
		Map<String, Integer> sectionsAndThreshold = new HashMap<>();

		if (deducteeMasterResidentialDTO.getIsDeducteeHasAdditionalSections() != null
				&& !deducteeMasterResidentialDTO.getIsDeducteeHasAdditionalSections().equals(false)) {
			// iterating the additional sections
			for (AdditionalSectionsDTO additionalSections : deducteeMasterResidentialDTO.getAdditionalSections()) {
				if (StringUtils.isBlank(deducteeMasterResidentialDTO.getSection())) {
					if (StringUtils.isNotBlank(additionalSections.getSection())) {
						deducteeMasterResidential.setSection(additionalSections.getSection());
						// nature of payment
						if (StringUtils.isBlank(deducteeMasterResidentialDTO.getNatureOfPayment())) {
							String nop = getNatureOfPaymentBasedOnHightRateRES(
									deducteeMasterResidentialDTO.getSection(), deducteeMasterResidentialDTO);
							deducteeMasterResidential.setNatureOfPayment(nop);
						} else {
							deducteeMasterResidential
									.setNatureOfPayment(deducteeMasterResidentialDTO.getNatureOfPayment());
						}
						deducteeMasterResidential.setIsDeducteeHasAdditionalSections(isAdditionalSection);
						deducteeMasterResidential.setSectionCode(additionalSections.getSectionCode());
						if (additionalSections.getRate() != 0) {
							deducteeMasterResidential.setRate(BigDecimal.valueOf(additionalSections.getRate()));
						} else {
							deducteeMasterResidential.setRate(BigDecimal.ZERO);
						}
						newSections.put(deducteeMasterResidential.getSection(),
								deducteeMasterResidential.getRate().floatValue());
					}
				} else if (StringUtils.isNotBlank(deducteeMasterResidentialDTO.getSection())
						&& deducteeMasterResidentialDTO.getSection().equals(additionalSections.getSection())) {
					throw new CustomException("Duplicate sections are not allowed", HttpStatus.BAD_REQUEST);
				} else if (StringUtils.isNotBlank(additionalSections.getSection())) {
					// nature of payment
					String sectionAndNop = StringUtils.EMPTY;
					if (StringUtils.isBlank(additionalSections.getNatureOfPayment())) {
						String nop = getNatureOfPaymentBasedOnHightRateRES(additionalSections.getSection(),
								deducteeMasterResidentialDTO);
						sectionAndNop = additionalSections.getSection() + "-" + nop;
					} else {
						sectionAndNop = additionalSections.getSection() + "-" + additionalSections.getNatureOfPayment();
					}
					if (additionalSections.getRate() != 0) {
						additionalSection.put(sectionAndNop, additionalSections.getRate());
					} else {
						additionalSection.put(sectionAndNop, (float) 0);
					}
					additionalSections.setRate((additionalSections.getRate() != 0 ? additionalSections.getRate() : 0));
					newSections.put(additionalSections.getSection(), additionalSections.getRate());
					if (StringUtils.isNotBlank(additionalSections.getSectionCode())) {
						newSectionAndSectionCode.put(additionalSections.getSectionCode(),
								additionalSections.getSection());
					}
					Integer threshold = (additionalSections.getIsThresholdApplicable() != null
							&& additionalSections.getIsThresholdApplicable().equals(true)) ? 1 : 0;
					sectionsAndThreshold.put(additionalSections.getSection(), threshold);
				}
				if (StringUtils.isNotBlank(deducteeMasterResidentialDTO.getSectionCode())
						&& StringUtils.isNotBlank(additionalSections.getSectionCode())
						&& deducteeMasterResidentialDTO.getSectionCode().equals(additionalSections.getSectionCode())) {
					throw new CustomException("Duplicate section Codes are not allowed", HttpStatus.BAD_REQUEST);
				}
			}
		}

		DeducteeMasterResidential deducteeDB = null;
		for (DeducteeMasterResidential deductee : deducteeDbDetails) {
			if (deductee.getApplicableTo() == null || deductee.getApplicableTo().after(new Date())) {
				deducteeDB = deductee;
				break;
			}

		}
		if (deducteeDB != null) {
			Map<String, Float> dBsectionsAndNop = new HashMap<>();
			Map<String, Float> dbSections = new HashMap<>();
			if (StringUtils.isNotBlank(deducteeDB.getSection())) {
				dBsectionsAndNop.put(deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment(),
						(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
				dbSections.put(deducteeDB.getSection(),
						(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
			}
			if (StringUtils.isNotBlank(deducteeDB.getAdditionalSections())) {
				Map<String, Float> map = objectMapper.readValue(deducteeDB.getAdditionalSections(),
						new TypeReference<Map<String, Float>>() {
						});
				for (Entry<String, Float> d : map.entrySet()) {
					dBsectionsAndNop.put(d.getKey(), d.getValue());
				}
				dBsectionsAndNop.putAll(map);

			}
			float rate = (deducteeMasterResidential.getRate() != null ? deducteeMasterResidential.getRate().floatValue()
					: 0);
			newSections.put(
					deducteeMasterResidential.getSection() != null ? deducteeMasterResidential.getSection() : "", rate);
			if (additionalSection != null) {
				newSections.putAll(additionalSection);
			}
			if (StringUtils.isBlank(deducteeMasterResidential.getSection())) {
				deducteeMasterResidential.setSection(deducteeDB.getSection());
				deducteeMasterResidential.setNatureOfPayment(deducteeDB.getNatureOfPayment());
				deducteeMasterResidential.setRate(deducteeDB.getRate());
				deducteeMasterResidential.setSectionCode(deducteeDB.getSectionCode());
			}
			boolean isduplicateSections = dbSections.keySet().equals(newSections.keySet());
			if (!isduplicateSections) {
				Map<String, Float> sections = new HashMap<>();
				Map<String, String> sectionCodeAndSections = new HashMap<>();
				for (Entry<String, Float> dBsectionAndNop : dBsectionsAndNop.entrySet()) {
					String dBSection = StringUtils.substringBefore(dBsectionAndNop.getKey(), "-");
					if (!newSections.containsKey(dBSection)
							&& !deducteeMasterResidential.getSection().equals(dBSection)) {
						if (StringUtils.isEmpty(dBsectionAndNop.getKey())) {
							deducteeMasterResidential.setIsDeducteeHasAdditionalSections(false);
						} else if (dBSection.equals(deducteeDB.getSection())) {
							String sectionAndNop = deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment();
							sections.put(sectionAndNop, dBsectionAndNop.getValue());
							sectionCodeAndSections.put(deducteeDB.getSectionCode(), deducteeDB.getSection());
						} else {
							sections.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
						}
						additionalSection.putAll(sections);
					}
				}

			}
			if (additionalSection.size() > 0) {
				deducteeMasterResidential.setIsDeducteeHasAdditionalSections(true);
			}
			// preparing map for additional section code
			deducteeDB.setActive(false);
			deducteeDB.setApplicableTo(subtractDay(new Date()));
			deducteeMasterResidentialDAO.updateApplicableTo(deducteeDB);
		}
		newSectionAndSectionCode = getJsonForadditionalSectionCode(newSectionAndSectionCode, deducteeDB);

		String additionalSections = objectMapper.writeValueAsString(additionalSection);
		String additionalSectionCodes = objectMapper.writeValueAsString(newSectionAndSectionCode);
		deducteeMasterResidential.setAdditionalSections(additionalSections);
		deducteeMasterResidential.setAdditionalSectionCode(additionalSectionCodes);
		sectionsAndThreshold = getJsonForadditionalSectionThreshold(sectionsAndThreshold, deducteeDB);
		String additionalSectionThresholds = objectMapper.writeValueAsString(sectionsAndThreshold);
		deducteeMasterResidential.setAdditionalSectionThresholds(additionalSectionThresholds);
		deducteeMasterResidential.setApplicableFrom(deducteeMasterResidentialDTO.getApplicableFrom());
		deducteeMasterResidential.setApplicableTo(deducteeMasterResidentialDTO.getApplicableTo());
		deducteeMasterResidential.setInvoiceTransactionCount(0);
		deducteeMasterResidential.setProvisionTransactionCount(0);
		deducteeMasterResidential.setAdvanceTransactionCount(0);
		List<DeductorMaster> deductorMaster = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		if (!deductorMaster.isEmpty() && deductorMaster != null) {
			deducteeMasterResidential.setDeductorCode(deductorMaster.get(0).getCode());
		}

		Map<String, String> nopMap = new HashMap<>();
		nopMap.put("nop", "Purchase of Goods");
		nopMap.put("section", "194Q");
		// feign client for nop
		Optional<NatureOfPaymentMaster> natureObj = mastersClient.getNOPBasedOnSectionAndNature(nopMap).getBody()
				.getData();
		Optional<TdsMaster> tdsMasterObj = Optional.empty();
		Optional<ThresholdGroupAndNopMapping> thresholdResponse = Optional.empty();
		Long nopId = 0L;
		if (natureObj.isPresent()) {
			nopId = natureObj.get().getId();
			tdsMasterObj = mastersClient.getTdsRateBasedOnNatureId(natureObj.get().getId()).getBody().getData();
			thresholdResponse = mastersClient.getThresholdGroupId(natureObj.get().getId()).getBody().getData();
		}
		// get threshold ledger data based on deductee pan or deductee key
		List<DeducteeNopGroup> deducteeNopList = deducteeNopGroupDAO.findByDeducteeKeyOrDeducteePan(
				deducteeMasterResidential.getDeducteeKey(), deductorPan, deducteeMasterResidential.getDeducteePAN(),
				deducteeMasterResidentialDTO.getCurrentBalanceYear());
		// added three amounts
		BigDecimal amount = deducteeMasterResidentialDTO.getDeducteeMasterBalancesOf194q()
				.add(deducteeMasterResidentialDTO.getAdvanceBalancesOf194q())
				.add(deducteeMasterResidentialDTO.getProvisionBalancesOf194q());
		logger.info("amount is {}: ", amount);

		DeducteeNopGroup deducteeNop = new DeducteeNopGroup();
		List<ThresholdLimitGroupMaster> thresholdMasterList = mastersClient.getThresholdGroupByIds().getBody()
				.getData();
		if (!deducteeNopList.isEmpty()) {
			/**
			 * case-1 if the record is updated with a different collectee code but existing
			 * pan,then we will be having 2 record, case-2 if the record is created with
			 * diferent colectee code but existing pan then we will be having one record for
			 * case2 we are taking the record having collectee code
			 * 
			 * case-3 if the NOI update is already done for the perticular pan, then check
			 * in DB with pan and collectee code combination, if any record is present then
			 * do not execute the update logic once again
			 */
			List<ThresholdGroupAndNopMapping> nopList = mastersClient.getThresholdNopGroupData(nopId).getBody()
					.getData();
			for (DeducteeNopGroup nop : deducteeNopList) {
				deducteeNopGroupUpdate(nop, amount, userName, deducteeMasterResidential.getDeducteePAN(), nopList,
						thresholdMasterList);
			}
		} else {
			deducteeNopGroupSave(deductorPan, userName, deducteeMasterResidential, amount,
					deducteeMasterResidentialDTO.getCurrentBalanceYear(), deducteeNop, nopId, thresholdMasterList);
		}
		// check for advance balances of 194Q amount
		BigDecimal advanceAmount = deducteeMasterResidentialDTO.getAdvanceBalancesOf194q();
		logger.info("advance march Amount is: {} ", advanceAmount);
		// check greater then zero
		if (advanceAmount.compareTo(BigDecimal.valueOf(0)) == 1) {
			// advance save
			advanceSave(userName, deducteeMasterResidential, natureObj, tdsMasterObj,
					deducteeMasterResidentialDTO.getCurrentBalanceMonth(), advanceAmount,
					deducteeMasterResidentialDTO.getCurrentBalanceYear(), thresholdResponse);
		}
		// check for advance as of march amount
		BigDecimal advanceAsOfMarchAmount = deducteeMasterResidentialDTO.getAdvancesAsOfMarch();
		logger.info("advance as of march Amount is {}: ", advanceAsOfMarchAmount);
		// check greater then zero
		if (advanceAsOfMarchAmount.compareTo(BigDecimal.valueOf(0)) == 1) {
			// advance save
			advanceSave(userName, deducteeMasterResidential, natureObj, tdsMasterObj,
					deducteeMasterResidentialDTO.getPreviousBalanceMonth(), advanceAsOfMarchAmount,
					deducteeMasterResidentialDTO.getPreviousBalanceYear(), thresholdResponse);

		}

		// check for provision balances of 194Q amount
		BigDecimal provisionAmount = deducteeMasterResidentialDTO.getProvisionBalancesOf194q();
		logger.info("provision Balance of 194Q Amount is: {} ", provisionAmount);
		// check greater then zero
		if (provisionAmount.compareTo(BigDecimal.valueOf(0)) == 1) {
			// provision save
			provisionSave(userName, deducteeMasterResidential, natureObj, tdsMasterObj,
					deducteeMasterResidentialDTO.getCurrentBalanceMonth(), provisionAmount,
					deducteeMasterResidentialDTO.getCurrentBalanceYear(), thresholdResponse);
		}
		// check for provision as of march amount
		BigDecimal provisionsAsOfMarchAmount = deducteeMasterResidentialDTO.getProvisionsAsOfMarch();
		logger.info("provision as of march Amount is: {} ", provisionsAsOfMarchAmount);
		// check greater then zero
		if (provisionsAsOfMarchAmount.compareTo(BigDecimal.valueOf(0)) == 1) {
			// provision save
			provisionSave(userName, deducteeMasterResidential, natureObj, tdsMasterObj,
					deducteeMasterResidentialDTO.getPreviousBalanceMonth(), provisionsAsOfMarchAmount,
					deducteeMasterResidentialDTO.getPreviousBalanceYear(), thresholdResponse);

		}
		deducteeMasterResidentialDAO.save(deducteeMasterResidential);
		return deducteeMasterResidential;
	}

	/**
	 * 
	 * @param userName
	 * @param deducteeMaster
	 * @param natureObj
	 * @param tdsMasterObj
	 * @param month
	 * @param provisionAmount
	 * @param assessmentYear
	 * @param thresholdResponse
	 */
	private void provisionSave(String userName, DeducteeMasterResidential deducteeMaster,
			Optional<NatureOfPaymentMaster> natureObj, Optional<TdsMaster> tdsMasterObj, int month,
			BigDecimal provisionAmount, int assessmentYear, Optional<ThresholdGroupAndNopMapping> thresholdResponse) {

		ProvisionDTO provisionDto = new ProvisionDTO();
		provisionDto.setDeducteeCode(deducteeMaster.getDeducteeCode());
		provisionDto.setAssessmentYear(assessmentYear);
		provisionDto.setAssessmentMonth(month);
		provisionDto.setChallanMonth(month);
		provisionDto.setProvisionalAmount(provisionAmount);
		provisionDto.setDeducteeKey(deducteeMaster.getDeducteeKey());
		provisionDto.setDeductorTan(deducteeMaster.getDeductorTan());
		provisionDto.setDeductorMasterTan(deducteeMaster.getDeductorTan());
		provisionDto.setDeductorPan(deducteeMaster.getDeductorPan());
		provisionDto.setDeducteeName(deducteeMaster.getDeducteeName());
		provisionDto.setDocumentType("PRV");
		provisionDto.setIsResident("N");
		provisionDto.setDeducteePan(deducteeMaster.getDeducteePAN());
		provisionDto.setSupplyType("TAX");
		LocalDate localDate = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month, 1);
		LocalDate monthDate = localDate.plusDays(localDate.lengthOfMonth() - 1);
		int monthLastDay = monthDate.getDayOfMonth();
		logger.info("month last day: {}", monthLastDay);
		LocalDate localDate1 = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
				monthLastDay);
		ZoneId defaultZoneId = ZoneId.systemDefault();
		provisionDto.setDocumentDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		provisionDto.setDocumentNumber(deducteeMaster.getDeducteeCode() + deducteeMaster.getDeducteePAN());
		provisionDto.setLineItemNumber("1");
		if (natureObj.isPresent()) {
			provisionDto.setProvisionNpId(natureObj.get().getId().intValue());
		} else {
			provisionDto.setProvisionNpId(null);
		}
		if (thresholdResponse.isPresent()) {
			provisionDto.setProvisionGroupid(thresholdResponse.get().getThresholdLimitGroupMaster().getId().intValue());
		} else {
			provisionDto.setProvisionGroupid(null);
		}
		provisionDto.setActive(true);
		provisionDto.setIsParent(false);
		provisionDto.setIsExempted(false);
		provisionDto.setChallanPaid(false);
		provisionDto.setApprovedForChallan(false);
		provisionDto.setIsChallanGenerated(false);
		provisionDto.setCreatedBy(userName);
		provisionDto.setCreatedDate(new Date());
		provisionDto.setModifiedBy(userName);
		provisionDto.setModifiedDate(new Date());
		provisionDto.setPostingDateOfDocument(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		provisionDto.setMismatch(false);
		provisionDto.setUnderThreshold(false);
		// 9 columns
		provisionDto.setWithholdingSection("194Q");
		provisionDto.setDerivedTdsSection("194Q");
		provisionDto.setFinalTdsSection("194Q");
		if (tdsMasterObj.isPresent()) {
			provisionDto.setWithholdingRate(tdsMasterObj.get().getRate());
			provisionDto.setDerivedTdsRate(tdsMasterObj.get().getRate());
			provisionDto.setFinalTdsRate(tdsMasterObj.get().getRate());
			provisionDto.setWithholdingAmount(
					provisionAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
			provisionDto.setDerivedTdsAmount(
					provisionAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
			provisionDto.setFinalTdsAmount(
					provisionAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
		}

		deducteeMasterResidentialDAO.provisionSave(provisionDto);

	}

	/**
	 * 
	 * @param userName
	 * @param deducteeMasterResidential
	 * @param natureObj
	 * @param tdsMasterObj
	 * @param month
	 * @param advanceAmount
	 * @param assessmentYear
	 * @param thresholdResponse
	 */
	private void advanceSave(String userName, DeducteeMasterResidential deducteeMasterResidential,
			Optional<NatureOfPaymentMaster> natureObj, Optional<TdsMaster> tdsMasterObj, int month,
			BigDecimal advanceAmount, int assessmentYear, Optional<ThresholdGroupAndNopMapping> thresholdResponse) {

		AdvanceDTO advanceDTO = new AdvanceDTO();
		advanceDTO.setAssessmentYear(assessmentYear);
		advanceDTO.setChallanMonth(month);
		advanceDTO.setAssessmentMonth(month);
		advanceDTO.setAmount(advanceAmount);
		advanceDTO.setDeducteeKey(deducteeMasterResidential.getDeducteeKey());
		advanceDTO.setDeducteeCode(deducteeMasterResidential.getDeducteeCode());
		advanceDTO.setDeductorMasterTan(deducteeMasterResidential.getDeductorTan());
		advanceDTO.setDeductorPan(deducteeMasterResidential.getDeductorPan());
		advanceDTO.setDeducteeName(deducteeMasterResidential.getDeducteeName());
		advanceDTO.setDocumentType("ADV");
		advanceDTO.setIsResident("N");
		advanceDTO.setDeducteePan(deducteeMasterResidential.getDeducteePAN());
		advanceDTO.setSupplyType("TAX");
		LocalDate localDate = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month, 1);
		LocalDate monthDate = localDate.plusDays(localDate.lengthOfMonth() - 1);
		int monthLastDay = monthDate.getDayOfMonth();
		logger.info("month last day: {}", monthLastDay);
		LocalDate localDate1 = LocalDate.of(CommonUtil.getCurrentYearByAssessmentYear(assessmentYear, month), month,
				monthLastDay);
		ZoneId defaultZoneId = ZoneId.systemDefault();
		advanceDTO.setDocumentDate(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		advanceDTO.setDocumentNumber(
				deducteeMasterResidential.getDeducteeCode() + deducteeMasterResidential.getDeducteePAN());
		advanceDTO.setLineItemNumber("1");
		if (natureObj.isPresent()) {
			advanceDTO.setAdvanceNpId(natureObj.get().getId().intValue());
		} else {
			advanceDTO.setAdvanceNpId(null);
		}
		if (thresholdResponse.isPresent()) {
			advanceDTO.setAdvanceGroupid(thresholdResponse.get().getThresholdLimitGroupMaster().getId().intValue());
		} else {
			advanceDTO.setAdvanceGroupid(null);
		}
		advanceDTO.setActive(true);
		advanceDTO.setIsParent(false);
		advanceDTO.setIsExempted(false);
		advanceDTO.setChallanPaid(false);
		advanceDTO.setApprovedForChallan(false);
		advanceDTO.setIsChallanGenerated(false);
		advanceDTO.setCreatedBy(userName);
		advanceDTO.setCreatedDate(new Date());
		advanceDTO.setModifiedBy(userName);
		advanceDTO.setModifiedDate(new Date());
		advanceDTO.setPostingDateOfDocument(Date.from((localDate1.atStartOfDay(defaultZoneId).toInstant())));
		advanceDTO.setMismatch(false);
		advanceDTO.setUnderThreshold(false);
		// 9 columns
		advanceDTO.setWithholdingSection("194Q");
		advanceDTO.setDerivedTdsSection("194Q");
		advanceDTO.setFinalTdsSection("194Q");
		if (tdsMasterObj.isPresent()) {
			advanceDTO.setWithholdingRate(tdsMasterObj.get().getRate());
			advanceDTO.setDerivedTdsRate(tdsMasterObj.get().getRate());
			advanceDTO.setFinalTdsRate(tdsMasterObj.get().getRate());
			advanceDTO.setWithholdingAmount(
					advanceAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
			advanceDTO.setDerivedTdsAmount(
					advanceAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
			advanceDTO.setFinalTdsAmount(
					advanceAmount.multiply(tdsMasterObj.get().getRate()).divide(new BigDecimal(100)));
		}

		deducteeMasterResidentialDAO.advanceSave(advanceDTO);

	}

	/**
	 * 
	 * @param deductorPan
	 * @param userName
	 * @param deducteeMasterResidential
	 * @param amount
	 * @param year
	 * @param deducteeNopGroup
	 * @param nopId
	 */
	private void deducteeNopGroupSave(String deductorPan, String userName,
			DeducteeMasterResidential deducteeMasterResidential, BigDecimal amount, int year,
			DeducteeNopGroup deducteeNopGroup, Long nopId, List<ThresholdLimitGroupMaster> thresholdMasterList) {
		// feign client for get all group id from ThresholdLimitGroupMaster table
		List<ThresholdGroupAndNopMapping> nopList = mastersClient.getThresholdNopGroupData(nopId).getBody().getData();
		for (ThresholdLimitGroupMaster threshold : thresholdMasterList) {
			deducteeNopGroup.setActive(true);
			deducteeNopGroup.setAdvancePending(BigDecimal.ZERO);
			deducteeNopGroup.setAmountUtilized(BigDecimal.ZERO);
			deducteeNopGroup.setThresholdReached(false);
			deducteeNopGroup.setThresholdLimitAmount(threshold.getThresholdAmount());
			if (nopList.get(0).getThresholdLimitGroupMaster().getId().equals(threshold.getId())) {
				deducteeNopGroup.setAmountUtilized(amount);
				if (deducteeNopGroup.getAmountUtilized().compareTo(threshold.getThresholdAmount()) >= 0) {
					deducteeNopGroup.setThresholdReached(true);
				}
			}
			deducteeNopGroup.setCreatedBy(userName);
			deducteeNopGroup.setCreatedDate(new Timestamp(new Date().getTime()));
			if (StringUtils.isNotBlank(deducteeMasterResidential.getDeducteePAN())) {
				deducteeNopGroup.setDeducteePan(deducteeMasterResidential.getDeducteePAN());
			} else {
				deducteeNopGroup.setDeducteeKey(deducteeMasterResidential.getDeducteeKey());
			}
			deducteeNopGroup.setDeductorPan(deductorPan);
			deducteeNopGroup.setGroupNopId(threshold.getId().intValue());
			deducteeNopGroup.setYear(year);
			deducteeNopGroupDAO.save(deducteeNopGroup);
		}
	}

	public void deducteeNopGroupUpdate(DeducteeNopGroup deducteeNop, BigDecimal amount, String userName,
			String deducteePan, List<ThresholdGroupAndNopMapping> nopList,
			List<ThresholdLimitGroupMaster> thresholdMasterList) {
		for (ThresholdLimitGroupMaster threshold : thresholdMasterList) {
			if (nopList.get(0).getThresholdLimitGroupMaster().getId().equals(deducteeNop.getGroupNopId().longValue())
					&& nopList.get(0).getThresholdLimitGroupMaster().getId() == deducteeNop.getGroupNopId()
							.intValue()) {
				deducteeNop.setId(deducteeNop.getId());
				deducteeNop.setModifiedDate(new Timestamp(new Date().getTime()));
				deducteeNop.setModifiedBy(userName);
				deducteeNop.setLastUpdatedDate(new Timestamp(new Date().getTime()));
				deducteeNop.setThresholdReached(false);
				if (deducteeNop.getDeducteeKey() != null) {
					// update the record with pan
					deducteeNop.setDeducteePan(deducteePan);
					deducteeNop.setDeducteeKey(null);
				}
				BigDecimal amountUtilizedDb = deducteeNop.getAmountUtilized() == null ? BigDecimal.ZERO
						: deducteeNop.getAmountUtilized();
				BigDecimal finalAmount = amount.add(amountUtilizedDb);
				deducteeNop.setAmountUtilized(finalAmount);
				if (deducteeNop.getAmountUtilized() != null
						&& deducteeNop.getAmountUtilized().compareTo(threshold.getThresholdAmount()) >= 0) {
					deducteeNop.setThresholdReached(true);
				}
				deducteeNopGroupDAO.update(deducteeNop);
				break;
			}
		}
	}

	/**
	 * 
	 * @param section
	 * @param deducteeMasterResidentialDTO
	 * @param deducteeMasterResidential
	 * @return
	 * @throws ParseException
	 */
	private String getNatureOfPaymentBasedOnHightRateRES(String section,
			DeducteeMasterResidentDTO deducteeMasterResidentialDTO) throws ParseException {
		String nop = StringUtils.EMPTY;
		// get nature of payment and tds rate based on section
		List<Map<String, Object>> nopRateList = mastersClient
				.getListOfNatureAndRate(section, deducteeMasterResidentialDTO.getDeducteeStatus(), "RES").getBody()
				.getData();
		Map<Double, String> rateMap = new HashMap<>();
		List<Double> rates = new ArrayList<>();
		Double highestRate = 0.0;
		if (nopRateList != null && !nopRateList.isEmpty()) {
			for (Map<String, Object> nopData : nopRateList) {
				String nature = (String) nopData.get("nature");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String rateApplicableFrom = (String) nopData.get("applicableFrom");
				String rateApplicableTo = (String) nopData.get("applicableTo");
				Date applicableFrom = formatter.parse(rateApplicableFrom);
				Date applicableTo = null;
				if (StringUtils.isNotBlank(rateApplicableTo)) {
					applicableTo = formatter.parse(rateApplicableTo);
				}
				Double rate = (Double) nopData.get("rate");
				if (deducteeMasterResidentialDTO.getApplicableFrom().getTime() >= applicableFrom.getTime()
						&& (applicableTo == null || deducteeMasterResidentialDTO.getApplicableTo() == null
								|| deducteeMasterResidentialDTO.getApplicableTo().getTime() <= applicableTo
										.getTime())) {
					rate = rate != null ? rate : 0.0;
					rateMap.put(rate, nature);
					rates.add(rate);
				}
			}
		}
		if (deducteeMasterResidentialDTO.getRate() > 0.0 && !rates.isEmpty()) {
			// section contains mutiple NOP then get the NOP based on closest rate passed in
			// the excel
			Optional<Double> rate = rates.parallelStream()
					.min(Comparator.comparingDouble(i -> Math.abs(i - (deducteeMasterResidentialDTO.getRate()))));
			nop = rateMap.get(rate.isPresent() ? rate.get() : 0.0);
		} else {
			nop = rateMap.get(highestRate);
		}
		return nop;
	}

	private Map<String, String> getJsonForadditionalSectionCode(Map<String, String> newSectionAndSectionCode,
			DeducteeMasterResidential deductee) throws JsonMappingException, JsonProcessingException {

		Map<String, String> dbSectionSectionCode = new HashMap<>();

		// getting values from different columns and putting into map so that we can add
		// it in
		// additional section column
		if (deductee != null) {
			if (StringUtils.isNotBlank(deductee.getSectionCode())) {
				newSectionAndSectionCode.put(deductee.getSectionCode(), deductee.getSection());
			}
			// getting the existing data in additional_section_code column
			if (StringUtils.isNotBlank(deductee.getAdditionalSectionCode())) {

				Map<String, String> map = objectMapper.readValue(deductee.getAdditionalSectionCode(),
						new TypeReference<Map<String, String>>() {
						});
				for (Entry<String, String> d : map.entrySet()) {
					dbSectionSectionCode.put(d.getKey(), d.getValue());
				}
				dbSectionSectionCode.putAll(map);
			}
			newSectionAndSectionCode.putAll(dbSectionSectionCode);
		}
		return newSectionAndSectionCode;

	}

	private Map<String, Integer> getJsonForadditionalSectionThreshold(Map<String, Integer> sectionsAndThreshold,
			DeducteeMasterResidential deducteeDB) throws JsonMappingException, JsonProcessingException {

		Map<String, Integer> dbSectionThreshold = new HashMap<>();

		// getting values from different columns and putting into map so that we can add
		// it in
		// additional section column
		if (deducteeDB != null) {
			if (StringUtils.isNotBlank(deducteeDB.getSection())) {
				Integer threshold = (deducteeDB.getIsThresholdLimitApplicable() != null
						&& deducteeDB.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
				sectionsAndThreshold.put(deducteeDB.getSection(), threshold);
			}
			// getting the existing data in additional_section_code column
			if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionThresholds())) {

				Map<String, Integer> map = objectMapper.readValue(deducteeDB.getAdditionalSectionThresholds(),
						new TypeReference<Map<String, Integer>>() {
						});
				for (Entry<String, Integer> d : map.entrySet()) {
					dbSectionThreshold.put(d.getKey(), d.getValue());
				}
				dbSectionThreshold.putAll(map);
			}
			sectionsAndThreshold.putAll(dbSectionThreshold);
		}
		return sectionsAndThreshold;

	}

	private Map<String, Integer> getJsonForadditionalSectionThresholdNr(Map<String, Integer> sectionsAndThreshold,
			DeducteeMasterNonResidential deducteeDB) throws JsonMappingException, JsonProcessingException {

		Map<String, Integer> dbSectionThreshold = new HashMap<>();

		// getting values from different columns and putting into map so that we can add
		// it in
		// additional section column
		if (deducteeDB != null) {
			if (StringUtils.isNotBlank(deducteeDB.getSection())) {
				Integer threshold = (deducteeDB.getIsThresholdLimitApplicable() != null
						&& deducteeDB.getIsThresholdLimitApplicable().equals(true)) ? 1 : 0;
				sectionsAndThreshold.put(deducteeDB.getSection(), threshold);
			}
			// getting the existing data in additional_section_code column
			if (StringUtils.isNotBlank(deducteeDB.getAdditionalSectionThresholds())) {

				Map<String, Integer> map = objectMapper.readValue(deducteeDB.getAdditionalSectionThresholds(),
						new TypeReference<Map<String, Integer>>() {
						});
				for (Entry<String, Integer> d : map.entrySet()) {
					dbSectionThreshold.put(d.getKey(), d.getValue());
				}
				dbSectionThreshold.putAll(map);
			}
			sectionsAndThreshold.putAll(dbSectionThreshold);
		}
		return sectionsAndThreshold;

	}

	/**
	 * @param key
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	public DeducteeMasterDTO getResidential(String deductorPan, Integer id)
			throws JsonMappingException, JsonProcessingException {
		DeducteeMasterDTO deducteeMasterDTO = new DeducteeMasterDTO();
		AddressDTO address = new AddressDTO();
		Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
		AdditionalSectionsDTO additionalSections = null;
		DeducteeMasterResidential deducteeMasterResidential = null;
		List<DeducteeMasterResidential> deducteeMasterResidentialOptional = deducteeMasterResidentialDAO
				.findById(deductorPan, id);
		if (!deducteeMasterResidentialOptional.isEmpty() && deducteeMasterResidentialOptional != null) {
			deducteeMasterResidential = deducteeMasterResidentialOptional.get(0);
			deducteeMasterDTO
					.setTdsApplicabilityUnderSection(deducteeMasterResidential.getTdsApplicabilityUnderSection());
			BeanUtils.copyProperties(deducteeMasterResidential, deducteeMasterDTO);
			deducteeMasterDTO.setId(deducteeMasterResidential.getDeducteeMasterId());
			address.setAreaLocality(deducteeMasterResidential.getAreaLocality());
			address.setFlatDoorBlockNo(deducteeMasterResidential.getFlatDoorBlockNo());
			address.setNameBuildingVillage(deducteeMasterResidential.getNameBuildingVillage());
			address.setPinCode(deducteeMasterResidential.getPinCode());
			address.setRoadStreetPostoffice(deducteeMasterResidential.getRoadStreetPostoffice());
			if (StringUtils.isNotBlank(deducteeMasterResidential.getCountry())) {
				address.setCountryName(deducteeMasterResidential.getCountry().toUpperCase());
			} else {
				address.setCountryName(deducteeMasterResidential.getCountry());
			}
			if (StringUtils.isNotBlank(deducteeMasterResidential.getState())) {
				address.setStateName(deducteeMasterResidential.getState().toUpperCase());
			} else {
				address.setStateName(deducteeMasterResidential.getState());
			}
			address.setTownCityDistrict(deducteeMasterResidential.getTownCityDistrict());
			Map<String, Float> additionalSectionss = null;
			Map<String, String> additionalSectionCodes = null;
			Map<String, Integer> additionalSectionThresholds = null;
			Map<String, String> swappedMap = new HashMap<>();
			Map<String, Integer> thresholdMap = new HashMap<>();
			if (deducteeMasterResidential.getAdditionalSections() != null) {
				additionalSectionss = objectMapper.readValue(deducteeMasterResidential.getAdditionalSections(),
						new TypeReference<Map<String, Float>>() {
						});
			}
			if (deducteeMasterResidential.getAdditionalSectionCode() != null) {
				additionalSectionCodes = objectMapper.readValue(deducteeMasterResidential.getAdditionalSectionCode(),
						new TypeReference<Map<String, String>>() {
						});
				for (Map.Entry<String, String> entry : additionalSectionCodes.entrySet()) {
					swappedMap.put(entry.getValue(), entry.getKey());
				}
			}
			if (deducteeMasterResidential.getAdditionalSectionThresholds() != null) {
				additionalSectionThresholds = objectMapper.readValue(
						deducteeMasterResidential.getAdditionalSectionThresholds(),
						new TypeReference<Map<String, Integer>>() {
						});
				for (Map.Entry<String, Integer> entry : additionalSectionThresholds.entrySet()) {
					thresholdMap.put(entry.getKey(), entry.getValue());
				}
			}
			if (deducteeMasterResidential.getIsDeducteeHasAdditionalSections() != null && additionalSectionss != null) {
				for (Map.Entry<String, Float> entry : additionalSectionss.entrySet()) {
					additionalSections = new AdditionalSectionsDTO();
					String section = StringUtils.substringBefore(entry.getKey(), "-");
					String nop = StringUtils.substringAfter(entry.getKey(), "-");
					additionalSections.setSection(section);
					additionalSections.setNatureOfPayment(nop);
					additionalSections.setRate(entry.getValue());
					additionalSections.setSectionCode(swappedMap.get(section));
					Boolean threshold = (thresholdMap.get(section) != null && thresholdMap.get(section) == 1) ? true
							: false;
					additionalSections.setIsThresholdApplicable(threshold);
					additionalSectionsSet.add(additionalSections);

				}
			}
			deducteeMasterDTO.setAdditionalSections(additionalSectionsSet);
			deducteeMasterDTO.setAddress(address);
			deducteeMasterDTO.setCreatedDate(deducteeMasterResidential.getCreatedDate());
			deducteeMasterDTO.setRate(deducteeMasterResidential.getRate());
			deducteeMasterDTO.setTdsExcemptionFlag(deducteeMasterResidential.getTdsExcemptionFlag());
			deducteeMasterDTO.setTdsExcemptionReason(deducteeMasterResidential.getTdsExcemptionReason());
			deducteeMasterDTO.setDeducteeTan(deducteeMasterResidential.getDeducteeTan());
			deducteeMasterDTO.setOpeningBalanceCreditNote(deducteeMasterResidential.getOpeningBalanceCreditNote());
			deducteeMasterDTO.setTdsExemptionNumber(deducteeMasterResidential.getTdsExemptionNumber());
			deducteeMasterDTO.setDeducteeGSTIN(deducteeMasterResidential.getDeducteeGSTIN());
			deducteeMasterDTO.setGrOrIRIndicator(deducteeMasterResidential.getGrOrIRIndicator());
		}
		return deducteeMasterDTO;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param deducteeName
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public CommonDTO<DeducteeMasterDTO> getListOfResidentialDeductees(String deductorPan, Pagination pagination,
			String deducteeName, String deducteeCode) throws JsonMappingException, JsonProcessingException {
		List<DeducteeMasterDTO> deducteeMasterDTOList = new ArrayList<>();
		DeducteeMasterDTO deducteeMasterDTO = null;
		logger.info("deductee name ---- : {}", deducteeName);
		List<DeducteeMasterResidential> residentList = null;
		if ("nodeducteename".equalsIgnoreCase(deducteeName) && "nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			residentList = deducteeMasterResidentialDAO.findAllByPan(deductorPan, pagination);
		} else if (!"nodeducteename".equalsIgnoreCase(deducteeName)
				&& "nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			residentList = deducteeMasterResidentialDAO.findAllByDeducteeNamePan(deductorPan, deducteeName, pagination);
		} else if ("nodeducteename".equalsIgnoreCase(deducteeName)
				&& !"nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			residentList = deducteeMasterResidentialDAO.findAllByDeducteeCode(deductorPan, deducteeCode, pagination);
		} else if (!"nodeducteename".equalsIgnoreCase(deducteeName)
				&& !"nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			residentList = deducteeMasterResidentialDAO.findAllByDeducteeNameAndCode(deductorPan, deducteeName,
					deducteeCode, pagination);
		}
		logger.info("LIST OF RESIDENTIAL DEDUCTEES : {}", residentList.size());
		if (!residentList.isEmpty() && residentList != null) {
			for (DeducteeMasterResidential deducteeMasterResidential : residentList) {
				logger.info("PAN STATUS : {}", deducteeMasterResidential.getPanStatus());
				deducteeMasterDTO = new DeducteeMasterDTO();
				AddressDTO address = new AddressDTO();
				AdditionalSectionsDTO additionalSections = null;
				Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
				BeanUtils.copyProperties(deducteeMasterResidential, deducteeMasterDTO);
				deducteeMasterDTO.setId(deducteeMasterResidential.getDeducteeMasterId());
				address.setAreaLocality(deducteeMasterResidential.getAreaLocality());
				address.setCountryName(deducteeMasterResidential.getCountry());
				address.setFlatDoorBlockNo(deducteeMasterResidential.getFlatDoorBlockNo());
				address.setNameBuildingVillage(deducteeMasterResidential.getNameBuildingVillage());
				address.setPinCode(deducteeMasterResidential.getPinCode());
				address.setRoadStreetPostoffice(deducteeMasterResidential.getRoadStreetPostoffice());
				address.setStateName(deducteeMasterResidential.getState());
				address.setTownCityDistrict(deducteeMasterResidential.getTownCityDistrict());
				Map<String, Float> additionalSectionss = null;
				if (deducteeMasterResidential.getAdditionalSections() != null) {
					additionalSectionss = objectMapper.readValue(deducteeMasterResidential.getAdditionalSections(),
							new TypeReference<Map<String, Float>>() {
							});
				}
				if (additionalSectionss != null) {
					for (Map.Entry<String, Float> entry : additionalSectionss.entrySet()) {
						additionalSections = new AdditionalSectionsDTO();
						// splitting the section code and picking section
						additionalSections.setSection(entry.getKey());
						additionalSections.setRate(entry.getValue());
						additionalSectionsSet.add(additionalSections);
					}
				}
				deducteeMasterDTO.setAdditionalSections(additionalSectionsSet);
				deducteeMasterDTO.setAddress(address);
				deducteeMasterDTO.setPanStatus(deducteeMasterResidential.getPanStatus());
				deducteeMasterDTO.setPanVerifiedDate(deducteeMasterResidential.getPanVerifiedDate());
				deducteeMasterDTO.setDeducteePAN(deducteeMasterResidential.getDeducteePAN());
				// Total deductee sections count
				int sectionsCount = StringUtils.isBlank(deducteeMasterResidential.getSection()) ? 0 : 1;
				deducteeMasterDTO.setTotalNoOfSections(sectionsCount + additionalSectionsSet.size());
				deducteeMasterDTOList.add(deducteeMasterDTO);
			}
		}
		BigInteger deducteeCount = deducteeMasterResidentialDAO.getAllDeducteeResidentialCount(deductorPan);

		PagedData<DeducteeMasterDTO> pagedData = new PagedData<>(deducteeMasterDTOList, residentList.size(),
				pagination.getPageNumber(),
				deducteeCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<DeducteeMasterDTO> deducteeMasterData = new CommonDTO<>();
		deducteeMasterData.setResultsSet(pagedData);
		deducteeMasterData.setCount(deducteeCount);
		return deducteeMasterData;
	}

	public DeducteeMasterResidential updateResident(DeducteeMasterResidentDTO deducteeMasterResidentDTO,
			String deductorPan, String userName) throws JsonProcessingException {
		logger.info("REST request to update a Deductee Residnet record : {}", deducteeMasterResidentDTO);

		List<DeducteeMasterResidential> deducteeMasterResidentialOptional = deducteeMasterResidentialDAO
				.findById(deductorPan, deducteeMasterResidentDTO.getId());
		DeducteeMasterResidential deducteeMasterResidential = null;
		if (!deducteeMasterResidentialOptional.isEmpty()) {
			deducteeMasterResidential = deducteeMasterResidentialOptional.get(0);
			deducteeMasterResidential.setDeducteeName(deducteeMasterResidentDTO.getDeducteeName());
			deducteeMasterResidential
					.setDeducteeCode(StringUtils.isNotBlank(deducteeMasterResidentDTO.getDeducteeCode())
							? deducteeMasterResidentDTO.getDeducteeCode().trim()
							: "");

			deducteeMasterResidential.setDeducteePAN(StringUtils.isNotBlank(deducteeMasterResidentDTO.getDeducteePAN())
					? deducteeMasterResidentDTO.getDeducteePAN().trim()
					: "");
			deducteeMasterResidential.setDeducteeStatus(deducteeMasterResidentDTO.getDeducteeStatus());
			deducteeMasterResidential
					.setDeducteeResidentialStatus(deducteeMasterResidentDTO.getDeducteeResidentialStatus());
			deducteeMasterResidential.setDeducteeAadharNumber(deducteeMasterResidentDTO.getDeducteeAadharNumber());
			deducteeMasterResidential.setDefaultRate(BigDecimal.valueOf(deducteeMasterResidentDTO.getDefaultRate()));
			deducteeMasterResidential.setEmailAddress(deducteeMasterResidentDTO.getEmailAddress());
			deducteeMasterResidential.setPhoneNumber(
					deducteeMasterResidentDTO.getPhoneNumber() != null ? deducteeMasterResidentDTO.getPhoneNumber()
							: "");
			deducteeMasterResidential.setFlatDoorBlockNo(deducteeMasterResidentDTO.getAddress().getFlatDoorBlockNo());
			deducteeMasterResidential
					.setNameBuildingVillage(deducteeMasterResidentDTO.getAddress().getNameBuildingVillage());
			deducteeMasterResidential
					.setRoadStreetPostoffice(deducteeMasterResidentDTO.getAddress().getRoadStreetPostoffice());
			deducteeMasterResidential.setAreaLocality(deducteeMasterResidentDTO.getAddress().getAreaLocality());
			deducteeMasterResidential.setTownCityDistrict(deducteeMasterResidentDTO.getAddress().getTownCityDistrict());
			deducteeMasterResidential.setState(deducteeMasterResidentDTO.getAddress().getStateName());
			deducteeMasterResidential.setCountry(deducteeMasterResidentDTO.getAddress().getCountryName());
			deducteeMasterResidential.setPinCode(deducteeMasterResidentDTO.getAddress().getPinCode());
			deducteeMasterResidential.setModifiedBy(userName);
			deducteeMasterResidential.setModifiedDate(new Timestamp(new Date().getTime()));
			// set section and rate later
			deducteeMasterResidential.setSection(deducteeMasterResidentDTO.getSection());
			deducteeMasterResidential.setRate(BigDecimal.valueOf(deducteeMasterResidentDTO.getRate()));
			deducteeMasterResidential
					.setIsDeducteeHasAdditionalSections(deducteeMasterResidentDTO.getIsDeducteeHasAdditionalSections());
			deducteeMasterResidential.setApplicableFrom(deducteeMasterResidentDTO.getApplicableFrom());
			deducteeMasterResidential.setApplicableTo(deducteeMasterResidentDTO.getApplicableTo());
			deducteeMasterResidentialDAO.updateResidential(deducteeMasterResidential);
		}
		return deducteeMasterResidential;
	}

	// non resident
	public DeducteeMasterNonResidential createNonResident(
			DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO, MultipartFile trcFile,
			MultipartFile tenFFile, MultipartFile wpeFile, MultipartFile noPEFile, String deductorPan, String userName,
			MultipartFile isFixedBasedIndiaFile)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {

		DeducteeMasterNonResidential deducteeMasterNonResidential = new DeducteeMasterNonResidential();
		BeanUtils.copyProperties(deducteeMasterNonResidentialDTO, deducteeMasterNonResidential);
		// deducteee key
		if (StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteeCode())) {
			deducteeMasterNonResidential.setDeducteeKey(deducteeMasterNonResidentialDTO.getDeducteeCode());
		} else {
			String name = deducteeMasterNonResidentialDTO.getDeducteeName().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			if (StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteePAN())) {
				deducteeMasterNonResidential
						.setDeducteeKey(name.concat(deducteeMasterNonResidentialDTO.getDeducteePAN()));
			} else {
				deducteeMasterNonResidential.setDeducteeKey(name);
			}
		}

		List<DeducteeMasterNonResidential> deducteeDbDetails = deducteeMasterNonResidentialDAO
				.findAllByDeducteeKey(deductorPan, deducteeMasterNonResidential.getDeducteeKey());
		// validation check
		if (!deducteeDbDetails.isEmpty()) {
			CommonValidationsCassandra.validateApplicableFields(deducteeDbDetails.get(0).getApplicableTo(),
					deducteeMasterNonResidential.getApplicableFrom());
		}
		deducteeMasterNonResidential.setDeductorPan(deductorPan);
		deducteeMasterNonResidential.setDeducteeName(deducteeMasterNonResidentialDTO.getDeducteeName().trim());
		String deducteeName = deducteeMasterNonResidential.getDeducteeName().toLowerCase();
		deducteeName = deducteeName.replaceAll("[^a-z0-9 ]", "");
		deducteeMasterNonResidential.setModifiedName(deducteeName);
		deducteeMasterNonResidential
				.setDeducteeCode(StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteeCode())
						? deducteeMasterNonResidentialDTO.getDeducteeCode().trim()
						: "");

		deducteeMasterNonResidential
				.setDeducteePAN(StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteePAN())
						? deducteeMasterNonResidentialDTO.getDeducteePAN().trim()
						: "");
		deducteeMasterNonResidential.setDeducteeTin(deducteeMasterNonResidentialDTO.getDeducteeTin());
		deducteeMasterNonResidential.setPanStatus("");
		deducteeMasterNonResidential.setDeducteeStatus(deducteeMasterNonResidentialDTO.getDeducteeStatus());
		deducteeMasterNonResidential
				.setDeducteeResidentialStatus(deducteeMasterNonResidentialDTO.getDeducteeResidentialStatus());
		deducteeMasterNonResidential.setDeducteeAadharNumber(deducteeMasterNonResidentialDTO.getDeducteeAadharNumber());
		deducteeMasterNonResidential.setEmailAddress(deducteeMasterNonResidentialDTO.getEmailAddress());
		deducteeMasterNonResidential.setPhoneNumber(deducteeMasterNonResidentialDTO.getPhoneNumber() != null
				? deducteeMasterNonResidentialDTO.getPhoneNumber()
				: "");
		deducteeMasterNonResidential
				.setFlatDoorBlockNo(deducteeMasterNonResidentialDTO.getAddress().getFlatDoorBlockNo());
		deducteeMasterNonResidential
				.setNameBuildingVillage(deducteeMasterNonResidentialDTO.getAddress().getNameBuildingVillage());
		deducteeMasterNonResidential
				.setRoadStreetPostoffice(deducteeMasterNonResidentialDTO.getAddress().getRoadStreetPostoffice());
		deducteeMasterNonResidential.setAreaLocality(deducteeMasterNonResidentialDTO.getAddress().getAreaLocality());
		// stateid
		deducteeMasterNonResidential.setState(deducteeMasterNonResidentialDTO.getAddress().getStateName());
		deducteeMasterNonResidential.setCountry(deducteeMasterNonResidentialDTO.getAddress().getCountryName());
		deducteeMasterNonResidential
				.setTownCityDistrict(deducteeMasterNonResidentialDTO.getAddress().getTownCityDistrict());
		deducteeMasterNonResidential.setPinCode(deducteeMasterNonResidentialDTO.getAddress().getPinCode());

		// set section and rate
		if (StringUtils.isEmpty(deducteeMasterNonResidentialDTO.getSection())) {
			deducteeMasterNonResidential.setRate(BigDecimal.ZERO);
		} else if (deducteeMasterNonResidentialDTO.getRate() != 0) {
			deducteeMasterNonResidential.setRate(BigDecimal.valueOf(deducteeMasterNonResidentialDTO.getRate())
					.setScale(4, BigDecimal.ROUND_HALF_DOWN));
		} else {
			deducteeMasterNonResidential.setRate(BigDecimal.ZERO);
		}
		deducteeMasterNonResidential.setSection(deducteeMasterNonResidentialDTO.getSection());
		// nature of payment
		if (StringUtils.isBlank(deducteeMasterNonResidentialDTO.getNatureOfPayment())) {
			String nop = getNatureOfPaymentBasedOnHightRateNR(deducteeMasterNonResidentialDTO.getSection(),
					deducteeMasterNonResidentialDTO);
			deducteeMasterNonResidential.setNatureOfPayment(nop);
		} else {
			deducteeMasterNonResidential.setNatureOfPayment(deducteeMasterNonResidentialDTO.getNatureOfPayment());
		}
		deducteeMasterNonResidential.setDefaultRate(BigDecimal.valueOf(deducteeMasterNonResidentialDTO.getDefaultRate())
				.setScale(4, BigDecimal.ROUND_HALF_DOWN));
		deducteeMasterNonResidential.setIsDeducteeHasAdditionalSections(
				deducteeMasterNonResidentialDTO.getIsDeducteeHasAdditionalSections());
		deducteeMasterNonResidential.setCreatedDate(new Timestamp(new Date().getTime()));
		deducteeMasterNonResidential.setCreatedBy(userName);
		deducteeMasterNonResidential.setModifiedBy(userName);
		deducteeMasterNonResidential.setModifiedDate(new Timestamp(new Date().getTime()));
		deducteeMasterNonResidential.setActive(true);
		deducteeMasterNonResidential
				.setIsThresholdLimitApplicable(deducteeMasterNonResidentialDTO.getIsThresholdLimitApplicable());
		deducteeMasterNonResidential.setSectionCode(deducteeMasterNonResidentialDTO.getSectionCode());
		// Deductee Enrichment Key
		if (StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteeCode())) {
			deducteeMasterNonResidential.setDeducteeEnrichmentKey(deducteeMasterNonResidentialDTO.getDeducteeCode());
		} else {
			String name = deducteeMasterNonResidentialDTO.getDeducteeName().toLowerCase();
			name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
			deducteeMasterNonResidential.setDeducteeEnrichmentKey(name);
		}
		Map<String, String> newSectionAndSectionCode = new HashMap<>();
		Map<String, Integer> sectionsAndThreshold = new HashMap<>();
		Map<String, Float> additionalSection = new HashMap<>();
		boolean isAdditionalSection = false;
		Map<String, Float> newSections = new HashMap<>();
		if (deducteeMasterNonResidentialDTO.getIsDeducteeHasAdditionalSections() != null
				&& !deducteeMasterNonResidentialDTO.getIsDeducteeHasAdditionalSections().equals(false)) {
			for (AdditionalSectionsDTO additionalSections : deducteeMasterNonResidentialDTO.getAdditionalSections()) {
				if (StringUtils.isBlank(deducteeMasterNonResidentialDTO.getSection())) {
					if (StringUtils.isNotBlank(additionalSections.getSection())) {
						deducteeMasterNonResidential.setSection(additionalSections.getSection());
						// nature of payment
						if (StringUtils.isBlank(additionalSections.getNatureOfPayment())) {
							String nop = getNatureOfPaymentBasedOnHightRateNR(additionalSections.getNatureOfPayment(),
									deducteeMasterNonResidentialDTO);
							deducteeMasterNonResidential.setNatureOfPayment(nop);
						} else {
							deducteeMasterNonResidential.setNatureOfPayment(additionalSections.getNatureOfPayment());
						}
						deducteeMasterNonResidential.setIsDeducteeHasAdditionalSections(isAdditionalSection);
						deducteeMasterNonResidential.setSectionCode(additionalSections.getSectionCode());
						if (additionalSections.getRate() != 0) {
							deducteeMasterNonResidential.setRate(BigDecimal.valueOf(additionalSections.getRate())
									.setScale(4, BigDecimal.ROUND_HALF_DOWN));
						} else {
							deducteeMasterNonResidential.setRate(BigDecimal.ZERO);
						}
						newSections.put(deducteeMasterNonResidential.getSection(),
								deducteeMasterNonResidential.getRate().floatValue());
					}
				} else if (StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getSection())
						&& deducteeMasterNonResidentialDTO.getSection().equals(additionalSections.getSection())) {
					throw new CustomException("Duplicate sections are not allowed", HttpStatus.BAD_REQUEST);
				} else if (StringUtils.isNotBlank(additionalSections.getSection())) {
					String sectionAndNop = StringUtils.EMPTY;
					if (StringUtils.isBlank(additionalSections.getNatureOfPayment())) {
						String nop = getNatureOfPaymentBasedOnHightRateNR(additionalSections.getSection(),
								deducteeMasterNonResidentialDTO);
						sectionAndNop = additionalSections.getSection() + "-" + nop;
					} else {
						sectionAndNop = additionalSections.getSection() + "-" + additionalSections.getNatureOfPayment();
					}
					if (additionalSections.getRate() != 0) {
						additionalSection.put(sectionAndNop, additionalSections.getRate());
					} else {
						additionalSection.put(sectionAndNop, (float) 0);
					}
					additionalSections.setRate((additionalSections.getRate() != 0 ? additionalSections.getRate() : 0));
					newSections.put(additionalSections.getSection(), additionalSections.getRate());
					if (StringUtils.isNotBlank(additionalSections.getSectionCode())) {
						newSectionAndSectionCode.put(additionalSections.getSectionCode(),
								additionalSections.getSection());
					}
					Integer threshold = (additionalSections.getIsThresholdApplicable() != null
							&& additionalSections.getIsThresholdApplicable().equals(true)) ? 1 : 0;
					sectionsAndThreshold.put(additionalSections.getSection(), threshold);
				}
				if (StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getSectionCode())
						&& StringUtils.isNotBlank(additionalSections.getSectionCode())
						&& deducteeMasterNonResidentialDTO.getSectionCode()
								.equals(additionalSections.getSectionCode())) {
					throw new CustomException("Duplicate section Codes are not allowed", HttpStatus.BAD_REQUEST);
				}
			}
		}

		DeducteeMasterNonResidential deducteeDB = null;
		for (DeducteeMasterNonResidential deductee : deducteeDbDetails) {
			if (deductee.getApplicableTo() == null || deductee.getApplicableTo().after(new Date())) {
				deducteeDB = deductee;
				break;
			}
		}
		if (deducteeDB != null) {
			Map<String, Float> dBsectionsAndNop = new HashMap<>();
			Map<String, Float> dbSections = new HashMap<>();
			if (StringUtils.isNotBlank(deducteeDB.getSection())) {
				dBsectionsAndNop.put(
						deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment() + "~"
								+ deducteeDB.getSectionCode(),
						(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
				dbSections.put(deducteeDB.getSection(),
						(deducteeDB.getRate() != null ? deducteeDB.getRate().floatValue() : 0));
			}
			if (StringUtils.isNotBlank(deducteeDB.getAdditionalSections())) {
				Map<String, Float> map = objectMapper.readValue(deducteeDB.getAdditionalSections(),
						new TypeReference<Map<String, Float>>() {
						});
				for (Entry<String, Float> d : map.entrySet()) {
					dBsectionsAndNop.put(d.getKey(), d.getValue());
				}
				dBsectionsAndNop.putAll(map);
			}
			float rate = (deducteeMasterNonResidential.getRate() != null
					? deducteeMasterNonResidential.getRate().floatValue()
					: 0);
			newSections.put(
					deducteeMasterNonResidential.getSection() != null ? deducteeMasterNonResidential.getSection() : "",
					rate);
			if (additionalSection != null) {
				newSections.putAll(additionalSection);
			}
			if (StringUtils.isBlank(deducteeMasterNonResidential.getSection())) {
				deducteeMasterNonResidential.setSection(deducteeDB.getSection());
				deducteeMasterNonResidential.setNatureOfPayment(deducteeDB.getNatureOfPayment());
				deducteeMasterNonResidential.setRate(deducteeDB.getRate());
				deducteeMasterNonResidential.setSectionCode(deducteeDB.getSectionCode());
			}
			boolean isduplicateSections = dbSections.keySet().equals(newSections.keySet());
			if (!isduplicateSections) {
				Map<String, Float> sections = new HashMap<>();
				for (Entry<String, Float> dBsectionAndNop : dBsectionsAndNop.entrySet()) {
					String dBSection = StringUtils.substringBefore(dBsectionAndNop.getKey(), "-");
					if (!newSections.containsKey(dBSection)
							&& !deducteeMasterNonResidential.getSection().equals(dBSection)) {
						if (StringUtils.isEmpty(dBsectionAndNop.getKey())) {
							deducteeMasterNonResidential.setIsDeducteeHasAdditionalSections(false);
						} else if (dBSection.equals(deducteeDB.getSection())) {
							String sectionAndNop = deducteeDB.getSection() + "-" + deducteeDB.getNatureOfPayment();
							sections.put(sectionAndNop, dBsectionAndNop.getValue());
						} else {
							sections.put(dBsectionAndNop.getKey(), dBsectionAndNop.getValue());
						}
						additionalSection.putAll(sections);
					}
				}
			}
			if (additionalSection.size() > 0) {
				deducteeMasterNonResidential.setIsDeducteeHasAdditionalSections(true);
			}

			deducteeDB.setActive(false);
			deducteeDB.setApplicableTo(subtractDay(new Date()));
			deducteeMasterNonResidentialDAO.updateApplicableTo(deducteeDB);
		}
		newSectionAndSectionCode = getJsonForadditionalSectionCodeNR(newSectionAndSectionCode, deducteeDB);
		String additionalSections = objectMapper.writeValueAsString(additionalSection);
		String additionaalSectionCode = objectMapper.writeValueAsString(newSectionAndSectionCode);
		deducteeMasterNonResidential.setAdditionalSectionCode(additionaalSectionCode);
		deducteeMasterNonResidential.setAdditionalSections(additionalSections);
		sectionsAndThreshold = getJsonForadditionalSectionThresholdNr(sectionsAndThreshold, deducteeDB);
		String additionalSectionThresholds = objectMapper.writeValueAsString(sectionsAndThreshold);
		deducteeMasterNonResidential.setAdditionalSectionThresholds(additionalSectionThresholds);
		deducteeMasterNonResidential.setApplicableFrom(deducteeMasterNonResidentialDTO.getApplicableFrom());
		deducteeMasterNonResidential.setApplicableTo(deducteeMasterNonResidentialDTO.getApplicableTo());
		deducteeMasterNonResidential.setCountryOfResidence(deducteeMasterNonResidentialDTO.getCountryOfResidence());
		deducteeMasterNonResidential.setIsTRCAvailable(deducteeMasterNonResidentialDTO.getIsTRCAvailable());
		if (deducteeMasterNonResidentialDTO.getIsTRCAvailable().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getTrcApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getTrcApplicableTo() != null && (deducteeMasterNonResidentialDTO
						.getTrcApplicableFrom().equals(deducteeMasterNonResidentialDTO.getTrcApplicableTo())
						|| deducteeMasterNonResidentialDTO.getTrcApplicableFrom()
								.after(deducteeMasterNonResidentialDTO.getTrcApplicableTo()))) {
					throw new CustomException(
							"Trc Applicable From Date should not be equals or greater than Trc Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential
							.setTrcApplicableFrom(deducteeMasterNonResidentialDTO.getTrcApplicableFrom());
					deducteeMasterNonResidential
							.setTrcApplicableTo(deducteeMasterNonResidentialDTO.getTrcApplicableTo());
				}
			} else {
				throw new CustomException("Trc Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		}
		if (trcFile != null) {
			String trcUri = blob.uploadExcelToBlob(trcFile);
			deducteeMasterNonResidential.setTrcFileAddress(trcUri);
		}

		deducteeMasterNonResidential.setIsTenFAvailable(deducteeMasterNonResidentialDTO.getIsTenFAvailable());
		if (deducteeMasterNonResidentialDTO.getIsTenFAvailable().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getTenFApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getTenFApplicableTo() != null && (deducteeMasterNonResidentialDTO
						.getTenFApplicableFrom().equals(deducteeMasterNonResidentialDTO.getTenFApplicableTo())
						|| deducteeMasterNonResidentialDTO.getTenFApplicableFrom()
								.after(deducteeMasterNonResidentialDTO.getTenFApplicableTo()))) {
					throw new CustomException(
							"TenF Applicable From Date should not be equals or greater than TenF Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential
							.setTenFApplicableFrom(deducteeMasterNonResidentialDTO.getTenFApplicableFrom());
					deducteeMasterNonResidential
							.setTenFApplicableTo(deducteeMasterNonResidentialDTO.getTenFApplicableTo());
				}
			} else {
				throw new CustomException("TenF Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		}
		if (tenFFile != null) {
			String tenfUri = blob.uploadExcelToBlob(tenFFile);
			deducteeMasterNonResidential.setForm10fFileAddress(tenfUri);
		}
		deducteeMasterNonResidential.setWhetherPEInIndia(deducteeMasterNonResidentialDTO.getWeatherPEInIndia());
		if (deducteeMasterNonResidentialDTO.getWeatherPEInIndia().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableTo() != null
						&& (deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableFrom()
								.equals(deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableTo())
								|| deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableFrom()
										.after(deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableTo()))) {
					throw new CustomException(
							"Weather PE In India Applicable From Date should not be equals or greater than Weather PE In India Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential.setWhetherPEInIndiaApplicableFrom(
							deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableFrom());
					deducteeMasterNonResidential.setWhetherPEInIndiaApplicableTo(
							deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableTo());
				}
			} else {
				throw new CustomException("Weather PE In India Applicable From date is Mandatory",
						HttpStatus.BAD_REQUEST);
			}
		}
		if (deducteeMasterNonResidentialDTO.getWeatherPEInIndia() != null && wpeFile != null) {
			String wpeUri = blob.uploadExcelToBlob(wpeFile);
			deducteeMasterNonResidential.setPeFileAddress(wpeUri);
		}

		deducteeMasterNonResidential.setIsPOEMavailable(deducteeMasterNonResidentialDTO.getIsPOEMavailable());
		if (deducteeMasterNonResidentialDTO.getIsPOEMavailable().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getPoemApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getPoemApplicableTo() != null && (deducteeMasterNonResidentialDTO
						.getPoemApplicableFrom().equals(deducteeMasterNonResidentialDTO.getPoemApplicableTo())
						|| deducteeMasterNonResidentialDTO.getPoemApplicableFrom()
								.after(deducteeMasterNonResidentialDTO.getPoemApplicableTo()))) {
					throw new CustomException(
							"POEM Applicable From Date should not be equals or greater than POEM Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential
							.setPoemApplicableFrom(deducteeMasterNonResidentialDTO.getPoemApplicableFrom());
					deducteeMasterNonResidential
							.setPoemApplicableTo(deducteeMasterNonResidentialDTO.getPoemApplicableTo());
				}
			} else {
				throw new CustomException("POEM Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		}

		deducteeMasterNonResidential.setNoPEDocumentAvailable(deducteeMasterNonResidentialDTO.getIsPEdocument());
		deducteeMasterNonResidential.setIsPEdocument(deducteeMasterNonResidentialDTO.getIsPEdocument());
		if (deducteeMasterNonResidentialDTO.getNoPEDocumentAvaliable() != null && noPEFile != null) {
			String noPEUri = blob.uploadExcelToBlob(noPEFile);
			deducteeMasterNonResidential.setNoPeDocAddress(noPEUri);
		}
		if (deducteeMasterNonResidentialDTO.getIsPEdocument().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getWpeApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getWpeApplicableTo() != null && (deducteeMasterNonResidentialDTO
						.getWpeApplicableFrom().equals(deducteeMasterNonResidentialDTO.getWpeApplicableTo())
						|| deducteeMasterNonResidentialDTO.getWpeApplicableFrom()
								.after(deducteeMasterNonResidentialDTO.getWpeApplicableTo()))) {
					throw new CustomException(
							"NO PE Document Applicable From Date should not be equals or greater than NO PE Document Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential
							.setNoPEApplicableFrom(deducteeMasterNonResidentialDTO.getWpeApplicableFrom());
					deducteeMasterNonResidential
							.setNoPEApplicableTo(deducteeMasterNonResidentialDTO.getWpeApplicableTo());
				}
			} else {
				throw new CustomException("NO PE Document Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		}
		deducteeMasterNonResidential.setRelatedParty(deducteeMasterNonResidentialDTO.getRelatedParty());
		deducteeMasterNonResidential.setIsGrossingUp(deducteeMasterNonResidentialDTO.getIsGrossingUp());
		deducteeMasterNonResidential
				.setIsDeducteeTransparent(deducteeMasterNonResidentialDTO.getIsDeducteeTransparent());
		deducteeMasterNonResidential
				.setIsBusinessCarriedInIndia(deducteeMasterNonResidentialDTO.getIsBusinessCarriedInIndia());
		deducteeMasterNonResidential
				.setIsPEinvoilvedInPurchaseGoods(deducteeMasterNonResidentialDTO.getIsPEinvoilvedInPurchaseGoods());
		deducteeMasterNonResidential.setIsPEamountReceived(deducteeMasterNonResidentialDTO.getIsPEamountReceived());
		deducteeMasterNonResidential
				.setIsFixedbaseAvailbleIndia(deducteeMasterNonResidentialDTO.getIsFixedbaseAvailbleIndia());
		deducteeMasterNonResidential
				.setIsNoPOEMDeclarationAvailable(deducteeMasterNonResidentialDTO.getIsNoPOEMDeclarationAvailable());
		if (deducteeMasterNonResidentialDTO.getIsFixedbaseAvailbleIndia().equals(true)) {
			if (deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableFrom() != null) {
				if (deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableTo() != null
						&& (deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableFrom()
								.equals(deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableTo())
								|| deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableFrom().after(
										deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableTo()))) {
					throw new CustomException(
							"Fixed Base Available In India Applicable From Date should not be equals or greater than Fixed Base Available In India Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					deducteeMasterNonResidential.setFixedbaseAvailbleIndiaApplicableFrom(
							deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableFrom());
					deducteeMasterNonResidential.setFixedbaseAvailbleIndiaApplicableTo(
							deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableTo());
				}
			} else {
				throw new CustomException("Fixed Base Available In India Applicable From is Mandatory",
						HttpStatus.BAD_REQUEST);
			}
		}
		deducteeMasterNonResidential
				.setIsAmountConnectedFixedBase(deducteeMasterNonResidentialDTO.getIsAmountConnectedFixedBase());
		deducteeMasterNonResidential.setNrRate(deducteeMasterNonResidentialDTO.getNrRate());
		deducteeMasterNonResidential
				.setPrinciplesOfBusinessPlace(deducteeMasterNonResidentialDTO.getPricipleBusinessPlace());
		deducteeMasterNonResidential
				.setStayPeriodFinancialYear(deducteeMasterNonResidentialDTO.getStayPeriodFinancialYear());
		deducteeMasterNonResidential.setIstrcFuture(deducteeMasterNonResidentialDTO.getIstrcFuture());
		if (deducteeMasterNonResidentialDTO.getIstrcFuture().equals(true)) {
			deducteeMasterNonResidential.setTrcFutureDate(deducteeMasterNonResidentialDTO.getTrcFutureDate());
		} else {
			deducteeMasterNonResidential.setTrcFutureDate(null);
		}
		deducteeMasterNonResidential.setIstenfFuture(deducteeMasterNonResidentialDTO.getIstenfFuture());
		if (deducteeMasterNonResidentialDTO.getIstenfFuture().equals(true)) {
			deducteeMasterNonResidential.setTenfFutureDate(deducteeMasterNonResidentialDTO.getTenfFutureDate());
		} else {
			deducteeMasterNonResidential.setTenfFutureDate(null);
		}
		if (isFixedBasedIndiaFile != null && !StringUtils.isEmpty(isFixedBasedIndiaFile.getOriginalFilename())) {
			String fixedFileBasedIndia = blob.uploadExcelToBlob(isFixedBasedIndiaFile);
			deducteeMasterNonResidential.setFixedBasedIndia(fixedFileBasedIndia);
		}

		List<DeductorMaster> deductorMaster = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
		if (!deductorMaster.isEmpty() && deductorMaster != null) {
			deducteeMasterNonResidential.setDeductorCode(deductorMaster.get(0).getCode());
		}
		deducteeMasterNonResidential.setInvoiceTransactionCount(0);
		deducteeMasterNonResidential.setProvisionTransactionCount(0);
		deducteeMasterNonResidential.setAdvanceTransactionCount(0);
		deducteeMasterNonResidential
				.setIsPoemDeclaration(deducteeMasterNonResidentialDTO.getIsPOEMDeclarationInFuture());
		deducteeMasterNonResidential.setPoemFutureDate(deducteeMasterNonResidentialDTO.getPoemFutureDate());
		deducteeMasterNonResidential.setCountryToRemittance(deducteeMasterNonResidentialDTO.getCountryToRemittance());
		deducteeMasterNonResidential.setNatureOfRemittance(deducteeMasterNonResidentialDTO.getNatureOfRemittance());
		deducteeMasterNonResidential
				.setBeneficialOwnerOfIncome(deducteeMasterNonResidentialDTO.getBeneficialOwnerOfIncome());
		deducteeMasterNonResidential
				.setIsBeneficialOwnershipOfDeclaration(deducteeMasterNonResidentialDTO.getIsBeneficialOwnership());
		deducteeMasterNonResidential
				.setMliPptConditionSatisifed(deducteeMasterNonResidentialDTO.getMliOrPptConditionSatisfied());
		deducteeMasterNonResidential
				.setMliSlobConditionSatisifed(deducteeMasterNonResidentialDTO.getMliOrSlobConditionSatisfied());
		deducteeMasterNonResidential.setIsMliPptSlob(deducteeMasterNonResidentialDTO.getIsMliOrPptSlob());
		deducteeMasterNonResidential.setArticleNumberDtaa(deducteeMasterNonResidentialDTO.getArticleNumberDtaa());
		deducteeMasterNonResidential
				.setSectionOfIncometaxAct(deducteeMasterNonResidentialDTO.getSectionOfIncomeTaxAct());
		deducteeMasterNonResidential
				.setAggreementForTransaction(deducteeMasterNonResidentialDTO.getAggreementForTransaction());
		deducteeMasterNonResidentialDAO.save(deducteeMasterNonResidential);
		return deducteeMasterNonResidential;
	}

	/**
	 * 
	 * @param deducteeMasterNonResidentialDTO
	 * @return
	 * @throws ParseException
	 */
	private String getNatureOfPaymentBasedOnHightRateNR(String section,
			DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO) throws ParseException {
		String nop = StringUtils.EMPTY;
		List<Map<String, Object>> nopRateList = mastersClient
				.getListOfNatureAndRate(section, deducteeMasterNonResidentialDTO.getDeducteeStatus(), "NR").getBody()
				.getData();
		Map<Double, String> rateMap = new HashMap<>();
		List<Double> rates = new ArrayList<>();
		Double highestRate = 0.0;
		if (nopRateList != null && !nopRateList.isEmpty()) {
			for (Map<String, Object> nopData : nopRateList) {
				String nature = (String) nopData.get("nature");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String rateApplicableFrom = (String) nopData.get("applicableFrom");
				String rateApplicableTo = (String) nopData.get("applicableTo");
				Date applicableFrom = formatter.parse(rateApplicableFrom);
				Date applicableTo = null;
				if (StringUtils.isNotBlank(rateApplicableTo)) {
					applicableTo = formatter.parse(rateApplicableTo);
				}
				Double rate = (Double) nopData.get("rate");
				if (deducteeMasterNonResidentialDTO.getApplicableFrom().getTime() >= applicableFrom.getTime()
						&& (applicableTo == null || deducteeMasterNonResidentialDTO.getApplicableTo() == null
								|| deducteeMasterNonResidentialDTO.getApplicableTo().getTime() <= applicableTo
										.getTime())) {
					rate = rate != null ? rate : 0.0;
					rateMap.put(rate, nature);
					rates.add(rate);
				}
			}
		}
		if (deducteeMasterNonResidentialDTO.getRate() > 0.0 && !rates.isEmpty()) {
			// section contains mutiple NOP then get the NOP based on closest rate passed in
			// the excel
			Optional<Double> rate = rates.parallelStream()
					.min(Comparator.comparingDouble(i -> Math.abs(i - (deducteeMasterNonResidentialDTO.getRate()))));
			nop = rateMap.get(rate.isPresent() ? rate.get() : 0.0);
		} else {
			nop = rateMap.get(highestRate);
		}
		return nop;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public DeducteeMasterDTO getNonResidential(String deductorPan, Integer id)
			throws JsonMappingException, JsonProcessingException {
		DeducteeMasterDTO deducteeMasterDTO = new DeducteeMasterDTO();
		AddressDTO address = new AddressDTO();
		Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
		AdditionalSectionsDTO additionalSections;
		List<DeducteeMasterNonResidential> response = deducteeMasterNonResidentialDAO.findById(deductorPan, id);
		if (!response.isEmpty()) {
			DeducteeMasterNonResidential deducteeMasterNonResidential = response.get(0);
			BeanUtils.copyProperties(deducteeMasterNonResidential, deducteeMasterDTO);
			deducteeMasterDTO.setId(deducteeMasterNonResidential.getDeducteeMasterId());
			deducteeMasterDTO.setDeducteeTin(deducteeMasterNonResidential.getDeducteeTin());
			deducteeMasterDTO.setPricipleBusinessPlace(deducteeMasterNonResidential.getPrinciplesOfBusinessPlace());
			deducteeMasterDTO.setIsPEdocument(deducteeMasterNonResidential.getNoPEDocumentAvailable());
			deducteeMasterDTO.setWpeApplicableFrom(deducteeMasterNonResidential.getNoPEApplicableFrom());
			deducteeMasterDTO.setWpeApplicableTo(deducteeMasterNonResidential.getNoPEApplicableTo());
			deducteeMasterDTO.setWeatherPEInIndia(deducteeMasterNonResidential.getWhetherPEInIndia());
			deducteeMasterDTO.setWeatherPEInIndiaApplicableFrom(
					deducteeMasterNonResidential.getWhetherPEInIndiaApplicableFrom());
			deducteeMasterDTO
					.setWeatherPEInIndiaApplicableTo(deducteeMasterNonResidential.getWhetherPEInIndiaApplicableTo());
			address.setAreaLocality(deducteeMasterNonResidential.getAreaLocality());
			address.setFlatDoorBlockNo(deducteeMasterNonResidential.getFlatDoorBlockNo());
			address.setNameBuildingVillage(deducteeMasterNonResidential.getNameBuildingVillage());
			address.setPinCode(deducteeMasterNonResidential.getPinCode());
			address.setRoadStreetPostoffice(deducteeMasterNonResidential.getRoadStreetPostoffice());
			if (StringUtils.isNotBlank(deducteeMasterNonResidential.getState())) {
				address.setStateName(deducteeMasterNonResidential.getState().toUpperCase());
			} else {
				address.setStateName(deducteeMasterNonResidential.getState());
			}
			if (StringUtils.isNotBlank(deducteeMasterNonResidential.getCountry())) {
				address.setCountryName(deducteeMasterNonResidential.getCountry().toUpperCase());
			} else {
				address.setCountryName(deducteeMasterNonResidential.getCountry());
			}
			address.setTownCityDistrict(deducteeMasterNonResidential.getTownCityDistrict());
			Map<String, Float> additionalSectionss = null;
			if (deducteeMasterNonResidential.getAdditionalSections() != null) {
				additionalSectionss = objectMapper.readValue(deducteeMasterNonResidential.getAdditionalSections(),
						new TypeReference<Map<String, Float>>() {
						});
			}
			Map<String, String> sectionCodeSection = new HashMap<>();
			if (StringUtils.isNotBlank(deducteeMasterNonResidential.getAdditionalSectionCode())) {
				sectionCodeSection = objectMapper.readValue(deducteeMasterNonResidential.getAdditionalSectionCode(),
						new TypeReference<Map<String, String>>() {
						});
			}
			Map<String, Integer> additionalSectionThresholds = null;
			Map<String, Integer> thresholdMap = new HashMap<>();
			if (deducteeMasterNonResidential.getAdditionalSectionThresholds() != null) {
				additionalSectionThresholds = objectMapper.readValue(
						deducteeMasterNonResidential.getAdditionalSectionThresholds(),
						new TypeReference<Map<String, Integer>>() {
						});
				for (Map.Entry<String, Integer> entry : additionalSectionThresholds.entrySet()) {
					thresholdMap.put(entry.getKey(), entry.getValue());
				}
			}
			Map<String, String> swappedMap = new HashMap<>();
			for (Map.Entry<String, String> entry : sectionCodeSection.entrySet()) {
				swappedMap.put(entry.getValue(), entry.getKey());
			}
			if (deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections() != null
					&& additionalSectionss != null) {
				for (Map.Entry<String, Float> entry : additionalSectionss.entrySet()) {
					additionalSections = new AdditionalSectionsDTO();
					String section = StringUtils.substringBefore(entry.getKey(), "-");
					String nop = StringUtils.substringAfter(entry.getKey(), "-");
					additionalSections.setSection(section);
					additionalSections.setNatureOfPayment(nop);
					additionalSections.setRate(entry.getValue());
					additionalSections.setSectionCode(swappedMap.get(section));
					Boolean threshold = (thresholdMap.get(section) != null && thresholdMap.get(section) == 1) ? true
							: false;
					additionalSections.setIsThresholdApplicable(threshold);
					additionalSectionsSet.add(additionalSections);
				}
			}
			deducteeMasterDTO.setAdditionalSections(additionalSectionsSet);
			deducteeMasterDTO.setIsDeducteeHasAdditionalSections(
					deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections());
			deducteeMasterDTO.setAddress(address);
			String trcUrl = deducteeMasterNonResidential.getTrcFileAddress();
			if (StringUtils.isNotBlank(trcUrl)) {
				String trcFileName = trcUrl.substring(trcUrl.lastIndexOf('/') + 1);
				deducteeMasterDTO.setTrcFileName(trcFileName);
				deducteeMasterDTO.setTrcFileUrl(deducteeMasterNonResidential.getTrcFileAddress());
			}
			String tenFUrl = deducteeMasterNonResidential.getForm10fFileAddress();
			if (StringUtils.isNotBlank(tenFUrl)) {
				String tenFFileName = tenFUrl.substring(tenFUrl.lastIndexOf('/') + 1);
				deducteeMasterDTO.setTenFFileName(tenFFileName);
				deducteeMasterDTO.setTenFFileUrl(deducteeMasterNonResidential.getForm10fFileAddress());

			}
			String noPEFileUrl = deducteeMasterNonResidential.getNoPeDocAddress();
			if (StringUtils.isNotBlank(noPEFileUrl)) {
				String noPEFileName = noPEFileUrl.substring(noPEFileUrl.lastIndexOf('/') + 1);
				deducteeMasterDTO.setNoPEFileName(noPEFileName);
				deducteeMasterDTO.setNoPEFileUrl(deducteeMasterNonResidential.getNoPeDocAddress());
			}
			String wpeFileUrl = deducteeMasterNonResidential.getPeFileAddress();
			if (StringUtils.isNotBlank(wpeFileUrl)) {
				String wpeFileName = wpeFileUrl.substring(wpeFileUrl.lastIndexOf('/') + 1);
				deducteeMasterDTO.setWpeFileName(wpeFileName);
				deducteeMasterDTO.setWpeFileUrl(deducteeMasterNonResidential.getPeFileAddress());
			}
			// fixed Based India File
			String fixedBasedFile = deducteeMasterNonResidential.getFixedBasedIndia();
			if (StringUtils.isNotBlank(fixedBasedFile)) {
				String fixedBasedFileName = fixedBasedFile.substring(fixedBasedFile.lastIndexOf('/') + 1);
				deducteeMasterDTO.setFixedBasedIndiaFileName(fixedBasedFileName);
				deducteeMasterDTO.setFixedBasedIndiaFile(fixedBasedFile);
			}
			deducteeMasterDTO.setAddress(address);
			deducteeMasterDTO.setStayPeriodFinancialYear(deducteeMasterNonResidential.getStayPeriodFinancialYear());
			deducteeMasterDTO.setRelatedParty(deducteeMasterNonResidential.getRelatedParty());
			deducteeMasterDTO.setRate(deducteeMasterNonResidential.getRate());
			deducteeMasterDTO.setIsMliOrPptSlob(deducteeMasterNonResidential.getIsMliPptSlob());
			deducteeMasterDTO.setMliOrPptConditionSatisfied(deducteeMasterNonResidential.getMliPptConditionSatisifed());
			deducteeMasterDTO
					.setMliOrSlobConditionSatisfied(deducteeMasterNonResidential.getMliSlobConditionSatisifed());
			deducteeMasterDTO.setIsPOEMDeclarationInFuture(deducteeMasterNonResidential.getIsPoemDeclaration());
			deducteeMasterDTO
					.setIsBeneficialOwnership(deducteeMasterNonResidential.getIsBeneficialOwnershipOfDeclaration());
			logger.info("REST response to get a Deductee Master record for Non Resident : {}", deducteeMasterDTO);
		}
		return deducteeMasterDTO;
	}

	private Map<String, String> getJsonForadditionalSectionCodeNR(Map<String, String> newSectionAndSectionCode,
			DeducteeMasterNonResidential deductee) throws JsonMappingException, JsonProcessingException {

		Map<String, String> dbSectionSectionCode = new HashMap<>();

		// getting values from different columns and putting into map so that we can add
		// it in
		// additional section column
		if (deductee != null) {
			if (StringUtils.isNotBlank(deductee.getSectionCode())) {
				newSectionAndSectionCode.put(deductee.getSectionCode(), deductee.getSection());
			}
			// getting the existing data in additional_section_code column
			if (StringUtils.isNotBlank(deductee.getAdditionalSectionCode())) {

				Map<String, String> map = objectMapper.readValue(deductee.getAdditionalSectionCode(),
						new TypeReference<Map<String, String>>() {
						});
				for (Entry<String, String> d : map.entrySet()) {
					dbSectionSectionCode.put(d.getKey(), d.getValue());
				}
				dbSectionSectionCode.putAll(map);
			}
			newSectionAndSectionCode.putAll(dbSectionSectionCode);
		}
		return newSectionAndSectionCode;

	}

	public CommonDTO<DeducteeMasterDTO> getListOfNonResidentialDeductees(String deductorPan, Pagination pagination,
			String deducteeName, String deducteeCode) throws JsonMappingException, JsonProcessingException {
		List<DeducteeMasterDTO> deducteeMasterDTOList = new ArrayList<>();
		DeducteeMasterDTO deducteeMasterDTO = null;
		logger.info("deductee name ---- : {}", deducteeName);
		List<DeducteeMasterNonResidential> nonResidentList = null;
		if ("nodeducteename".equalsIgnoreCase(deducteeName) && "nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			nonResidentList = deducteeMasterNonResidentialDAO.findAllByPan(deductorPan, pagination);
		} else if (!"nodeducteename".equalsIgnoreCase(deducteeName)
				&& "nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			nonResidentList = deducteeMasterNonResidentialDAO.findAllByDeducteeNamePan(deductorPan, deducteeName,
					pagination);
		} else if ("nodeducteename".equalsIgnoreCase(deducteeName)
				&& !"nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			nonResidentList = deducteeMasterNonResidentialDAO.findAllByDeducteeCode(deductorPan, deducteeCode,
					pagination);
		} else if (!"nodeducteename".equalsIgnoreCase(deducteeName)
				&& !"nodeducteecode".equalsIgnoreCase(deducteeCode)) {
			nonResidentList = deducteeMasterNonResidentialDAO.findAllByDeducteeNameAndCode(deductorPan, deducteeName,
					deducteeCode, pagination);
		}
		if (!nonResidentList.isEmpty() && nonResidentList != null) {
			for (DeducteeMasterNonResidential deducteeMasterNonResidential : nonResidentList) {
				deducteeMasterDTO = new DeducteeMasterDTO();
				AddressDTO address = new AddressDTO();
				AdditionalSectionsDTO additionalSections = null;
				Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
				BeanUtils.copyProperties(deducteeMasterNonResidential, deducteeMasterDTO);
				deducteeMasterDTO.setId(deducteeMasterNonResidential.getDeducteeMasterId());
				deducteeMasterDTO.setPricipleBusinessPlace(deducteeMasterNonResidential.getPrinciplesOfBusinessPlace());
				address.setAreaLocality(deducteeMasterNonResidential.getAreaLocality());
				address.setCountryName(deducteeMasterNonResidential.getCountry());
				address.setFlatDoorBlockNo(deducteeMasterNonResidential.getFlatDoorBlockNo());
				address.setNameBuildingVillage(deducteeMasterNonResidential.getNameBuildingVillage());
				address.setPinCode(String.valueOf(deducteeMasterNonResidential.getPinCode()));
				address.setRoadStreetPostoffice(deducteeMasterNonResidential.getRoadStreetPostoffice());
				address.setStateName(deducteeMasterNonResidential.getState());
				address.setTownCityDistrict(deducteeMasterNonResidential.getTownCityDistrict());
				Map<String, Float> additionalSectionss = null;
				if (deducteeMasterNonResidential.getAdditionalSections() != null) {
					additionalSectionss = objectMapper.readValue(deducteeMasterNonResidential.getAdditionalSections(),
							new TypeReference<Map<String, Float>>() {
							});
				}
				if (additionalSectionss != null) {
					for (Map.Entry<String, Float> entry : additionalSectionss.entrySet()) {
						additionalSections = new AdditionalSectionsDTO();
						additionalSections.setSection(entry.getKey());
						additionalSections.setRate(entry.getValue());
						additionalSectionsSet.add(additionalSections);
					}
					deducteeMasterDTO.setIsDeducteeHasAdditionalSections(
							deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections());
				}
				deducteeMasterDTO.setWpeApplicableFrom(deducteeMasterNonResidential.getNoPEApplicableFrom());
				deducteeMasterDTO.setWpeApplicableTo(deducteeMasterNonResidential.getNoPEApplicableTo());
				deducteeMasterDTO.setWeatherPEInIndia(deducteeMasterNonResidential.getWhetherPEInIndia());
				deducteeMasterDTO.setWeatherPEInIndiaApplicableFrom(
						deducteeMasterNonResidential.getWhetherPEInIndiaApplicableFrom());
				deducteeMasterDTO.setWeatherPEInIndiaApplicableTo(
						deducteeMasterNonResidential.getWhetherPEInIndiaApplicableTo());
				deducteeMasterDTO.setAdditionalSections(additionalSectionsSet);
				deducteeMasterDTO.setPanStatus(deducteeMasterNonResidential.getPanStatus());
				deducteeMasterDTO.setPanVerifiedDate(deducteeMasterNonResidential.getPanVerifiedDate());
				deducteeMasterDTO.setAddress(address);
				deducteeMasterDTO.setTrcFileName(deducteeMasterNonResidential.getTrcFileAddress());
				deducteeMasterDTO.setTenFFileName(deducteeMasterNonResidential.getForm10fFileAddress());
				deducteeMasterDTO.setNoPEFileName(deducteeMasterNonResidential.getNoPeDocAddress());
				deducteeMasterDTO.setWpeFileName(deducteeMasterNonResidential.getPeFileAddress());
				// Total deductee sections count
				int sectionsCount = StringUtils.isBlank(deducteeMasterNonResidential.getSection()) ? 0 : 1;
				deducteeMasterDTO.setTotalNoOfSections(sectionsCount + additionalSectionsSet.size());
				deducteeMasterDTOList.add(deducteeMasterDTO);
			}
		}
		BigInteger deducteeCount = deducteeMasterNonResidentialDAO.getAllDeducteeNonResidentialCount(deductorPan);

		PagedData<DeducteeMasterDTO> pagedData = new PagedData<>(deducteeMasterDTOList, nonResidentList.size(),
				pagination.getPageNumber(),
				deducteeCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<DeducteeMasterDTO> deducteeMasterData = new CommonDTO<>();
		deducteeMasterData.setResultsSet(pagedData);
		deducteeMasterData.setCount(deducteeCount);
		return deducteeMasterData;

	}

	// update NR
	public DeducteeMasterNonResidential updateNonResident(
			DeducteeMasterNonResidentialDTO deducteeMasterNonResidentialDTO, String deductorPan, MultipartFile trcFile,
			MultipartFile tenFFile, MultipartFile wpeFile, MultipartFile noPEFile)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("REST request to update NON Resident Deductee Master REcord : {}", deducteeMasterNonResidentialDTO);

		List<DeducteeMasterNonResidential> deducteeMasterNonResidentialOptional = deducteeMasterNonResidentialDAO
				.findById(deductorPan, deducteeMasterNonResidentialDTO.getId());
		DeducteeMasterNonResidential deducteeMasterNonResidential = null;
		if (!deducteeMasterNonResidentialOptional.isEmpty()) {
			deducteeMasterNonResidential = deducteeMasterNonResidentialOptional.get(0);
			BeanUtils.copyProperties(deducteeMasterNonResidentialDTO, deducteeMasterNonResidential);
			deducteeMasterNonResidential.setDeducteeName(deducteeMasterNonResidentialDTO.getDeducteeName());
			deducteeMasterNonResidential
					.setDeducteeCode(StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteeCode())
							? deducteeMasterNonResidentialDTO.getDeducteeCode().trim()
							: "");

			deducteeMasterNonResidential
					.setDeducteePAN(StringUtils.isNotBlank(deducteeMasterNonResidentialDTO.getDeducteePAN())
							? deducteeMasterNonResidentialDTO.getDeducteePAN().trim()
							: "");
			deducteeMasterNonResidential.setDeducteeTin(deducteeMasterNonResidentialDTO.getDeducteeTin());
			deducteeMasterNonResidential.setDeducteeStatus(deducteeMasterNonResidentialDTO.getDeducteeStatus());
			deducteeMasterNonResidential
					.setDeducteeResidentialStatus(deducteeMasterNonResidentialDTO.getDeducteeResidentialStatus());
			deducteeMasterNonResidential
					.setDeducteeAadharNumber(deducteeMasterNonResidentialDTO.getDeducteeAadharNumber());
			deducteeMasterNonResidential.setEmailAddress(deducteeMasterNonResidentialDTO.getEmailAddress());
			deducteeMasterNonResidential.setPhoneNumber(deducteeMasterNonResidentialDTO.getPhoneNumber() != null
					? deducteeMasterNonResidentialDTO.getPhoneNumber()
					: "");
			deducteeMasterNonResidential
					.setFlatDoorBlockNo(deducteeMasterNonResidentialDTO.getAddress().getFlatDoorBlockNo());
			deducteeMasterNonResidential
					.setNameBuildingVillage(deducteeMasterNonResidentialDTO.getAddress().getNameBuildingVillage());
			deducteeMasterNonResidential
					.setRoadStreetPostoffice(deducteeMasterNonResidentialDTO.getAddress().getRoadStreetPostoffice());
			deducteeMasterNonResidential
					.setAreaLocality(deducteeMasterNonResidentialDTO.getAddress().getAreaLocality());
			// stateid
			deducteeMasterNonResidential.setState(deducteeMasterNonResidentialDTO.getAddress().getStateName());
			deducteeMasterNonResidential.setCountry(deducteeMasterNonResidentialDTO.getAddress().getCountryName());
			deducteeMasterNonResidential
					.setTownCityDistrict(deducteeMasterNonResidentialDTO.getAddress().getTownCityDistrict());
			deducteeMasterNonResidential.setPinCode(deducteeMasterNonResidentialDTO.getAddress().getPinCode());

			// sectionID
			deducteeMasterNonResidential.setSection(deducteeMasterNonResidentialDTO.getSection());
			deducteeMasterNonResidential.setRate(BigDecimal.valueOf(deducteeMasterNonResidentialDTO.getRate())
					.setScale(4, BigDecimal.ROUND_HALF_DOWN));
			deducteeMasterNonResidential
					.setDefaultRate(BigDecimal.valueOf(deducteeMasterNonResidentialDTO.getDefaultRate()));
			deducteeMasterNonResidential.setIsDeducteeHasAdditionalSections(
					deducteeMasterNonResidentialDTO.getIsDeducteeHasAdditionalSections());

			Map<String, Float> additionalSection = new HashMap<>();
			if (deducteeMasterNonResidentialDTO.getIsDeducteeHasAdditionalSections() != null) {
				for (AdditionalSectionsDTO additionalSections : deducteeMasterNonResidentialDTO
						.getAdditionalSections()) {
					if (StringUtils.isNotBlank(additionalSections.getSection()) && additionalSections.getRate() != 0) {
						String sectionAndNop = additionalSections.getSection() + "-"
								+ additionalSections.getNatureOfPayment();
						additionalSection.put(sectionAndNop, additionalSections.getRate());
					}
				}
			}
			String additionalSections = objectMapper.writeValueAsString(additionalSection);
			deducteeMasterNonResidential.setAdditionalSections(additionalSections);
			deducteeMasterNonResidential.setApplicableFrom(deducteeMasterNonResidentialDTO.getApplicableFrom());
			deducteeMasterNonResidential.setApplicableTo(deducteeMasterNonResidentialDTO.getApplicableTo());
			deducteeMasterNonResidential.setCountryOfResidence(deducteeMasterNonResidentialDTO.getCountryOfResidence());
			deducteeMasterNonResidential.setIsTRCAvailable(deducteeMasterNonResidentialDTO.getIsTRCAvailable());
			deducteeMasterNonResidential.setIsTenFAvailable(deducteeMasterNonResidentialDTO.getIsTenFAvailable());
			deducteeMasterNonResidential.setWhetherPEInIndia(deducteeMasterNonResidentialDTO.getWeatherPEInIndia());
			if (deducteeMasterNonResidentialDTO.getIsPEdocument() != null) {
				deducteeMasterNonResidential
						.setNoPEApplicableFrom(deducteeMasterNonResidentialDTO.getWpeApplicableFrom());
				deducteeMasterNonResidential.setNoPEApplicableTo(deducteeMasterNonResidentialDTO.getWpeApplicableTo());
			}
			deducteeMasterNonResidential.setIsPOEMavailable(deducteeMasterNonResidentialDTO.getIsPOEMavailable());
			if (deducteeMasterNonResidentialDTO.getIsPOEMavailable() != null) {
				deducteeMasterNonResidential
						.setPoemApplicableFrom(deducteeMasterNonResidentialDTO.getPoemApplicableFrom());
				deducteeMasterNonResidential.setPoemApplicableTo(deducteeMasterNonResidentialDTO.getPoemApplicableTo());
			}
			deducteeMasterNonResidential.setNoPEDocumentAvailable(deducteeMasterNonResidentialDTO.getIsPEdocument());
			deducteeMasterNonResidential.setRelatedParty(deducteeMasterNonResidentialDTO.getRelatedParty());
			deducteeMasterNonResidential.setIsGrossingUp(deducteeMasterNonResidentialDTO.getIsGrossingUp());
			deducteeMasterNonResidential
					.setIsDeducteeTransparent(deducteeMasterNonResidentialDTO.getIsDeducteeTransparent());
			deducteeMasterNonResidential.setIstenfFuture(deducteeMasterNonResidentialDTO.getIstenfFuture());
			deducteeMasterNonResidential
					.setIsBusinessCarriedInIndia(deducteeMasterNonResidentialDTO.getIsBusinessCarriedInIndia());
			deducteeMasterNonResidential
					.setIsPEinvoilvedInPurchaseGoods(deducteeMasterNonResidentialDTO.getIsPEinvoilvedInPurchaseGoods());
			deducteeMasterNonResidential.setIsPEamountReceived(deducteeMasterNonResidentialDTO.getIsPEamountReceived());
			deducteeMasterNonResidential.setIsPEdocument(deducteeMasterNonResidentialDTO.getIsPEdocument());
			deducteeMasterNonResidential.setWhetherPEInIndiaApplicableFrom(
					deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableFrom());
			deducteeMasterNonResidential
					.setWhetherPEInIndiaApplicableTo(deducteeMasterNonResidentialDTO.getWeatherPEInIndiaApplicableTo());
			deducteeMasterNonResidential
					.setIsFixedbaseAvailbleIndia(deducteeMasterNonResidentialDTO.getIsFixedbaseAvailbleIndia());
			deducteeMasterNonResidential.setFixedbaseAvailbleIndiaApplicableFrom(
					deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableFrom());
			deducteeMasterNonResidential.setFixedbaseAvailbleIndiaApplicableTo(
					deducteeMasterNonResidentialDTO.getFixedbaseAvailbleIndiaApplicableTo());
			deducteeMasterNonResidential
					.setIsAmountConnectedFixedBase(deducteeMasterNonResidentialDTO.getIsAmountConnectedFixedBase());
			deducteeMasterNonResidential.setNrRate(deducteeMasterNonResidentialDTO.getNrRate());
			deducteeMasterNonResidential
					.setPrinciplesOfBusinessPlace(deducteeMasterNonResidentialDTO.getPricipleBusinessPlace());
			deducteeMasterNonResidential
					.setStayPeriodFinancialYear(deducteeMasterNonResidentialDTO.getStayPeriodFinancialYear());
			if (deducteeMasterNonResidentialDTO.getIsTRCAvailable() != null && trcFile != null) {
				String trcUri = blob.uploadExcelToBlob(trcFile);
				deducteeMasterNonResidential.setTrcFileAddress(trcUri);
			}
			if (deducteeMasterNonResidentialDTO.getIsTenFAvailable() != null && tenFFile != null) {
				String tenfUri = blob.uploadExcelToBlob(tenFFile);
				deducteeMasterNonResidential.setForm10fFileAddress(tenfUri);
			}
			if (deducteeMasterNonResidentialDTO.getNoPEDocumentAvaliable() != null && noPEFile != null) {
				String noPEUri = blob.uploadExcelToBlob(noPEFile);
				deducteeMasterNonResidential.setNoPeDocAddress(noPEUri);
			}
			if (deducteeMasterNonResidentialDTO.getWeatherPEInIndia() != null && wpeFile != null) {
				String wpeUri = blob.uploadExcelToBlob(wpeFile);
				deducteeMasterNonResidential.setPeFileAddress(wpeUri);
			}
		}
		if (deducteeMasterNonResidentialDTO.getIstrcFuture() != null
				&& deducteeMasterNonResidentialDTO.getIstrcFuture().equals(true)) {
			deducteeMasterNonResidential.setTrcFutureDate(deducteeMasterNonResidentialDTO.getTrcFutureDate());
		}
		if (deducteeMasterNonResidentialDTO.getIstenfFuture() != null
				&& deducteeMasterNonResidentialDTO.getIstenfFuture().equals(true)) {
			deducteeMasterNonResidential.setTenfFutureDate(deducteeMasterNonResidentialDTO.getTenfFutureDate());
		}
		String deducteeName = deducteeMasterNonResidentialDTO.getDeducteeName().toLowerCase();
		deducteeName = deducteeName.replaceAll("[^a-z0-9 ]", "");
		deducteeMasterNonResidential.setModifiedName(deducteeName);
		deducteeMasterNonResidentialDAO.updateNonResidential(deducteeMasterNonResidential);

		return deducteeMasterNonResidential;

	}

	/*
	 * This method returns the list of deductee names along with ids
	 *
	 */

	public List<CustomDeducteesNonResidentDTO> getListOfOnlyNonResidentDeductees(String deductorPan) {
		return deducteeMasterNonResidentialDAO.findAllByDeducteeNamePan(deductorPan);
	}

	/**
	 * This method for get deductee Names and Pans based on deductor pan.
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<CustomDeducteesDTO> getListOfOnlyResidentDeductees(String deductorPan) {
		return deducteeMasterResidentialDAO.findAllByDeducteeNamePan(deductorPan);

	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload deducteeBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(UploadTypes.DEDUCTEE_EXCEL.name());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	private boolean isAlreadyProcessed(String sha256Sum) {

		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);

		return sha256Record != null && !sha256Record.isEmpty();
	}

	@Transactional
	public BatchUpload saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);

		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = deducteeBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, null, tenantId);
			return batchUpload;
		}
		BatchUpload batchUpload = new BatchUpload();
		if (FilenameUtils.getExtension(multiPartFile.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {

				XSSFSheet worksheet = workbook.getSheetAt(0);
				XSSFRow headerRow = worksheet.getRow(0);

				int headersCount = Excel.getNonEmptyCellsCount(headerRow);
				logger.info("Column header count :{}", headersCount);
				ArrayList<String> fileHeaders = new ArrayList<>();
				for (org.apache.poi.ss.usermodel.Cell cell : headerRow) {
					fileHeaders.add(cell.getStringCellValue());
				}
				// Headers validation
				ArrayList<String> differenceHeaders = new ArrayList<>();
				ArrayList<String> resHeaders = (ArrayList<String>) errorReportService.getResDeducteeHeaderFields();
				ArrayList<String> nrHeaders = (ArrayList<String>) errorReportService.getNrDeducteeHeaderFields();
				if (headersCount == nrHeaders.size()) {
					fileHeaders.stream().forEach(header -> {
						if (!nrHeaders.contains(header)) {
							differenceHeaders.add(header);
						}
					});
				} else if (headersCount == resHeaders.size()) {
					fileHeaders.stream().forEach(header -> {
						if (!resHeaders.contains(header)) {
							differenceHeaders.add(header);
						}
					});
				}
				logger.info("difference headers  :{}", differenceHeaders);
				if ((headersCount != resHeaders.size() && headersCount != nrHeaders.size())
						|| !differenceHeaders.isEmpty()) {
					batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
					batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
					batchUpload.setSuccessCount(0L);
					batchUpload.setFailedCount(0L);
					batchUpload.setRowsCount(0L);
					batchUpload.setProcessedCount(0);
					batchUpload.setMismatchCount(0L);
					batchUpload.setSha256sum(sha256);
					batchUpload.setStatus("Failed");
					batchUpload.setCreatedBy(userName);
					batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
					return deducteeBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
							assessmentMonthPlusOne, userName, null, tenantId);
				} else {
					batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
					batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
					batchUpload.setStatus("Processing");
					batchUpload.setSuccessCount(0L);
					batchUpload.setFailedCount(0L);
					batchUpload.setRowsCount(0L);
					batchUpload.setProcessedCount(0);
					batchUpload.setMismatchCount(0L);
					batchUpload.setSha256sum(sha256);
					batchUpload.setMismatchCount(0L);
					batchUpload = deducteeBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
							assessmentMonthPlusOne, userName, null, tenantId);
				}
				// Converting excel file to csv
				File file = new File(multiPartFile.getOriginalFilename());
				OutputStream os = new FileOutputStream(file);
				os.write(multiPartFile.getBytes());
				Workbook csvWorkBook = new Workbook(file.getAbsolutePath());
				ByteArrayOutputStream baout = new ByteArrayOutputStream();
				csvWorkBook.save(baout, SaveFormat.CSV);
				File csvFile = new File(FilenameUtils.removeExtension(file.getName()) + ".csv");

				FileUtils.writeByteArrayToFile(csvFile, baout.toByteArray());
				String csvPath = blob.uploadExcelToBlobWithFile(csvFile, tenantId);
				if (headersCount > 58) {
					batchUpload = deducteeBulkService.processNonResidentDeductees(csvPath, deductorTan, assesssmentYear,
							assessmentMonthPlusOne, userName, tenantId, deductorPan, batchUpload);
				} else {
					batchUpload = deducteeBulkService.processResidentDeductees(csvPath, deductorTan, assesssmentYear,
							assessmentMonthPlusOne, userName, tenantId, deductorPan, batchUpload);
				}
				os.close();
			} catch (Exception e) {
				throw new RuntimeException("Failed to process deductee data ", e);
			}
		} else if (FilenameUtils.getExtension(multiPartFile.getOriginalFilename()).equalsIgnoreCase("csv")) {
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setStatus("Processing");
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			batchUpload = deducteeBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, null, tenantId);
			batchUpload = deducteeBulkService.processResidentDeductees(batchUpload.getFilePath(), deductorTan,
					assesssmentYear, assessmentMonthPlusOne, userName, tenantId, deductorPan, batchUpload);
		}
		return batchUpload;

	}

	public Map<String, Object> getDeducteesByPanAndName(String deductorTan, String deducteePan, String deducteeName,
			Integer invoiceLineItemId, int year, String deductorPan, String section)
			throws JsonMappingException, JsonProcessingException {
		List<DeducteeMasterDTO> deducteeMasterDTOList = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		List<AoMaster> listOfAo = null;
		boolean isAoMasterExists = false;
		List<DeducteeMasterNonResidential> deducteeMasterNonResidentialList = null;
		if (!"null".equalsIgnoreCase(deducteePan.trim()) && StringUtils.isNotBlank(deducteePan.trim())) {
			deducteeMasterNonResidentialList = deducteeMasterNonResidentialDAO.getDeducteeByPan(deductorPan.trim(),
					deducteePan.trim());
			listOfAo = aoMasterDAO.findAllByDeductorTanDeducteePan(deductorTan.trim(), deducteePan.trim(), section);
		} else if (StringUtils.isNotBlank(deducteeName)) {
			deducteeMasterNonResidentialList = deducteeMasterNonResidentialDAO
					.getDeducteeByName(deductorPan.trim(), deducteeName.trim()).getData();
			listOfAo = aoMasterDAO.findAllByDeductorTanDeducteeName(deductorTan.trim(), deducteeName.trim(), section);

		}
		if (deducteeMasterNonResidentialList != null && !deducteeMasterNonResidentialList.isEmpty()) {
			AoMaster aoMaster = null;
			if (listOfAo != null && !listOfAo.isEmpty()) {
				for (AoMaster aoMasterData : listOfAo) {
					if (aoMasterData.getApplicableFrom() != null
							&& (aoMasterData.getApplicableFrom().before(new Date())
									|| aoMasterData.getApplicableFrom().equals(new Date()))
							&& (aoMasterData.getApplicableTo() == null
									|| aoMasterData.getApplicableTo().after(new Date())
									|| aoMasterData.getApplicableTo().equals(new Date()))) {
						aoMaster = aoMasterData;
						break;
					}
				}
			}
			if (aoMaster != null) {
				isAoMasterExists = true;
			} else {
				aoMaster = new AoMaster();
			}
			map.put("aoMaster", aoMaster);
			map.put("isAoMasterExists", isAoMasterExists);

			DeducteeMasterDTO deducteeMasterDTO = null;
			AddressDTO address = null;

			for (DeducteeMasterNonResidential deducteeMasterNonResidential : deducteeMasterNonResidentialList) {
				address = new AddressDTO();
				deducteeMasterDTO = new DeducteeMasterDTO();
				BeanUtils.copyProperties(deducteeMasterNonResidential, deducteeMasterDTO);
				Map<String, Float> additionalSection = objectMapper.readValue(
						deducteeMasterNonResidential.getAdditionalSections(), new TypeReference<Map<String, Float>>() {
						});
				AdditionalSectionsDTO additionalSections = null;
				Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
				if (deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections() != null
						&& deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections()) {
					for (Map.Entry<String, Float> entry : additionalSection.entrySet()) {
						additionalSections = new AdditionalSectionsDTO();
						additionalSections.setSection(entry.getKey());
						additionalSections.setRate(entry.getValue());
						additionalSectionsSet.add(additionalSections);
					}
					deducteeMasterDTO.setAdditionalSections(additionalSectionsSet);
				}
				address.setAreaLocality(deducteeMasterNonResidential.getAreaLocality());
				address.setCountryName(deducteeMasterNonResidential.getCountry());
				address.setFlatDoorBlockNo(deducteeMasterNonResidential.getFlatDoorBlockNo());
				address.setNameBuildingVillage(deducteeMasterNonResidential.getNameBuildingVillage());
				address.setPinCode(String.valueOf(deducteeMasterNonResidential.getPinCode()));
				address.setRoadStreetPostoffice(deducteeMasterNonResidential.getRoadStreetPostoffice());
				address.setStateName(deducteeMasterNonResidential.getState());
				address.setTownCityDistrict(deducteeMasterNonResidential.getTownCityDistrict());
				deducteeMasterDTO.setAddress(address);
				deducteeMasterDTO.setId(deducteeMasterNonResidential.getDeducteeMasterId());
				deducteeMasterDTO.setApplicableFrom(deducteeMasterNonResidential.getApplicableFrom());
				deducteeMasterDTO.setApplicableTo(deducteeMasterNonResidential.getApplicableTo());
				deducteeMasterDTO.setCountryOfResidence(deducteeMasterNonResidential.getCountryOfResidence());
				deducteeMasterDTO.setDeducteeCode(deducteeMasterNonResidential.getDeducteeCode());
				deducteeMasterDTO.setDeducteeName(deducteeMasterNonResidential.getDeducteeName());
				deducteeMasterDTO.setDeducteePAN(deducteeMasterNonResidential.getDeducteePAN());
				deducteeMasterDTO
						.setDeducteeResidentialStatus(deducteeMasterNonResidential.getDeducteeResidentialStatus());
				deducteeMasterDTO.setDeducteeStatus(deducteeMasterNonResidential.getDeducteeStatus());
				deducteeMasterDTO.setDeducteeTin(deducteeMasterNonResidential.getDeducteeTin());
				deducteeMasterDTO.setDefaultRate(deducteeMasterNonResidential.getDefaultRate());
				deducteeMasterDTO.setEmailAddress(deducteeMasterNonResidential.getEmailAddress());
				deducteeMasterDTO.setIsDeducteeHasAdditionalSections(
						deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections());
				deducteeMasterDTO.setIsPOEMavailable(deducteeMasterNonResidential.getIsPOEMavailable());
				deducteeMasterDTO.setIsTenFAvailable(deducteeMasterNonResidential.getIsTenFAvailable());
				deducteeMasterDTO.setIsTRCAvailable(deducteeMasterNonResidential.getIsTRCAvailable());
				deducteeMasterDTO.setNoPEDocumentAvaliable(deducteeMasterNonResidential.getNoPEDocumentAvailable());
				deducteeMasterDTO.setPanStatus(deducteeMasterNonResidential.getPanStatus());
				deducteeMasterDTO.setPanVerifiedDate(deducteeMasterNonResidential.getPanVerifiedDate());
				deducteeMasterDTO.setPhoneNumber(String.valueOf(deducteeMasterNonResidential.getPhoneNumber()));
				deducteeMasterDTO.setPoemApplicableFrom(deducteeMasterNonResidential.getPoemApplicableFrom());
				deducteeMasterDTO.setPoemApplicableTo(deducteeMasterNonResidential.getPoemApplicableTo());
				deducteeMasterDTO.setRate(deducteeMasterNonResidential.getRate());
				deducteeMasterDTO.setSection(deducteeMasterNonResidential.getSection());
				deducteeMasterDTO.setTenFApplicableFrom(deducteeMasterNonResidential.getTenFApplicableFrom());
				deducteeMasterDTO.setTenFApplicableTo(deducteeMasterNonResidential.getTenFApplicableTo());
				deducteeMasterDTO.setTrcApplicableFrom(deducteeMasterNonResidential.getTrcApplicableFrom());
				deducteeMasterDTO.setTrcApplicableTo(deducteeMasterNonResidential.getTrcApplicableTo());
				deducteeMasterDTO.setWeatherPEInIndia(deducteeMasterNonResidential.getWhetherPEInIndia());
				deducteeMasterDTO.setWeatherPEInIndiaApplicableFrom(
						deducteeMasterNonResidential.getWhetherPEInIndiaApplicableFrom());
				deducteeMasterDTO.setWeatherPEInIndiaApplicableTo(
						deducteeMasterNonResidential.getWhetherPEInIndiaApplicableTo());
				deducteeMasterDTO.setIsPEdocument(deducteeMasterNonResidential.getNoPEDocumentAvailable());
				deducteeMasterDTO.setWpeApplicableFrom(deducteeMasterNonResidential.getNoPEApplicableFrom());
				deducteeMasterDTO.setWpeApplicableTo(deducteeMasterNonResidential.getNoPEApplicableTo());
				String trcUrl = deducteeMasterNonResidential.getTrcFileAddress();
				if (StringUtils.isNotBlank(trcUrl)) {
					String trcFileName = trcUrl.substring(trcUrl.lastIndexOf('/') + 1, trcUrl.length());
					deducteeMasterDTO.setTrcFileName(trcFileName);
					deducteeMasterDTO.setTrcFileUrl(deducteeMasterNonResidential.getTrcFileAddress());
				}
				String tenFUrl = deducteeMasterNonResidential.getForm10fFileAddress();
				if (StringUtils.isNotBlank(tenFUrl)) {
					String tenFFileName = tenFUrl.substring(tenFUrl.lastIndexOf('/') + 1, tenFUrl.length());
					deducteeMasterDTO.setTenFFileName(tenFFileName);
					deducteeMasterDTO.setTenFFileUrl(deducteeMasterNonResidential.getForm10fFileAddress());

				}
				String noPEFileUrl = deducteeMasterNonResidential.getNoPeDocAddress();
				if (StringUtils.isNotBlank(noPEFileUrl)) {
					String noPEFileName = noPEFileUrl.substring(noPEFileUrl.lastIndexOf('/') + 1, noPEFileUrl.length());
					deducteeMasterDTO.setNoPEFileName(noPEFileName);
					deducteeMasterDTO.setNoPEFileUrl(deducteeMasterNonResidential.getNoPeDocAddress());
				}
				String wpeFileUrl = deducteeMasterNonResidential.getPeFileAddress();
				if (StringUtils.isNotBlank(wpeFileUrl)) {
					String wpeFileName = wpeFileUrl.substring(wpeFileUrl.lastIndexOf('/') + 1, wpeFileUrl.length());
					deducteeMasterDTO.setWpeFileName(wpeFileName);
					deducteeMasterDTO.setWpeFileUrl(deducteeMasterNonResidential.getPeFileAddress());
				}
				deducteeMasterDTOList.add(deducteeMasterDTO);
			}
		}
		map.put("deducteeMasterData", deducteeMasterDTOList);
		return map;
	}

	public DeducteeMasterNonResidentialDTO getDeducteeBasedOnPanAndName(String deductorPan, String deducteePan,
			String deducteeName) {
		DeducteeMasterNonResidential deducteeMasterNonResidential = null;
		DeducteeMasterNonResidentialDTO deducteeMasterDTO = new DeducteeMasterNonResidentialDTO();
		String deducteeModifiedName = deducteeName.toLowerCase().replaceAll("[^a-z0-9 ]", "");
		List<DeducteeMasterNonResidential> nrDeducteeList = deducteeMasterNonResidentialDAO
				.findAllByModifiedDeducteeNamePan(deductorPan, deducteeModifiedName, deducteePan);
		if (nrDeducteeList != null && !nrDeducteeList.isEmpty()) {
			for (DeducteeMasterNonResidential deductee : nrDeducteeList) {
				if (deductee.getApplicableTo() == null || deductee.getApplicableTo().after(new Date())) {
					deducteeMasterNonResidential = deductee;
					break;
				}
			}
		}
		if (deducteeMasterNonResidential != null) {
			BeanUtils.copyProperties(deducteeMasterNonResidential, deducteeMasterDTO);
		}
		return deducteeMasterDTO;
	}

	public DeducteeMasterResidential getDeducteeMasterResidential(String deductorPan, Integer year, String tenantId) {

		int month = Calendar.getInstance().get(Calendar.MONTH);

		Calendar calendarStart = Calendar.getInstance();
		calendarStart.set(Calendar.YEAR, year);
		calendarStart.set(Calendar.MONTH, month);
		calendarStart.set(Calendar.DAY_OF_MONTH, 1);
		Date startDate = calendarStart.getTime();

		Calendar calendarEnd = Calendar.getInstance();
		calendarEnd.set(Calendar.YEAR, year);
		calendarEnd.set(Calendar.MONTH, month);
		calendarEnd.set(Calendar.DAY_OF_MONTH, 31);
		Date endDate = calendarEnd.getTime();

		List<DeducteeMasterResidential> residentialObj = deducteeMasterResidentialDAO
				.getResidentalBasedOnTanAndYear(deductorPan, startDate, endDate);
		if (!residentialObj.isEmpty()) {
			return residentialObj.get(0);
		}
		return new DeducteeMasterResidential();
	}

	public String getDeducteePanStatus(String deductorPan, Integer year, Integer month) {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(year, month - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(year, month - 1));
		return getDeducteePanStatus(deductorPan, startDate, endDate);
	}

	public String getDeducteePanStatus(String deductorPan, String startDate, String endDate) {
		long countValidPan = deducteeMasterResidentialDAO.countDeducteeResidentialPanStatusValid(deductorPan, startDate,
				endDate);
		logger.info("Total deductee residential valid pan status: {}", countValidPan);

		long countInValidPan = deducteeMasterResidentialDAO.countDeducteeResidentialPanStatusInValid(deductorPan,
				startDate, endDate);
		logger.info("Total deductee residential invalid pan status: {}", countInValidPan);

		long countEmptyPan = deducteeMasterResidentialDAO.countDeducteeResidentialPanStatusEmpty(deductorPan, startDate,
				endDate);
		logger.info("Total deductee residential Empty pan status: {}", countEmptyPan);

		if (countValidPan == 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else if (countInValidPan > 0 || countEmptyPan > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (countValidPan > 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		} else {
			return StringUtils.EMPTY;
		}
	}

	public List<DeducteeMasterResidential> listDeducteeMasterResidential(String deductorPan, Integer year,
			String tenantId) {

		int month = Calendar.getInstance().get(Calendar.MONTH);

		List<DeducteeMasterResidential> listOfResident = new ArrayList<>();
		List<DeducteeMasterResidential> listOfResidentObj = deducteeMasterResidentialDAO.getDeductees(deductorPan,
				CommonUtil.getMonthStartDate(year, month), CommonUtil.getMonthEndDate(year, month), Pagination.UNPAGED)
				.getData();
		if (!listOfResidentObj.isEmpty()) {
			listOfResidentObj.forEach(resident -> {
				listOfResident.add(resident);
			});
			return listOfResident;
		}
		return listOfResident;
	}

	public List<String> getDeductees(String deductorTan, String deducteeType, String type, int year, int month,
			boolean isMismatch) {
		if ("resident".equalsIgnoreCase(deducteeType)) {
			deducteeType = "N";
		} else {
			deducteeType = "Y";
		}
		if ("invoice".equalsIgnoreCase(type)) {
			if (month == 0) {
				return deducteeMasterResidentialDAO.getInvoiceDeducteesBasedOnYEar(deductorTan, deducteeType, year,
						isMismatch);
			} else {
				return deducteeMasterResidentialDAO.getInvoiceDeductees(deductorTan, deducteeType, year, month,
						isMismatch);
			}
		} else if ("advance".equalsIgnoreCase(type)) {
			return deducteeMasterResidentialDAO.getAdvanceDeductees(deductorTan, deducteeType, year, month, isMismatch);
		} else {
			return deducteeMasterResidentialDAO.getProvisionDeductees(deductorTan, deducteeType, year, month,
					isMismatch);
		}
	}

	public CustomDeducteesDTO convertResidentDeducteeModelToDTO(DeducteeMasterResidential deducteeMasterResidential) {
		CustomDeducteesDTO residentDeductee = new CustomDeducteesDTO();
		try {
			residentDeductee.setId(deducteeMasterResidential.getDeducteeMasterId());
			residentDeductee.setDeducteeName(deducteeMasterResidential.getDeducteeName());
			residentDeductee.setDeducteePan(deducteeMasterResidential.getDeducteePAN());
		} catch (Exception e) {
			logger.error("Exception occurred while convertResidentDeducteeModelToDTO : ", e);
		}
		return residentDeductee;
	}

	public CustomDeducteesDTO convertNonResidentDeducteeModelToDTO(
			DeducteeMasterNonResidential deducteeMasterNonResidential) {
		CustomDeducteesDTO residentDeductee = new CustomDeducteesDTO();
		try {
			residentDeductee.setId(deducteeMasterNonResidential.getDeducteeMasterId());
			residentDeductee.setDeducteeName(deducteeMasterNonResidential.getDeducteeName());
			residentDeductee.setDeducteePan(deducteeMasterNonResidential.getDeducteePAN());
		} catch (Exception e) {
			logger.error("Exception occurred while convertNonResidentDeducteeModelToDTO : ", e);
		}
		return residentDeductee;
	}

	public Set<String> getDeducteeNames(String deductorPan, String deducteeType) {
		Set<String> deducteeNames = new HashSet<>();
		if ("resident".equalsIgnoreCase(deducteeType)) {
			List<DeducteeMasterResidential> listOfDeductees = deducteeMasterResidentialDAO
					.getDeducteesByPan(deductorPan);
			if (!listOfDeductees.isEmpty() && listOfDeductees != null) {
				for (DeducteeMasterResidential deductee : listOfDeductees) {
					deducteeNames.add(deductee.getDeducteeName());
				}
			}
		} else {
			List<DeducteeMasterNonResidential> listOfDeductees = deducteeMasterNonResidentialDAO
					.getDeducteesByPan(deductorPan);
			if (!listOfDeductees.isEmpty() && listOfDeductees != null) {
				for (DeducteeMasterNonResidential deductee : listOfDeductees) {
					deducteeNames.add(deductee.getDeducteeName());
				}
			}
		}
		return deducteeNames;
	}

	public static Date subtractDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	public List<DeducteeMasterDTO> getListOfResidentialDeductees(String deductorPan) {
		List<DeducteeMasterResidential> listOfDeductees = deducteeMasterResidentialDAO.getDeducteesByPan(deductorPan);
		List<DeducteeMasterDTO> listDeductee = new ArrayList<>();
		if (!listOfDeductees.isEmpty()) {
			for (DeducteeMasterResidential deductee : listOfDeductees) {
				DeducteeMasterDTO deducteeMaster = new DeducteeMasterDTO();
				BeanUtils.copyProperties(deductee, deducteeMaster);
				deducteeMaster.setId(deductee.getDeducteeMasterId());
				listDeductee.add(deducteeMaster);
			}
		}
		return listDeductee;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @throws Exception
	 */
	@Async
	public void asyncDeducteePANValidationReport(String deductorPan, String tenantId, String deductorTan,
			String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		deducteePANValidationReport(deductorPan, tenantId, deductorTan, userName);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @throws Exception
	 */
	public void deducteePANValidationReport(String deductorPan, String tenantId, String deductorTan, String userName)
			throws Exception {
		String fileName = "Deductee_Pan_Validation_Report_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date())
				+ ".xlsx";
		int year = CommonUtil.getAssessmentYear(null);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.DEDUCTEE_PAN_VALIDATION.name(), "Processing", month, userName, null, fileName);

		Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
		List<DeducteeMasterResidential> deducteeMasterResidentials = deducteeMasterResidentialDAO
				.findAllByDeductorPan(deductorPan);
		logger.info("deductees pan count :{}", deducteeMasterResidentials.size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Deductee Pan Validation");

		String[] mainHeaderNames = new String[] { "Vendor Details from Deductee Master", "",
				"Vendor Details from TRACES/NSDL Portal", "", "Vendor Name Comparison", "", "Interpretation" };
		worksheet.getCells().importArray(mainHeaderNames, 0, 0, false);

		String[] subHeaderNames = new String[] { "Deductee PAN", "Deductee Code", "Deductee Name", "Status",
				"Name from Traces", "Match score", "Match category", "Remarks", "Total sections" };
		worksheet.getCells().importArray(subHeaderNames, 1, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!deducteeMasterResidentials.isEmpty()) {
			int rowIndex = 2;
			for (DeducteeMasterResidential deducteeMasterResidential : deducteeMasterResidentials) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getDeducteePAN()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getDeducteePAN());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getDeducteeCode()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getDeducteeCode());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getDeducteeName()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getDeducteeName());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getDeducteeStatus()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getDeducteeStatus());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getNameAsPerTraces()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getNameAsPerTraces());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getMatchScore()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getMatchScore());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getPanStatus()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getPanStatus());
				rowData.add(StringUtils.isBlank(deducteeMasterResidential.getRemarksAsPerTraces()) ? StringUtils.EMPTY
						: deducteeMasterResidential.getRemarksAsPerTraces());
				// Total deductee sections count
				int sectionsCount = StringUtils.isBlank(deducteeMasterResidential.getSection()) ? 0 : 1;
				sectionsCount += deducteeMasterResidential.getAdditionalSections() != null
						? additionalSectionsSet.size()
						: 0;
				rowData.add(String.valueOf(sectionsCount));
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		// Style for A2 to H2 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(217, 217, 217));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A2:I2");
		headerColorRange1.setStyle(style1);

		// css for main heaeders
		Style mergeRange1Style = workbook.createStyle();
		mergeRange1Style.setForegroundColor(Color.fromArgb(157, 195, 230));
		mergeRange1Style.setPattern(BackgroundType.SOLID);
		mergeRange1Style.getFont().setBold(true);
		mergeRange1Style.setHorizontalAlignment(TextAlignmentType.CENTER);
		mergeRange1Style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

		// Merge cells
		Range mergeRange1 = worksheet.getCells().createRange("A1:C1");
		mergeRange1.merge();
		mergeRange1.setStyle(mergeRange1Style);

		Range mergeRange2 = worksheet.getCells().createRange("D1:E1");
		mergeRange2.merge();
		mergeRange2.setStyle(mergeRange1Style);

		Range mergeRange3 = worksheet.getCells().createRange("F1:G1");
		mergeRange3.merge();
		mergeRange3.setStyle(mergeRange1Style);

		worksheet.getCells().get("H1").setStyle(mergeRange1Style);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String cellname = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		Range range;
		if (!cellname.equalsIgnoreCase("I2")) {
			range = worksheet.getCells().createRange("A3:" + cellname);
			Style style = workbook.createStyle();
			style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

			Iterator<?> cellArray = range.iterator();
			while (cellArray.hasNext()) {
				Cell temp = (Cell) cellArray.next();
				// Saving the modified style to the cell.
				temp.setStyle(style);
			}
		} else {
			range = worksheet.getCells().createRange("A1:" + cellname);
			range.setOutlineBorders(CellBorderType.THIN, Color.getBlack());
		}
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A2:I2");
		workbook.save(out, SaveFormat.XLSX);

		saveBatchUploadReport(deductorTan, tenantId, year, out, 1L, UploadTypes.DEDUCTEE_PAN_VALIDATION.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
	}

	@Async
	public void asyncNRDeducteePANValidationReport(String deductorPan, String tenantId, String deductorTan,
			String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		deducteeNRPANValidationReport(deductorPan, tenantId, deductorTan, userName);
	}

	public void deducteeNRPANValidationReport(String deductorPan, String tenantId, String deductorTan, String userName)
			throws Exception {
		String fileName = "Deductee_NR_Pan_Validation_Report_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date())
				+ ".xlsx";
		int year = CommonUtil.getAssessmentYear(null);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.DEDUCTEE_PAN_VALIDATION.name(), "Processing", month, userName, null, fileName);

		Set<AdditionalSectionsDTO> additionalSectionsSet = new HashSet<>();
		List<DeducteeMasterNonResidential> deducteeMasterNonResidentials = deducteeMasterNonResidentialDAO
				.findAllByDeductorPan(deductorPan);
		logger.info("NR deductees pan count :{}", deducteeMasterNonResidentials.size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Deductee Pan Validation");

		String[] mainHeaderNames = new String[] { "Vendor Details from Deductee Master", "",
				"Vendor Details from TRACES/NSDL Portal", "", "Vendor Name Comparison", "", "Interpretation" };
		worksheet.getCells().importArray(mainHeaderNames, 0, 0, false);

		String[] subHeaderNames = new String[] { "Deductee PAN", "Deductee Code", "Deductee Name", "Status",
				"Name from Traces", "Match score", "Match category", "Remarks", "Total sections" };
		worksheet.getCells().importArray(subHeaderNames, 1, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!deducteeMasterNonResidentials.isEmpty()) {
			int rowIndex = 2;
			for (DeducteeMasterNonResidential deducteeMasterNonResidential : deducteeMasterNonResidentials) {
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getDeducteePAN()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getDeducteePAN());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getDeducteeCode()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getDeducteeCode());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getDeducteeName()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getDeducteeName());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getDeducteeStatus()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getDeducteeStatus());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getNameAsPerTraces()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getNameAsPerTraces());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getMatchScore()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getMatchScore());
				rowData.add(StringUtils.isBlank(deducteeMasterNonResidential.getPanStatus()) ? StringUtils.EMPTY
						: deducteeMasterNonResidential.getPanStatus());
				rowData.add(
						StringUtils.isBlank(deducteeMasterNonResidential.getRemarksAsPerTraces()) ? StringUtils.EMPTY
								: deducteeMasterNonResidential.getRemarksAsPerTraces());
				// Total deductee sections count
				int sectionsCount = StringUtils.isBlank(deducteeMasterNonResidential.getSection()) ? 0 : 1;
				sectionsCount += deducteeMasterNonResidential.getAdditionalSections() != null
						? additionalSectionsSet.size()
						: 0;
				rowData.add(String.valueOf(sectionsCount));
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		// Style for A2 to H2 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(217, 217, 217));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A2:I2");
		headerColorRange1.setStyle(style1);

		// css for main heaeders
		Style mergeRange1Style = workbook.createStyle();
		mergeRange1Style.setForegroundColor(Color.fromArgb(157, 195, 230));
		mergeRange1Style.setPattern(BackgroundType.SOLID);
		mergeRange1Style.getFont().setBold(true);
		mergeRange1Style.setHorizontalAlignment(TextAlignmentType.CENTER);
		mergeRange1Style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

		// Merge cells
		Range mergeRange1 = worksheet.getCells().createRange("A1:C1");
		mergeRange1.merge();
		mergeRange1.setStyle(mergeRange1Style);

		Range mergeRange2 = worksheet.getCells().createRange("D1:E1");
		mergeRange2.merge();
		mergeRange2.setStyle(mergeRange1Style);

		Range mergeRange3 = worksheet.getCells().createRange("F1:G1");
		mergeRange3.merge();
		mergeRange3.setStyle(mergeRange1Style);

		worksheet.getCells().get("H1").setStyle(mergeRange1Style);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String cellname = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		Range range;
		if (!cellname.equalsIgnoreCase("I2")) {
			range = worksheet.getCells().createRange("A3:" + cellname);
			Style style = workbook.createStyle();
			style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
			style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

			Iterator<?> cellArray = range.iterator();
			while (cellArray.hasNext()) {
				Cell temp = (Cell) cellArray.next();
				// Saving the modified style to the cell.
				temp.setStyle(style);
			}
		} else {
			range = worksheet.getCells().createRange("A1:" + cellname);
			range.setOutlineBorders(CellBorderType.THIN, Color.getBlack());
		}
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A2:I2");
		workbook.save(out, SaveFormat.XLSX);

		saveBatchUploadReport(deductorTan, tenantId, year, out, 1L, UploadTypes.DEDUCTEE_PAN_VALIDATION.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param tenantId
	 * @param assessmentYear
	 * @param out
	 * @param noOfRows
	 * @param uploadType
	 * @param status
	 * @param month
	 * @param userName
	 * @param batchId
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws StorageException
	 * @throws ParseException
	 */
	protected BatchUpload saveBatchUploadReport(String deductorTan, String tenantId, int assessmentYear,
			ByteArrayOutputStream out, Long noOfRows, String uploadType, String status, int month, String userName,
			Integer batchId, String fileName) throws FileNotFoundException, IOException, URISyntaxException,
			InvalidKeyException, StorageException, ParseException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Deductee pan validation report {} started for : {}", uploadType, userName);
		String path = null;
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setAssessmentYear(assessmentYear);
		batchUpload.setDeductorMasterTan(deductorTan);
		batchUpload.setUploadType(uploadType);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setStatus(status);
		batchUpload.setFilePath(path);
		batchUpload.setSuccessFileUrl(path);
		batchUpload.setFailedCount(Long.valueOf(0));
		batchUpload.setErrorFilePath(null);
		batchUpload.setRowsCount(noOfRows);
		batchUpload.setActive(true);
		if (StringUtils.isNotBlank(fileName)) {
			fileName = fileName + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
			batchUpload.setFileName(fileName);
		}
		List<BatchUpload> response = new ArrayList<>();
		if (batchId != null) {
			response = batchUploadDAO.findById(assessmentYear, deductorTan, uploadType, batchId);
		}
		if (!response.isEmpty()) {
			batchUpload = response.get(0);
			if (out != null) {
				File file = getConvertedExcelFile(out.toByteArray(), batchUpload.getFileName());
				path = blob.uploadExcelToBlobWithFile(file, tenantId);
				batchUpload.setStatus(status);
				batchUpload.setFilePath(path);
				batchUpload.setSuccessFileUrl(path);
				batchUpload.setRowsCount(noOfRows);
				batchUpload.setSuccessCount(noOfRows);
				batchUpload.setModifiedBy(userName);
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
			} else {
				batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setModifiedDate(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessStartTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setProcessEndTime(CommonUtil.fileUploadTime(new Date()));
				batchUpload.setRowsCount(0l);
			}
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload.setCreatedDate(CommonUtil.fileUploadTime(new Date()));
			batchUpload.setCreatedBy(userName);
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		logger.info("Deductee pan validation report {} completed for : {}", uploadType, userName);
		return batchUpload;
	}

	/**
	 * 
	 * @param byteArray
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
		File someFile = new File(fileName);
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(byteArray);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param modifiedName
	 * @param deducteePAN
	 * @param deducteeCode
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteePanModifiedName(String deductorPan,
			String modifiedName) {
		return deducteeMasterNonResidentialDAO.findAllByDeducteePanModifiedName(deductorPan, modifiedName);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteePan
	 * @return
	 */
	public List<DeducteeMasterResidential> getDeducteeEmails(String deductorPan, String deducteePan) {
		return deducteeMasterResidentialDAO.findAllByDeductorPanDeducteePan(deductorPan, deducteePan);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteePan
	 * @return
	 */
	public List<DeducteeMasterNonResidential> getDeducteeNonResidentEmails(String deductorPan, String deducteePan) {
		return deducteeMasterNonResidentialDAO.findAllByDeductorPanDeducteePan(deductorPan, deducteePan);
	}

	public Map<String, Integer> getActiveAndInactiveDeducteeCounts(String deductorPan, String type) {
		Map<String, Integer> deducteeCounts = null;
		if ("resident".equalsIgnoreCase(type)) {
			deducteeCounts = deducteeMasterResidentialDAO.getActiveAndInactiveResidentDeducteeCounts(deductorPan, type);
		} else {
			deducteeCounts = deducteeMasterNonResidentialDAO.getActiveAndInactiveNonResidentDeducteeCounts(deductorPan,
					type);
		}

		return deducteeCounts;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteePan
	 * @return
	 */
	public KYCDetails getKycDetailsBasedOnPan(String deductorPan, String deducteePan, String tan) {
		List<KYCDetails> kyc = kycDetailsDAO.getKycDetailsBasedOnPan(deductorPan, tan, deducteePan);
		if (!kyc.isEmpty()) {
			return kyc.get(0);
		} else {
			return new KYCDetails();
		}
	}

	/**
	 * 
	 * @param pan
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @throws Exception
	 */
	@Async
	public void asyncLdcValidationReport(String deductorPan, String tenantId, String deductorTan, String userName)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		ldcValidationReport(deductorPan, tenantId, deductorTan, userName);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tenantId
	 * @param deductorTan
	 * @param userName
	 * @throws Exception
	 */
	private void ldcValidationReport(String deductorPan, String tenantId, String deductorTan, String userName)
			throws Exception {
		String fileName = "Ldc_Validation_Report_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
		int year = CommonUtil.getAssessmentYear(null);
		int month = CommonUtil.getAssessmentMonthPlusOne(null);
		BatchUpload batchUpload = saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
				UploadTypes.LDC_VALIDATION_REPORT.name(), "Processing", month, userName, null, fileName);

		List<LdcMaster> ldcMasterList = ldcMasterDAO.getLdcByTan(deductorTan, tenantId);
		logger.info("ldc master count :{}", ldcMasterList.size());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Ldc Validation");

		String[] headerNames = new String[] { "Deductor TAN", "Deductor PAN", "LDC Certificate Number", "Deductee PAN",
				"Deductee Name", "Match category", "Sections" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!ldcMasterList.isEmpty()) {
			int rowIndex = 1;
			for (LdcMaster ldcMaster : ldcMasterList) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(
						StringUtils.isBlank(ldcMaster.getTanNumber()) ? StringUtils.EMPTY : ldcMaster.getTanNumber());
				rowData.add(deductorPan);
				rowData.add(StringUtils.isBlank(ldcMaster.getCertificateNumber()) ? StringUtils.EMPTY
						: ldcMaster.getCertificateNumber());
				rowData.add(StringUtils.isBlank(ldcMaster.getPan()) ? StringUtils.EMPTY : ldcMaster.getPan());
				rowData.add(StringUtils.isBlank(ldcMaster.getDeducteeName()) ? StringUtils.EMPTY
						: ldcMaster.getDeducteeName());
				rowData.add(
						StringUtils.isBlank(ldcMaster.getLdcStatus()) ? StringUtils.EMPTY : ldcMaster.getLdcStatus());
				rowData.add(StringUtils.isBlank(ldcMaster.getSection()) ? StringUtils.EMPTY : ldcMaster.getSection());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		// Style for A1 to G1 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(114, 159, 207));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A1:G1");
		headerColorRange1.setStyle(style1);

		// css for main heaeders
		Style mergeRange1Style = workbook.createStyle();
		mergeRange1Style.setForegroundColor(Color.fromArgb(157, 195, 230));
		mergeRange1Style.setPattern(BackgroundType.SOLID);
		mergeRange1Style.getFont().setBold(true);
		mergeRange1Style.setHorizontalAlignment(TextAlignmentType.CENTER);
		mergeRange1Style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
		mergeRange1Style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

		worksheet.autoFitColumns();
		worksheet.autoFitRows();

		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A1:G1");
		workbook.save(out, SaveFormat.XLSX);

		saveBatchUploadReport(deductorTan, tenantId, year, out, 1L, UploadTypes.LDC_VALIDATION_REPORT.name(),
				"Processed", month, userName, batchUpload.getBatchUploadID(), null);
	}

	public BatchUpload generateDeducteeNrStaggingFile(String tan, String pan, String tenantId, Integer year,
			Integer month) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
		batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
		batchUpload.setStatus("Processing");
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setMismatchCount(0L);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setActive(true);
		batchUpload.setAssessmentYear(year);
		batchUpload.setAssessmentMonth(month);
		batchUpload.setUploadType("DEDUCTEE_NR_STAGGING");
		batchUpload.setFileName("DEDUCTEE_NR_STAGGING" + "-" + year + "-" + month + "-" + new Date());
		batchUpload = batchUploadDAO.save(batchUpload);
		deducteeBulkService.generateDeducteeNrStaggingFile(tan, pan, tenantId, batchUpload);
		return batchUpload;
	}

	/**
	 * Responsible for retrieving both resident and Non resident deductees name
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<CustomDeducteesDTO> getListOfBothResidentAndNonResidentDeductees(String deductorPan) {
		List<CustomDeducteesDTO> aggrigatedList = deducteeMasterResidentialDAO.findAllByDeducteeNamePan(deductorPan);
		List<CustomDeducteesNonResidentDTO> listNR = deducteeMasterNonResidentialDAO
				.findAllByDeducteeNamePan(deductorPan);
		logger.info("Both Resident and Non resident deductees are retrieved {}");
		listNR.forEach(n -> {
			CustomDeducteesDTO dto = new CustomDeducteesDTO();
			dto.setDeducteeName(n.getDeducteeNames());
			dto.setDeducteePan(n.getDeducteePan());
			dto.setId(n.getId());
			aggrigatedList.add(dto);
		});
		return aggrigatedList;

	}

	public List<DeducteeMasterNonResidential> getNRDeductees(String deductorPan) {
		return deducteeMasterNonResidentialDAO.getNRDeductees(deductorPan);
	}

}