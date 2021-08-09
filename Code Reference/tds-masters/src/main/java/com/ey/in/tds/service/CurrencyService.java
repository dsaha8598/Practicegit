package com.ey.in.tds.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.Currency;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.repository.CurrencyRepository;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.microsoft.azure.storage.StorageException;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Service
public class CurrencyService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	public Currency saveCurrency(@Valid Currency currency, String userName) {

		currency.setActive(true);
		currency.setCreatedBy(userName);
		currency.setCreatedDate(Instant.now());
		return currencyRepository.save(currency);
	}

	public List<Currency> getAllCurrency() {

		return currencyRepository.findAll();
	}

	public List<Currency> getCurrencyDate(Date date) {
		return currencyRepository.byDate(date);
	}

	public Currency getByCurrencyId(Long id) {
		Currency currency = new Currency();
		Optional<Currency> currencyObj = currencyRepository.findById(id);
		if (currencyObj.isPresent()) {
			return currencyObj.get();
		}
		return currency;
	}

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {
		String uploadType = UploadTypes.CURRENCY_PDF.name();
		MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
		String sha256 = sha256SumService.getSHA256Hash(file);
		if (isAlreadyProcessed(sha256)) {
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			masterBatchUpload.setSuccessCount(0L);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setRowsCount(0L);
			masterBatchUpload.setProcessed(0);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setStatus("Duplicate");
			masterBatchUpload.setNewStatus("Duplicate");
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
					assessmentMonth, userName, null, uploadType);
			return masterBatchUpload;
		} else {
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setStatus("Processing");
			masterBatchUpload.setSuccessCount(0L);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setRowsCount(0L);
			masterBatchUpload.setProcessed(0);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
					assessmentMonth, userName, null, uploadType);
			return readCurrencyPdf(file, sha256, assesssmentYear, assessmentMonth, userName, masterBatchUpload,
					uploadType);
		}
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		Optional<MasterBatchUpload> sha256Record = masterBatchUploadRepository.getSha256Records(sha256Sum);
		return sha256Record.isPresent();
	}

	/**
	 * 
	 * @param workbook
	 * @param file
	 * @param sha256
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param masterBatchUpload
	 * @param uploadType
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Async
	public MasterBatchUpload readCurrencyPdf(MultipartFile file, String sha256, int assesssmentYear,
			int assessmentMonth, String userName, MasterBatchUpload masterBatchUpload, String uploadType)
			throws IOException, ParseException, InvalidKeyException, URISyntaxException, StorageException {

		File files = null;
		File currencyErrorFile = null;

		// converting multipartFile to file
		files = convertMultipartToFile(file);

		// converting PDF to Text
		List<Currency> currencies = convertPDFToTextByParser(files.getAbsolutePath());

		// get all currency
		List<Currency> existingCurrencies = currencyRepository.findAll();

		Date processingDate = currencies.get(0).getDate();
		String processedDate = new SimpleDateFormat("dd/MM/yyyy").format(processingDate);
		// deleting all currencies
		for (Currency existingCurrency : existingCurrencies) {
			Date date = existingCurrency.getDate();
			String existingCurrencyDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
			if (existingCurrencyDate.equals(processedDate)) {
				existingCurrency.setActive(false);
				currencyRepository.save(existingCurrency);
			}
		}
		// save all crrency
		currencies.forEach(currency -> {
			currency.setCreatedBy(userName);
			currency.setFileName(file.getOriginalFilename());
			currencyRepository.save(currency);
		});

		masterBatchUpload.setSha256sum(sha256);
		masterBatchUpload.setMismatchCount(0L);
		masterBatchUpload.setRowsCount(Long.valueOf(currencies.size()));
		masterBatchUpload.setSuccessCount(Long.valueOf(currencies.size()));
		masterBatchUpload.setFailedCount(0L);
		masterBatchUpload.setFileName(file.getOriginalFilename());
		masterBatchUpload.setProcessed(currencies.size());
		masterBatchUpload.setDuplicateCount(0L);
		masterBatchUpload.setStatus("Processed");
		masterBatchUpload.setCreatedDate(Instant.now());
		masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
		masterBatchUpload.setCreatedBy(userName);

		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, currencyErrorFile, uploadType);
	}

	public List<Currency> convertPDFToTextByParser(String filePath) throws IOException {

		PDDocument pd = PDDocument.load(new File(filePath));
		ObjectExtractor oe = new ObjectExtractor(pd);

		// Tabula algo.
		SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

		// extract only the first page
		Page page = oe.extract(1);

		List<Table> table = sea.extract(page);

		logger.info("Tables detected: " + table);

		List<Currency> currencies = new ArrayList<>();
		logger.info(table.toString());
		final AtomicInteger rowIndexHolder = new AtomicInteger();
		int rowSize = table.get(0).getRows().size();
		List<Date> cDates = new ArrayList<>();
		table.get(0).getRows().forEach(row -> {
			int rowIndex = rowIndexHolder.getAndIncrement();
			Date currencyDate = null;
			if (rowIndex > 2 && rowIndex <= rowSize - 2) {
				Currency currency = new Currency();
				currency.setActive(true);
				final AtomicInteger indexHolder = new AtomicInteger();
				row.forEach(cell -> {
					int columnIndex = indexHolder.getAndIncrement();
					if (columnIndex == 0) {
						currency.setCurrencyName(cell.getText());
					} else if (columnIndex == 1) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setCrossSellingRate(new Double(0));
						} else {
							currency.setCrossSellingRate(Double.valueOf(cell.getText()));
						}
					} else if (columnIndex == 2) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setCrossBuyingRate(new Double(0));
						} else {
							currency.setCrossBuyingRate(Double.valueOf(cell.getText()));
						}
					} else if (columnIndex == 3) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setBuyingTT(new Double(0));
						} else {
							currency.setBuyingTT(Double.valueOf(cell.getText()));
						}
					} else if (columnIndex == 4) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setBuyingBill(new Double(0));
						} else {
							currency.setBuyingBill(Double.valueOf(cell.getText()));
						}
					} else if (columnIndex == 5) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setSellingTT(new Double(0));
						} else {
							currency.setSellingTT(Double.valueOf(cell.getText()));
						}
					} else if (columnIndex == 6) {
						if (StringUtils.isBlank(cell.getText())) {
							currency.setSellingBill(new Double(0));
						} else {
							currency.setSellingBill(Double.valueOf(cell.getText()));
						}
					}
				});
				if (!cDates.isEmpty()) {
					currency.setDate(cDates.get(0));
				} else {
					currency.setDate(new Date());
				}
				currencies.add(currency);
			} else if (rowIndex == 0) {
				String dateString = row.get(0).getText().substring(81, 91);
				String timeString = row.get(0).getText().substring(132, 140);
				SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				try {
					currencyDate = sdfDate.parse(dateString + " " + timeString);
					cDates.add(currencyDate);
				} catch (ParseException e) {
					// Do nothing
				}
			}

		});
		return currencies;
	}

	public File convertMultipartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(convFile)) {
			fos.write(file.getBytes());
			fos.close();
		}
		return convFile;
	}
	
	public BigDecimal getCurrencyAmountCalculation(int foreignCurrency, String currencyType) {
		Optional<Currency> currencyResponse = currencyRepository.getByCurrentDateRate(currencyType);
		Currency currency = null;
		double amount = 0;
		if (currencyResponse.isPresent()) {
			currency = currencyResponse.get();
			amount = foreignCurrency * currency.getBuyingTT();
		}
		return BigDecimal.valueOf(amount);
	}
	
	public List<Map<String, Object>> getAllCurrencyData() {

		return currencyRepository.getAllCurrencyData();
	}

}
