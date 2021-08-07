package com.ey.in.tds.onboarding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.CollecteeDeclaration;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class TestExcelConversion {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		File file = new File("HYDS35527C.TU422_Compliance_Check_206AB_206CCA_27-06-20214679683871734694348.xlsx");
		Workbook workbook= new Workbook(file.getAbsolutePath());
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.getCells().deleteRows(0, 4);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.CSV);
		File xlsxInvoiceFile = new File("TestCsvFile");
		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);
		System.out.println(csv.getRowCount());
		for (CsvRow row : csv.getRows()) {
			
			String pan = row.getField("PAN");
			// Name
			String name = row.getField("Name");
			// PAN Allotment Date
			String panAllotmentDate = row.getField("PAN Allotment Date");
			// PAN-Aadhaar Link Status
			String panAadhaarLinkStatus = StringUtils.isNotBlank(row.getField("PAN-Aadhaar Link Status"))
					? row.getField("PAN-Aadhaar Link Status")
					: null;
			// Specified Person u/s 206AB & 206CCA
			String specifiedPerson = row.getField("Specified Person u/s 206AB & 206CCA");

			DeducteeDeclaration deducteeDeclaration = new DeducteeDeclaration();
			CollecteeDeclaration collecteeDeclaration = new CollecteeDeclaration();
			ShareholderDeclaration shareholderDeclaration = new ShareholderDeclaration();

			if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(pan)) {
				String rateType = StringUtils.EMPTY;
				System.out.println(rateType+"Inside");

			}else {
				System.out.println("Specified Person is Mandatory");
			}
		}
	}

}
