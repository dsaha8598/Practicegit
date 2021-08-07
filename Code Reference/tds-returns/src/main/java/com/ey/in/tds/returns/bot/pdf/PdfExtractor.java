package com.ey.in.tds.returns.bot.pdf;

import com.ey.in.tds.returns.bot.domain.Form15CBDetails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PdfExtractor {

    private Logger logger = LoggerFactory.getLogger(PdfExtractor.class);

    public Form15CBDetails extractFromFileAtPath(String absFilePath) {
        String text = extractText(absFilePath);
        String remitteeName = extractTextBetweenString("Name of the Beneficiary of the remittance",
                "Flat/ Door/ Block No", text);
        String flatDoorBlockNum = extractTextBetweenString("Flat/ Door/ Block No",
                "Name of premises/ Building/ Village", text);
        String buildingVillagePremises = extractTextBetweenString("Name of premises/ Building/ Village",
                "Road/ Street", text);
        String roadStreet = extractTextBetweenString("Road/ Street",
                "Area/ Locality", text);
        String areaLocality = extractTextBetweenString("Area/ Locality", "Town/ City / District"
                , text);
        String townCityDistrict = extractTextBetweenString("Town/ City / District", "State", text);
        String state = extractTextBetweenString("State", "Country", text);
        String country = extractTextBetweenString("Country", "ZipCode", text);
        String zipCode = extractTextBetweenString("ZipCode", "B. REMITTANCE", text);
        String ackNum = extractTextBetweenString("Acknowledgement Number", "This form has been digitally",
                text);
        String proposedDateOfRemitance = extractTextBetweenString("Proposed date of remittance",
                "Nature of remittance as", text);
        String ackDate = extractSigningDate(text);
        return new Form15CBDetails(remitteeName, flatDoorBlockNum, buildingVillagePremises, townCityDistrict, areaLocality,
                zipCode, state, roadStreet, country, ackNum, proposedDateOfRemitance, null,
                ackDate);
    }

    private String extractText(String absFilePath) {
        StringBuilder content = new StringBuilder();
        PDFTextStripper tStripper = null;
        PDDocument document = null;
        try {
            tStripper = new PDFTextStripper();
            tStripper.setStartPage(1);
            tStripper.setEndPage(3);
            document = PDDocument.load(new File(absFilePath));
            if (!document.isEncrypted()) {
                String pdfFileInText = tStripper.getText(document);
                String[] lines = pdfFileInText.split("\n");
                for (String line : lines) {
                    content.append(line);
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load PDF file at {}", absFilePath);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    logger.error("Failed to close PDF file {}", absFilePath, e);
                }
            }
        }
        return content.toString().trim();
    }

    private String extractSigningDate(String text) {
        String part1 = extractTextBetweenString("This form has been digitally signed by",
                "Sl No and issuer ",text);
        String part2 = extractTextBetweenString("on","Dsc",part1).trim();
        if(part2.charAt(part2.length()-1)=='.') {
            part2 = part2.substring(0,part2.length()-1).trim();
        }
        return part2.substring(0,part2.indexOf(" "));
    }

    private String extractTextBetweenString(String start, String end, String originalText) {
        StringBuilder content = new StringBuilder();
        Pattern pattern = Pattern.compile(start + "(.*?)" + end, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(originalText);
        while (matcher.find()) {
            content.append(matcher.group(1));
        }
        return content.toString().trim();
    }
}
