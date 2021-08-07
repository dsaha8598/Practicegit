package com.ey.in.tds.dividend.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tds.returns.dividend.validator.Errors;

public class Form15XmlUtil {

//	public static final String SAVE_FILE_TYPE = Configs.getConfigProperty("final.save.file.type");
//	public static final String SAVE_FILE_TYPE_DESC = Configs.getConfigProperty("final.save.file.type.desc");

//	public static Document getDocument(File file) throws SAXException, IOException, ParserConfigurationException {
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		dbf.setNamespaceAware(true);
//		InputStream is = null;
//		if (file.getName().endsWith(".draft") && "Y".equals(Configs.getConfigProperty("encode.draft.flag"))) {
//			is = new Base64InputStream(new FileInputStream(file));
//		} else if (file.getName().endsWith(".zip")
//				&& ".zip".equals(Configs.getConfigProperty("final.save.file.type"))) {
//			is = new ZipInputStream(new FileInputStream(file));
//			ZipEntry ze = ((ZipInputStream) is).getNextEntry();
//			LoggerManager.LOG.info("ze : " + ze);
//		} else {
//			is = new BufferedInputStream(new FileInputStream(file));
//		}
//
//		Document doc = dbf.newDocumentBuilder().parse((InputStream) is);
//		((InputStream) is).close();
//		return doc;
//	}

//	public static Object unmarshal(File xmlFile) throws Exception {
//		String pckg = Configs.getConfigProperty("jaxb.context.package.name");
//		return unmarshal(xmlFile, pckg);
//	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static Object unmarshalForm15CB(File xmlFile, String pckg) throws Exception {
//		Document document = getDocument(xmlFile);
//		String rootName = document.getDocumentElement().getLocalName();
//		LoggerManager.LOG.info("rootName : " + rootName);
//
//		try {
//			if (rootName != null && rootName.toUpperCase().startsWith("FORM")) {
//				JAXBContext jaxbContext = JAXBContext.newInstance(pckg);
//				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//				SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//				Schema schema = schemaFactory
//						.newSchema(Form15XmlUtil.class.getResource(Configs.getConfigProperty("xsd15CB.file.path")));
//				unmarshaller.setSchema(schema);
//				Object root = unmarshaller.unmarshal(document);
//				JAXBElement<Object> obj = null;
//				Object jaxbModel = null;
//				if (root instanceof JAXBElement) {
//					obj = (JAXBElement) root;
//					jaxbModel = obj.getValue();
//				} else {
//					jaxbModel = root;
//				}
//
//				return jaxbModel;
//			}
//		} catch (Exception var11) {
//			var11.printStackTrace();
//		}
//
//		return null;
//	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static Object unmarshal(File xmlFile, String pckg) throws Exception {
//		Document document = getDocument(xmlFile);
//		String rootName = document.getDocumentElement().getLocalName();
//		LoggerManager.LOG.info("rootName : " + rootName);
//		if (rootName != null && rootName.toUpperCase().startsWith("FORM")) {
//			JAXBContext jaxbContext = JAXBContext.newInstance(pckg);
//			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//			Object root = unmarshaller.unmarshal(document);
//			JAXBElement<Object> obj = null;
//			Object jaxbModel = null;
//			if (root instanceof JAXBElement) {
//				obj = (JAXBElement) root;
//				jaxbModel = obj.getValue();
//			} else {
//				jaxbModel = root;
//			}
//
//			return jaxbModel;
//		} else {
//			return null;
//		}
//	}

//	public static void generateXml(Form form, Boolean isSubmit, File verifiedFile)
//			throws IOException, JAXBException, SAXException {
//		if (verifiedFile != null) {
//			CustomValidationHandler validationHandler = new CustomValidationHandler();
//
//			try {
//				Object entity = form.getEntity();
//				JAXBContext jaxbContext = JAXBContext.newInstance(entity.getClass());
//				Marshaller marshaller = jaxbContext.createMarshaller();
//				SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//				Schema schema = schemaFactory
//						.newSchema(Form15XmlUtil.class.getResource(Configs.getConfigProperty("xsd.file.path")));
//				marshaller.setEventHandler(validationHandler);
//				marshaller.setSchema(schema);
//				marshaller.setProperty("jaxb.formatted.output", true);
//				if (verifiedFile.getName().endsWith(".zip")) {
//					ZipOutputStream zos = new ZipOutputStream(
//							new BufferedOutputStream(new FileOutputStream(verifiedFile)));
//					ZipEntry ze = new ZipEntry(verifiedFile.getName().replaceFirst(".zip", ".xml"));
//					zos.putNextEntry(ze);
//					marshaller.marshal(entity, zos);
//					zos.closeEntry();
//					zos.close();
//				} else {
//					marshaller.marshal(entity, verifiedFile);
//				}
//
//				Form.getForm().setChanged(false);
//
//				RefHolders.outputFileSave = verifiedFile;
//			} catch (Exception var11) {
//				LoggerManager.LOG.info("validationHandler " + validationHandler.getErrorMessage());
//				LoggerManager.LOG.log(Level.SEVERE, var11.getLocalizedMessage(), var11);
////				MessageDialogCtrl.displayErrorDialog(validationHandler.getErrorMessage());
//				throw new FormGenerationException(var11, "Error while verifying the Form 15 xml");
//			}
//		}

//	}

