package com.ey.in.tds.ingestion;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;

public class AsposeExcelConversion {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		long start = System.currentTimeMillis();

		// Load the input Excel file
		Workbook workbook = new Workbook("TCS_Payment_template.xlsx");

		// Save output CSV file
		workbook.save("TCS_Payment_template.csv", SaveFormat.CSV);
		long end = System.currentTimeMillis();
		

		//Test commit 1
		
		//Test commit 2
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");

	}

}
