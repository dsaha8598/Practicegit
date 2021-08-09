package com.ey.in.tds.onboarding.util;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class PDFReadingTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File convFile = new File("/Users/venkat/dev/git/TFO_TDS/Engineering/Code and Unit Testing/TDS/Services/tds-onboarding/src/test/resources/Certificate_197DELA1XXXXC_2019_1ABXXXXXUE.pdf");
		

		PDDocument pd = PDDocument.load(convFile);
		ObjectExtractor oe = new ObjectExtractor(pd);
		SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();// Tabula algo.

		int count = pd.getNumberOfPages();

		for (int j = 1; j <= count; j++) {
			Page page = oe.extract(j); // extract pages
			List<Table> listTable = sea.extract(page);
			//logger.debug("No of Tables in Page:" + j + ":" + listTable.size());

			// iterating each table
			for (Table table : listTable) {
				final AtomicInteger rowIndexHolder = new AtomicInteger();
				int rowSize = table.getRows().size();
				table.getRows().forEach(row -> {

					LdcMaster ldcData1 = new LdcMaster();
			
					ldcData1.setUtilizedAmount(BigDecimal.ZERO);

					int rowIndex = rowIndexHolder.getAndIncrement();
					final AtomicInteger indexHolder = new AtomicInteger();
					// restricting to iterate header row
					if (rowIndex > 0 && rowSize >= rowIndex) {
						row.forEach(cell -> {
							int columnIndex = indexHolder.getAndIncrement();
							if (columnIndex == 0) {
								// ldcData.s(cell.getText());
							} else if (columnIndex == 1) {
								ldcData1.setCertificateNumber(cell.getText());
							} else if (columnIndex == 2) {
								//ldcData1.getKey().setPan(cell.getText());
							} else if (columnIndex == 3) {
								ldcData1.setDeducteeName((cell.getText()));
							} else if (columnIndex == 4) {
								ldcData1.setSection(cell.getText());
							} else if (columnIndex == 5) {
								if (!StringUtils.isAllBlank(cell.getText())) {
									try {
										ldcData1.setAmount(new BigDecimal((cell.getText())));
									} catch (Exception e) {
										//logger.error("error occured while parsing ammount  :" + e);
									}
								}
							} else if (columnIndex == 6) {
								if (!StringUtils.isAllBlank(cell.getText())) {
									try {
										ldcData1.setRate((new BigDecimal((cell.getText()))));
									} catch (Exception e) {
										//logger.error("error occured while parsing ammount  :" + e);
									}
								}
							} else if (columnIndex == 7) {
								try {
									ldcData1.setApplicableFrom(new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
								} catch (Exception e) {
									//logger.error("error while parsing date :" + e);
									ldcData1.setApplicableFrom(new Date());
								}
							} else if (columnIndex == 8) {
								try {
									ldcData1.setApplicableTo(new SimpleDateFormat("dd-MMM-yyyy").parse(cell.getText()));
								} catch (Exception e) {
									//logger.error("error while parsing date :" + e);
									ldcData1.setApplicableTo(new Date());
								}
							}

						});// inner forEach
						

						

					} // if block
					System.out.println(ldcData1.getApplicableTo());
					System.out.println(ldcData1.getApplicableFrom());
					
				}); // outer forEach for
			} // inner for
		} // outer for
	}

}