	// ---------------------- Custom methods ---------------------------

	public static File readFromZipFile(File tempDirectory, File zipFile) {
		System.out.println("Exctracting from zip file : " + zipFile.getAbsolutePath());
		try (FileInputStream fis = new FileInputStream(zipFile); ZipInputStream zis = new ZipInputStream(fis);) {
			ZipEntry ze = zis.getNextEntry();
			byte[] buffer = new byte[1024];
			if (ze != null) {
				String fileName = ze.getName();
				File verifiedFile = new File(tempDirectory + File.separator + fileName);
				FileOutputStream fos = new FileOutputStream(verifiedFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zis.closeEntry();
				return verifiedFile;
			} else {
				throw new FormGenerationException("Error while verifying the Form 15CB xml : zip entry not available");
			}
		} catch (Exception e) {
			throw new FormGenerationException(e, "Error while verifying the Form 15CB xml");
		}
	}

	public static File createFormsDirectory() throws IOException {
		File myTempDir = com.google.common.io.Files.createTempDir();
		File parent = myTempDir.getParentFile();
		System.out.println("System Temp directory -->" + parent);
		Path folder = Paths.get(parent.getAbsolutePath() + File.separator + DIVIDEND_FORMS_DIRECTORY);
		File temp = folder.toFile();
		if (temp.exists()) {
			System.out.println("Dividend Temp directory already exists");
		} else {
			Path path = Files.createDirectory(folder);
			FileUtils.deleteDirectory(myTempDir);
			System.out.println("Created Dividend Temp directory --> " + path.getFileName());
		}
		return temp;
	}

	public static boolean cleanFormsDirectory() throws IOException {
		if (TEMP_DIRECTORY.exists()) {
			System.out.println("Dividend Temp directory exists, so cleaning it");
			FileUtils.cleanDirectory(TEMP_DIRECTORY);
			return true;
		} else {
			System.out.println("Dividend Temp directory does not exists");
			return false;
		}
	}

	public static boolean deleteDirectory(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not delete directory -->" + directory.getAbsolutePath());
			return false;
		}
	}

	public static boolean deleteFile(File file) {
		try {
			FileUtils.forceDelete(file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not delete file -->" + file);
			return false;
		}
	}

	public static void deleteFiles(List<File> files) {
		for (File file : files) {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not delete file -->" + file);
			}
		}
	}

