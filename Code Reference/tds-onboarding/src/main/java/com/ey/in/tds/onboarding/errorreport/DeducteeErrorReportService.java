package com.ey.in.tds.onboarding.errorreport;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.Style;
import com.aspose.cells.Worksheet;

@Service
public class DeducteeErrorReportService {

	public void setColorsBasedOnErrorCodes(Worksheet worksheet, int rowIndex, Map<String, String> errorCodesMap,
			String headerName, String cellLetter) {
		if (errorCodesMap.containsKey(headerName.toLowerCase())) {
			Cell cell = worksheet.getCells().get(cellLetter + (rowIndex + 1));
			Style style = cell.getStyle();
			style.setForegroundColor(Color.fromArgb(255, 0, 0));
			style.setPattern(BackgroundType.SOLID);
			cell.setStyle(style);
		}
	}

}
