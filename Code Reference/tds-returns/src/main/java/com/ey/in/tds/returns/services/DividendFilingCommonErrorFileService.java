package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.returns.dividend.validator.Errors;
import com.microsoft.azure.storage.StorageException;

/**
 * this file is having the logic to generate error file with reason for not
 * generating the filing
 * 
 * @author dipak
 *
 */
@Service
public class DividendFilingCommonErrorFileService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected BlobStorage blobStorage;

	public File generateErrorFile(List<Errors> listBook)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Error Sheet");
			sheet.setColumnWidth(0, 6000);
			sheet.setColumnWidth(1, 9000);
			sheet.setColumnWidth(2, 18000);
			sheet.setDefaultRowHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
			sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));
			sheet.setDisplayGridlines(false);
			
			Font font = workbook.createFont();// Create font
			font.setBold(true);// Make font bold
			
			XSSFCellStyle style1 = createStyle(workbook, 214, 214, 214, font, defaultIndexedColorMap);// Create style
			XSSFCellStyle style2 = createStyle(workbook, 249, 180, 123, font, defaultIndexedColorMap);// Create style
			XSSFCellStyle style3 = createStyle(workbook, 189, 224, 151, font, defaultIndexedColorMap);// Create style
			
			XSSFCellStyle dataRowStyle=(XSSFCellStyle) workbook.createCellStyle();
			dataRowStyle.setWrapText(true);
			dataRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			dataRowStyle.setAlignment(HorizontalAlignment.CENTER);
			dataRowStyle.setBorderBottom(BorderStyle.THIN);
			dataRowStyle.setBorderLeft(BorderStyle.THIN);
			dataRowStyle.setBorderTop(BorderStyle.THIN);
			dataRowStyle.setBorderRight(BorderStyle.THIN);
			dataRowStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255), defaultIndexedColorMap));
			
			Row headerRow = sheet.createRow(0);
			writeHeader(headerRow, style1,style2,style3);
			int rowCount = 1;
			for (Errors aBook : listBook) {
				Row row = sheet.createRow(rowCount);
				writeBook(aBook, row,dataRowStyle);
				rowCount++;
			}
			File tempFile = File.createTempFile("Error_Excel_" + System.currentTimeMillis(), ".xls");
			try (FileOutputStream outputStream = new FileOutputStream(tempFile.getAbsoluteFile())) {
				workbook.write(outputStream);
			}
			return tempFile;
		}
	}

	private void writeHeader(Row row, XSSFCellStyle style1,XSSFCellStyle style2,XSSFCellStyle style3) {
		Cell cell0 = row.createCell(0);
		cell0.setCellValue("TRANSACTION ID");
		cell0.setCellStyle(style1);

		Cell cell = row.createCell(1);
		cell.setCellValue("UNIQUE SHAREHOLDER IDENTIFICATION NUMBER");
		cell.setCellStyle(style2);

		Cell cell2 = row.createCell(2);
		cell2.setCellValue("ERROR MESSAGE");
		cell2.setCellStyle(style3);

	}

	public void writeBook(Errors errors, Row row,XSSFCellStyle dataRowStyle) {
		{
			Cell cell0 = row.createCell(0);
			cell0.setCellValue(errors.getCompleteMessage() == null ? "" : errors.getCompleteMessage());
			cell0.setCellStyle(dataRowStyle);

			Cell cell = row.createCell(1);
			cell.setCellValue(errors.getId() == null ? "" : errors.getId() + "");
			cell.setCellStyle(dataRowStyle);

			Cell cell2 = row.createCell(2);
			cell2.setCellValue(errors.getMessage() == null ? "" : errors.getMessage());
			cell2.setCellStyle(dataRowStyle);

		}
	}
	
	private XSSFCellStyle createStyle(XSSFWorkbook workbook,int r,int g,int b,Font font,DefaultIndexedColorMap defaultIndexedColorMap) {
		XSSFCellStyle style=(XSSFCellStyle) workbook.createCellStyle();
		style.setFont(font);// set it to
		style.setWrapText(true);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(new XSSFColor(new java.awt.Color(r, g, b), defaultIndexedColorMap));
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		return style;
	}
}