	public static File zip(final List<File> sourceFiles, final String targetZipName) throws IOException {
		File targetZip = new File(TEMP_DIRECTORY + File.separator + targetZipName + ".zip");
		try (FileOutputStream fos = new FileOutputStream(targetZip);
				ZipOutputStream zipOut = new ZipOutputStream(fos);) {
			for (File srcFile : sourceFiles) {
				try (FileInputStream fis = new FileInputStream(srcFile);) {
					ZipEntry zipEntry = new ZipEntry(srcFile.getName());
					zipOut.putNextEntry(zipEntry);
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
			}
		}
		return targetZip;
	}

	private static File TEMP_DIRECTORY;

	private static final String DIVIDEND_FORMS_DIRECTORY = "dividend";

	public static void setTempDirectory() {
		try {
			TEMP_DIRECTORY = createFormsDirectory();
		} catch (IOException e) {
			e.printStackTrace();
			throw new FormGenerationException(e, "Could not find/create temp directory for dividend forms generation");
		}
	}

	static {
		setTempDirectory();
	}

	public static File formsDirectory() {
		return TEMP_DIRECTORY;
	}

	private static SecureRandom secureRandom = new SecureRandom();

	public static String random() {
		return "" + Math.abs(secureRandom.nextInt());
	}

	public static String randomFileName() {
		return "" + Math.abs(secureRandom.nextInt()) + ".xml";
	}

	public static String randomPrefixedFileName(String prefix) {
		return prefix + "_" + Math.abs(secureRandom.nextInt()) + ".xml";
	}

	public static String randomSuffixedFileName(String suffix) {
		return Math.abs(secureRandom.nextInt()) + "_" + suffix + ".xml";
	}

	public static String randomFileName(String prefix, String suffix) {
		return prefix + "_" + +Math.abs(secureRandom.nextInt()) + "_" + suffix + ".xml";
	}

	public static String FORM_15_CA_PART_A_FILE_PREFIX = "15_CA_PA";
	public static String FORM_15_CA_PART_B_FILE_PREFIX = "15_CA_PB";
	public static String FORM_15_CA_PART_C_FILE_PREFIX = "15_CA_PC";
	public static String FORM_15_CB_FILE_PREFIX = "15_CB";
	public static String FORM_15_G_FILE_PREFIX = "15_G";
	public static String FORM_15_H_FILE_PREFIX = "15_H";

	public static void main(String[] args) {
		try {
			File tempDirectory = createFormsDirectory();
			deleteDirectory(tempDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File generateErrorFile(List<Errors> errors, Form15FileType fileType) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);
		worksheet.setName("Error File");
		String[] mainHeaderNames = new String[]{"Error Id",
				"Error Message", "Error Type", "Complete Error Message"};
		worksheet.getCells().importArray(mainHeaderNames, 0, 0, false);
		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!errors.isEmpty()) {
			int rowIndex = 1;
			for (Errors error : errors) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(error.getId()) ? StringUtils.EMPTY
						: error.getId());
				rowData.add(StringUtils.isBlank(error.getMessage()) ? StringUtils.EMPTY
						: error.getMessage());
				rowData.add(
						StringUtils.isBlank(error.getType().getErrorType()) ? StringUtils.EMPTY : error.getType().getErrorType());

				rowData.add(
						StringUtils.isBlank(error.getCompleteMessage()) ? StringUtils.EMPTY : error.getCompleteMessage());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		}

		Cell a1 = worksheet.getCells().get("A1");
		Style style1 = a1.getStyle();
		style1.setForegroundColor(Color.fromArgb(157, 195, 230));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		a1.setStyle(style1);

		Cell a2 = worksheet.getCells().get("B1");
		Style style2 = a2.getStyle();
		style2.setForegroundColor(Color.fromArgb(157, 195, 230));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		a2.setStyle(style2);

		Cell a3 = worksheet.getCells().get("C1");
		Style style3 = a3.getStyle();
		style3.setForegroundColor(Color.fromArgb(157, 195, 230));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		a3.setStyle(style3);

		Cell a4 = worksheet.getCells().get("D1");
		Style style4 = a4.getStyle();
		style4.setForegroundColor(Color.fromArgb(157, 195, 230));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		style4.setHorizontalAlignment(TextAlignmentType.CENTER);
		a4.setStyle(style4);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(true);

		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.XLSX);
		File errorFile = new File(
				FilenameUtils.getBaseName("Form 15") + fileType.toString() + "_" + new Date().getTime() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
		baout.close();
		return errorFile;
	}
}