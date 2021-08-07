package com.ey.in.tds.onboarding;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;

import com.ey.in.tds.common.dto.CustomSectionRateDTO;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class TestBalancesUpdate {

	public static void main(String[] args) throws EncryptedDocumentException, IOException {
		String connectionUrl = "jdbc:sqlserver://cisttfodb001.database.windows.net:1433;" + "database=tds_ay200002_uat;"
				+ "user=Appuser_ay200002UAT;" + "password=E+9rBX)}a\\R9+4t6;" + "encrypt=true;"
				+ "trustServerCertificate=false;" + "loginTimeout=30;";

		try (Connection connection = DriverManager.getConnection(connectionUrl);
				Statement statement = connection.createStatement();) {

			File csvFile = new File("/home/amani/Documents/ThrsholdBalances.csv");

			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
			int rowCount = csv.getRowCount();
			System.out.println("rowCount:" + rowCount);
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setGroupingSeparator(',');
			symbols.setDecimalSeparator('.');
			String pattern = "#,##0.0#";
			DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
			decimalFormat.setParseBigDecimal(true);
			// reading csv file and processing records
			Map<String, List<String>> collecteeCodeMap = new HashMap<String, List<String>>();
			Map<String, BigDecimal> collecteeCodeAmountMap = new HashMap<String, BigDecimal>();
			for (CsvRow tcsGstPrRow : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(tcsGstPrRow.getFields(), ", ").replaceAll(",", ""))) {
					if (StringUtils.isNotBlank(tcsGstPrRow.getField("CustomerCode"))) {
						String customerCode = tcsGstPrRow.getField("CustomerCode");
						System.out.println("Cstomer code:" + customerCode);

						BigDecimal balnceAmountBi = BigDecimal.ZERO;
						if (StringUtils.isNotBlank(tcsGstPrRow.getField("Amounts"))) {
							String balceAmount = tcsGstPrRow.getField("Amounts");

							System.out.println("Amount" + balceAmount.replaceAll(",", ""));
							Double obj = new Double(balceAmount.replaceAll(",", ""));

							balnceAmountBi = BigDecimal.valueOf(obj);

						}
						// Create and execute a SELECT SQL statement.
						String selectSql = "select collectee_pan from Client_Masters.collectee_master where collectee_code = '"
								+ customerCode + "'";
						ResultSet resultSet1 = statement.executeQuery(selectSql);

						while (resultSet1.next()) {
							String collecteePan = resultSet1.getString(1);
							System.out.println(resultSet1.getString(1));

							List<String> codes = new ArrayList<>();
							if (collecteeCodeMap.get(collecteePan) != null) {
								codes = collecteeCodeMap.get(collecteePan);
							}
							codes.add(customerCode);
							collecteeCodeMap.put(collecteePan, codes);

							BigDecimal amount = BigDecimal.valueOf(0);
							if (collecteeCodeAmountMap.get(collecteePan) != null) {
								amount = collecteeCodeAmountMap.get(collecteePan);
							}
							amount = amount.add(balnceAmountBi);
							collecteeCodeAmountMap.put(collecteePan, amount);

						}

					}

				}
			}

			System.out.println("map:" + collecteeCodeMap);

			System.out.println("map:" + collecteeCodeAmountMap);

			for (Map.Entry<String, BigDecimal> entry : collecteeCodeAmountMap.entrySet()) {

				String selectSql3 = "update Transactions.collectee_noi_threshold_ledger set amount_utilized ="
						+ entry.getValue() + " where collectee_pan = '" + entry.getKey() + "' and year = 2022";

				// update amount
				statement.executeUpdate(selectSql3);
				
				System.out.println("updated for collectee pan-" + entry.getKey()+ " And Amunt:" + entry.getValue());

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
