package com.ey.in.tds.tcs.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.tcs.TCSHsnCode;
import com.ey.in.tds.common.domain.tcs.TCSHsnRateMapping;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.domain.tcs.TCSRateMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.tcs.dto.TCSHsnSacDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.sac.TcsHsnSacExelData;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.tcs.repository.TCSHsnCodeRepository;
import com.ey.in.tds.tcs.repository.TCSHsnRateMappingRepository;
import com.ey.in.tds.tcs.repository.TCSMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 
 * @author scriptbees.
 *
 */
@Service
public class TCSSacService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;
	
	@Autowired
	private TCSHsnCodeRepository  tcsHsnCodeRepository;
	
	@Autowired
	private TCSNatureOfIncomeRepository  tcsNatureOfIncomeRepository;

	@Autowired
	private TCSHsnRateMappingRepository  tcsHsnRateMappingRepository;
	
	@Autowired
	private TCSMasterRepository  tcsMasterRepository;
	
	/**
	 * 
	 * @param multiPartFile
	 * @param deductorTan
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	public boolean saveFileData(MultipartFile multiPartFile, String deductorTan, String userName, String tenantId,
			String deductorPan) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = "";
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = MasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count {}:", headersCount);
			if (headersCount != 4) {
				logger.info("Column headers mismatch ");
				throw new CustomException("Column headers mismatch", HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				return processSacData(workbook, multiPartFile, sha256, deductorTan, userName, tenantId, deductorPan);
			}
		} catch (Exception e) {
			logger.error("Failed to process sac data", e);
			throw new RuntimeException("Failed to process sac data ", e);
		}
	}
	/**
	 * 
	 * @param workbook
	 * @param uploadedFile
	 * @param sha256
	 * @param deductorTan
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @return
	 * @throws Exception
	 */
	@Async
	public Boolean processSacData(XSSFWorkbook workbook, MultipartFile uploadedFile, String sha256, String deductorTan,
			String userName, String tenantId, String deductorPan) throws Exception {
		MultiTenantContext.setTenantId("master");
		try {
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> itr = sheet.iterator();
			int totalRows = sheet.getPhysicalNumberOfRows();
			Map<String, Integer> map = new HashMap<>();
			// get column names
			while (itr.hasNext()) {
				Row row = itr.next();
				short minColIx = row.getFirstCellNum(); // get the first column index for a row
				short maxColIx = row.getLastCellNum(); // get the last column index for a row
				for (short colIx = minColIx; colIx < maxColIx; colIx++) { // loop from first to last index
					Cell cell = row.getCell(colIx); // get the cell
					map.put(cell.getStringCellValue(), cell.getColumnIndex()); // add the cell contents (name of column)
																				// and cell index to the map
				}
				break;
			}
			List<TcsHsnSacExelData> sacList = new ArrayList<>();
			// get the column index for the column with header name
			int idxForHsnOrSac = map.get("HSN/SAC Code");
			int idxForDesc = map.get("Description");
			int idxForTcsSection = map.get("TCS Section");
			int idxForNOC = map.get("Nature of Income");
			for (int x = 1; x <= totalRows; x++) {
				TcsHsnSacExelData tcsHsnExcelData = new TcsHsnSacExelData(); // Data structure to hold the data from the xls file.
				XSSFRow dataRow = sheet.getRow(x); // get row 1 to row n (rows containing data)
				if (dataRow == null) {
					continue;
				}
				Cell hsnOrSac = dataRow.getCell(idxForHsnOrSac); // Get the cells for each of the indexes
				Cell desc = dataRow.getCell(idxForDesc);
				Cell noc = dataRow.getCell(idxForNOC);
				Cell tcsSection = dataRow.getCell(idxForTcsSection);
				try {
					// Get the values out of those cells and set them
					CellType cellType = hsnOrSac.getCellType();
					if (cellType.equals(CellType.STRING)) {
						tcsHsnExcelData.setHsnOrSacCode(hsnOrSac.getStringCellValue().trim() + "");
					} else {
						tcsHsnExcelData.setHsnOrSacCode((long)hsnOrSac.getNumericCellValue() + "");
					}
					tcsHsnExcelData.setDescription(desc.getStringCellValue());
					if (noc != null) {
						tcsHsnExcelData.setNatureOfIncome(noc.getStringCellValue());
					}
					if (tcsSection != null) {
						tcsHsnExcelData.setTcsSection(tcsSection.getStringCellValue());
					}
					createTcsHsnCode(userName, tcsHsnExcelData);
					sacList.add(tcsHsnExcelData);
				} catch (Exception e) {
					logger.error("Exception occurred :", e);
				}
			}
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(sacList);
			String redisKey = "ref_tcs";
			logger.info("sac data pushing to redis");
			redisUtilUserTenantInfo.putValue(redisKey, jsonString);
		
		} catch (Exception e) {
			logger.error("Exception occurred :", e);
		}
		return true;
	}
	/**
	 * 
	 * @param userName
	 * @param tcsHsnCode
	 * @param tcsHsnRateMapping
	 * @param tcsHsnExcelData
	 * @return
	 */
	private void createTcsHsnCode(String userName, TcsHsnSacExelData tcsHsnExcelData) {
		// Using NOI ID, insert record in hsn_code, code, description, noiId
		if (StringUtils.isNotBlank(tcsHsnExcelData.getTcsSection())
				&& StringUtils.isNotBlank(tcsHsnExcelData.getNatureOfIncome())) {
			TCSHsnCode tcsHsnCode = new TCSHsnCode();
			TCSHsnRateMapping tcsHsnRateMapping = new TCSHsnRateMapping();
			TCSNatureOfIncome natureOfIncomeId = tcsNatureOfIncomeRepository
					.getNatureOfId(tcsHsnExcelData.getTcsSection(), tcsHsnExcelData.getNatureOfIncome());
			if (natureOfIncomeId != null) {
				tcsHsnCode.setNatureOfIncomeId(natureOfIncomeId.getId());
				tcsHsnCode.setHsnCode(Long.valueOf(tcsHsnExcelData.getHsnOrSacCode()));
				tcsHsnCode.setDescription(tcsHsnExcelData.getDescription());
				tcsHsnCode.setActive(true);
				tcsHsnCode.setCreatedBy(userName);
				tcsHsnCode.setCreatedDate(Instant.now());
				tcsHsnCode.setModifiedBy(userName);
				tcsHsnCode.setModifiedDate(Instant.now());
				// save in tcs_hcs_code
				tcsHsnCode = tcsHsnCodeRepository.save(tcsHsnCode);
				logger.info("HSN Record saved with code {}:", tcsHsnCode.getHsnCode());
				tcsHsnRateMapping.setHsnCodeId(tcsHsnCode.getId());
				TCSRateMaster tcsRateMaster = tcsMasterRepository.getTcsRateMasterRate(natureOfIncomeId.getId());
				if (tcsRateMaster != null) {
					tcsHsnRateMapping.setTcsMasterId(tcsRateMaster.getId());
				} else {
					throw new CustomException("TCS Rate Master ID not found");
				}
				// save in tcs_hsn_rate_mapping
				tcsHsnRateMapping.setActive(true);
				tcsHsnRateMapping.setCreatedBy(userName);
				tcsHsnCode.setCreatedDate(Instant.now());
				tcsHsnCode.setModifiedBy(userName);
				tcsHsnCode.setModifiedDate(Instant.now());
				tcsHsnRateMappingRepository.save(tcsHsnRateMapping);
			}
		}
	} 

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<TcsHsnSacExelData> getAllSacs() throws Exception {
		List<TcsHsnSacExelData> sacs = new ArrayList<>();
		try {
			sacs.addAll(redisUtilUserTenantInfo.fetchTcsSACRedisData("ref_tcs"));
		} catch (Exception e) {
			logger.info("Data not found in redis");
		}
		logger.info("Retrieved data : {}", sacs);
		return sacs;
	}
	/**
	 * 
	 * @return
	 */
	public CommonDTO<TCSHsnSacDTO> getAllHsnSac(Pagination pagination) {

		List<TCSHsnSacDTO> sacs = new ArrayList<>();
		Long listHsnCodeCount = tcsHsnCodeRepository.count();

		Pageable pageable = PageRequest.of(pagination.getPageNumber() - 1, pagination.getPageSize(),
				Sort.by("id").descending());

		Page<TCSHsnCode> listHsnCodeWithPagination = tcsHsnCodeRepository.findAll(pageable);

		if (listHsnCodeCount > 0) {
			for (TCSHsnCode hsnCode : listHsnCodeWithPagination.getContent()) {
				TCSHsnSacDTO hsnSacDTO = new TCSHsnSacDTO();
				hsnSacDTO.setHsnSacCode(hsnCode.getHsnCode());
				hsnSacDTO.setDesc(hsnCode.getDescription());
				TCSNatureOfIncome noi = tcsNatureOfIncomeRepository.getNatureOfIncome(hsnCode.getNatureOfIncomeId());
				if (noi != null) {
					hsnSacDTO.setNatureOfIncome(noi.getNature());
					hsnSacDTO.setTcsSection(noi.getSection());
					sacs.add(hsnSacDTO);
				}
			}
		}

		PagedData<TCSHsnSacDTO> pagedData = new PagedData<>(sacs, sacs.size(), pagination.getPageNumber(),
				listHsnCodeCount > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<TCSHsnSacDTO> commonDTO = new CommonDTO<>();
		commonDTO.setResultsSet(pagedData);
		commonDTO.setCount(BigInteger.valueOf(listHsnCodeCount));
		logger.info("Retrieved data : {}", sacs);

		return commonDTO;
	}

}
