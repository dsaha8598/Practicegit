package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.dividend.forms.builder.gh.Quarter;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.Form15FilingDetailsDAO;
import com.ey.in.tds.returns.dto.Form15HDTO;
import com.microsoft.azure.storage.StorageException;
import com.ey.in.tds.returns.dividend.validator.Errors;


@Service
public class Filing15HService {

    @Autowired
    private IngestionClient ingestionClient;

    @Autowired
    private OnboardingClient onboardingClient;

    @Autowired
    private MastersClient mastersClient;

    @Autowired
    protected BlobStorage blobStorage;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Form15FilingDetailsDAO form15FilingDetailsDAO;

    @Autowired
    private PlatformTransactionManager transactionManager;
    
    @Autowired
    private DividendFilingCommonErrorFileService dividendFilingCommonErrorFileService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public String generate15HExcelFilingReport(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
                                               String userName, Integer assessmentYear) throws Exception {
        logger.info("Filing 15H Excel: Started generation for TAN: {}, quarter : {} ", deductorTan, quarter);

        form15FilingDetailsDAO.createFilingReportStatus(deductorTan, assessmentYear,
                quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.EXCEL, ReturnType.REGULAR,
                userName);

        CompletableFuture.runAsync(() -> this.create15HExcelFilingReport(deductorTan, deductorPan, quarter, tenantId,
                userName, assessmentYear));

        return "Request for generating Form 15H Excel submitted successfully";
    }

    private void create15HExcelFilingReport(String deductorTan, String deductorPan, Quarter quarter, String tenantId,
                                            String userName, Integer assessmentYear) {
        MultiTenantContext.setTenantId(tenantId);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult((status) -> {
            try {
            	List<Errors> errors=new ArrayList<>();
                List<Form15HDTO> form15HDTOS = generate15HDTOs(deductorTan, deductorPan, quarter.name(), tenantId,
                        userName, assessmentYear,errors);
                
                if(!form15HDTOS.isEmpty()) {
                generateExcelFileFor15GH(form15HDTOS, tenantId, quarter, assessmentYear, deductorTan, userName,errors);
                }else {
                	File errorFile=dividendFilingCommonErrorFileService.generateErrorFile(errors);
					String url=blobStorage.uploadExcelToBlobWithFile(errorFile, tenantId);
					form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
	                        quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.EXCEL,
	                        ReturnType.REGULAR, Form15FilingStatus.ERROR, url, "", userName);
                }
                
            } catch (Exception e) {
                logger.error("Error occured while Generating Form 15H excel", e);
                form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assessmentYear,
                        quarter.startingDate(assessmentYear), Form15FileType.H, Form15FileFormat.EXCEL,
                        ReturnType.REGULAR, Form15FilingStatus.ERROR, null, e.getMessage(), userName);
            }
        });
    }

    public List<Form15HDTO> generate15HDTOs(String tanNumber, String deductorPan, String quarter, String tenantId,
                                            String userName, Integer assessmentYear,List<Errors> errors) {

        List<Form15HDTO> form15HDTOS = new ArrayList<>();

      /*  ResponseEntity<ApiStatus<List<InvoiceShareholderResident>>> residentShareholderFor15GH = ingestionClient
                .getResidentShareholderFor15GH(tanNumber, tenantId, quarter, assessmentYear);
        List<InvoiceShareholderResident> residents = Objects.requireNonNull(residentShareholderFor15GH.getBody())
                .getData();*/
        Integer[] months=getMonthUsingQuarter(quarter);
		List<InvoiceShareholderResident> residents=form15FilingDetailsDAO.findAllResidentByQuarter(tanNumber,assessmentYear, months[0], months[1], months[2]);
		if(residents.isEmpty()) {
			Errors error=new Errors();
			error.setMessage("No records found for Quarter "+quarter+" with deducter Tan "+tanNumber);
			errors.add(error);
		}

        for (InvoiceShareholderResident residentShareholder : residents) {
        	String errorMsg=generateErrorMessages(residentShareholder);
        	if(StringUtils.isNotBlank(errorMsg)) {
        		Errors error=new Errors();
            	error.setId(residentShareholder.getFolioNumber());
            	error.setMessage(errorMsg);
            	error.setCompleteMessage(residentShareholder.getId()+"");
            	errors.add(error);
        	}
            if (checkValidRecord(residentShareholder)) {
                Form15HDTO form15HDTO = new Form15HDTO();
                form15HDTO.setFolioNo(residentShareholder.getFolioNumber());
                errorMsg=errorMsg + populateShareholderMasterDate(form15HDTO, deductorPan, tenantId,
                        residentShareholder.getShareholderId());
                populateDeductorMasterData(form15HDTO, deductorPan, tenantId);
                form15HDTO.setShareholderId(residentShareholder.getShareholderId());
                form15HDTO.setQuarter(quarter);
                form15HDTO.setFinancialYear(getFinancialYear(assessmentYear));
                if(StringUtils.isBlank(residentShareholder.getFilingType())) {
                	errorMsg=errorMsg+"Filing Type Is a Mandatory field. It should not be blank."+"\n";
                }else {
                form15HDTO.setFilingType(residentShareholder.getFilingType());
                }
                	form15HDTO.setAcknowledgementNumber(residentShareholder.getForm15ghAcknowledgementNo());
                form15HDTO.setDeductorTan(residentShareholder.getDeductorTan());
                form15HDTO.setIncomePaid(residentShareholder.getDividendAmountRs());
                form15HDTO.setDeclarationDate(residentShareholder.getForm15ghReceivedDate());
                form15HDTO.setIncomePaidDate(residentShareholder.getDateOfDistribution());
                form15HDTO.setAssesseeName(residentShareholder.getShareholderName());
                form15HDTO.setAssesseePan(residentShareholder.getShareholderPan());
                form15HDTO.setDateOfBirth(residentShareholder.getDateOfBirth());
                form15HDTO.setPreviousYearOfDeclaration(assessmentYear - 1);
                if(StringUtils.isNotBlank(residentShareholder.getEmailId()) && !residentShareholder.getEmailId().equals("None")) {
                form15HDTO.setEmail(residentShareholder.getEmailId());
                }
                form15HDTO.setAssessedToTax(residentShareholder.getTaxUnderITAct1961());
                if(residentShareholder.getTaxUnderITAct1961()==true && residentShareholder.getAssesmentYearFor15Gh()==null) {
                	errorMsg = errorMsg + "LATEST ASSESSMENT YEAR FOR WHICH ASSESSED Is a Mandatory field. It should not be blank." + "\n";
				}else {
					form15HDTO.setAssessedYear(residentShareholder.getAssesmentYearFor15Gh());
				}
				if(residentShareholder.getIncomeFor15ghDeclaration()!=null) {
					form15HDTO.setDeclaredIncome(residentShareholder.getIncomeFor15ghDeclaration());
				}else {
					errorMsg = errorMsg + "ESTIMATED INCOME  FOR WHICH  THIS DECLARATION IS MADE Is a Mandatory field. It should not be blank." + "\n";
				}
				if(residentShareholder.getTotalIncomePYFor15ghDeclaration()!=null) {
					form15HDTO.setTotalIncomeWhereDeclaredIncomeIncluded(
						residentShareholder.getTotalIncomePYFor15ghDeclaration());
				}else {
					errorMsg = errorMsg + "ESTIMATED TOTAL INCOME OF THE P.Y.  IN WHICH ESTIMATED INCOME FOR WHICH  THIS DECLARATION IS MADE TO BE INCLUDED." + "\n";
				}
                form15HDTO.setIdentificationNumber(residentShareholder.getShareholderId());
                form15HDTO.setNatureOfIncome("Dividend Income");
                form15HDTO.setAmountOfIncome(residentShareholder.getDividendAmountRs());
                form15HDTO.setRecordType(StringUtils.isBlank(residentShareholder.getRecordType())?"":residentShareholder.getRecordType().toUpperCase());
                
                if(StringUtils.isBlank(errorMsg)) {
                form15HDTOS.add(form15HDTO);
                }else {
                	Stream.of(errorMsg.split("\n")).forEach(n->{
						Errors error = new Errors();
						error.setCompleteMessage(residentShareholder.getId()+"");
						error.setId(residentShareholder.getFolioNumber());
						error.setMessage(n);
						errors.add(error);
					});
                }
            }

        }
        return form15HDTOS;
    }

    private void populateDeductorMasterData(Form15HDTO form15HDTO, String deductorPan, String tenantId) {
        ResponseEntity<ApiStatus<DeductorMasterDTO>> response = onboardingClient.getDeductorByPan(tenantId,
                deductorPan);
        DeductorMasterDTO deductorMasterDTO = Objects.requireNonNull(response.getBody()).getData();
        if (deductorMasterDTO != null) {
            if (deductorMasterDTO.getDvndDeductorTypeName() == null) {
                form15HDTO.setSectionOfAct("194");
            } else {
                if (deductorMasterDTO.getDvndDeductorTypeName().equalsIgnoreCase("Mutual Fund")) {
                    form15HDTO.setSectionOfAct("194K");
                } else {
                    form15HDTO.setSectionOfAct("194");
                }
            }
        } else {
            throw new CustomException("deductor not found for given pan number", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean checkValidRecord(InvoiceShareholderResident residentShareholder) {
        return (!Objects.isNull(residentShareholder.getForm15ghApplicable())
                && residentShareholder.getForm15ghApplicable())
                && (Objects.isNull(residentShareholder.getForm15ghUniqueIdentificationNo())
                || residentShareholder.getForm15ghUniqueIdentificationNo().isEmpty())
                && (!Objects.isNull(residentShareholder.getShareholderType()) && checkValidShareholderType(
                residentShareholder.getShareholderType(), residentShareholder.getDateOfBirth()));
    }

    private boolean checkValidShareholderType(String shareholderType, Date dateOfBirth) {
        ResponseEntity<ApiStatus<List<ShareholderType>>> shareholderTypes = mastersClient.getShareholderTypes();
        List<ShareholderType> data = Objects.requireNonNull(shareholderTypes.getBody()).getData();

        if (data.stream().anyMatch(x -> x.getName().equalsIgnoreCase(shareholderType))) {
            return (shareholderType.equalsIgnoreCase("Person (individual)")) && (checkSeniorCitizen(dateOfBirth));
        } else {
            return false;
        }
    }

    private boolean checkSeniorCitizen(Date dateOfBirth) {
        if(!Objects.isNull(dateOfBirth)) {
            LocalDate localDate = LocalDate.now();
            LocalDate birth = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Period p = Period.between(birth, localDate);
            return p.getYears() > 60;
        }
        return false;
    }

    private String getFinancialYear(Integer assessmentYear) {
        int previousYear = assessmentYear - 1;
        return "" + previousYear + "-" + assessmentYear;
    }

    private String populateShareholderMasterDate(Form15HDTO form15HDTO, String deductorPan, String tenantId, Integer id) {
        ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = null;
        String errorMsg="";
        if (!Objects.isNull(id)) {
            shareholderNonResident = onboardingClient.getResidentialShareholder(tenantId, deductorPan, id);
            ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();
            logger.info("Populating shareholder master data for folio no", data.getFolioNo());
            AddressDTO address = data.getAddress();
            if(!Objects.isNull(address)) {
            	if(StringUtils.isNotBlank(data.getAddress().getFlatDoorBlockNo()) && !data.getAddress().getFlatDoorBlockNo().equalsIgnoreCase("none")) {
            		form15HDTO.setFlatDoorBlockNo(data.getAddress().getFlatDoorBlockNo());
    				}else {
    					errorMsg = errorMsg+"Flat/Door/Block No. is Mandatory. It can not be blank "+"\n";	
    				}
            	if(StringUtils.isNotBlank(data.getAddress().getAreaLocality()) && !data.getAddress().getAreaLocality().equalsIgnoreCase("none")) {
            		form15HDTO.setAreaLocality(data.getAddress().getAreaLocality());
					}else {
						errorMsg = errorMsg+"Area/ Locality is Mandatory. It can not be blank "+"\n";	
					}
				if(StringUtils.isNotBlank(data.getAddress().getTownCityDistrict()) && !data.getAddress().getTownCityDistrict().equalsIgnoreCase("none")) {
					form15HDTO.setTownCityDistrict(data.getAddress().getTownCityDistrict());
					}else {
						errorMsg = errorMsg+"Town/City/ District is Mandatory. It can not be blank "+"\n";	
					}
				if(StringUtils.isNotBlank(data.getAddress().getStateName()) && !data.getAddress().getStateName().equalsIgnoreCase("none")) {
					form15HDTO.setState(data.getAddress().getStateName());
					}else {
						errorMsg = errorMsg+"State is Mandatory. It can not be blank "+"\n";	
					}
				if(StringUtils.isNotBlank(data.getAddress().getStateName()) && !data.getAddress().getStateName().equalsIgnoreCase("none")) {
					form15HDTO.setPinCode(data.getAddress().getPinCode());
					}else {
						errorMsg = errorMsg+"PIN is Mandatory. It can not be blank "+"\n";	
					}
                form15HDTO.setNameBuildingVillage(data.getAddress().getNameBuildingVillage());
                form15HDTO.setRoadStreetPostoffice(data.getAddress().getRoadStreetPostoffice());
            }else {
            	errorMsg=errorMsg+"Address is Mandatory. It can not be blank "+"\n";
            }
//            form15HDTO.setTelephoneNumber(data.getContact());
            if(StringUtils.isNotBlank(data.getContact()) && !data.getContact().equalsIgnoreCase("none") ){
            form15HDTO.setMobileNumber(data.getContact());
            }else {
            	errorMsg=errorMsg+"Contact Number is Mandatory. It can not be blank "+"\n";
            }
        }
        return errorMsg;
    }

    private void generateExcelFileFor15GH(List<Form15HDTO> form15HDTOS, String tenantId, Quarter quarter,
                                          Integer assesmentYear, String deductorTan, String userName,List<Errors> errors) {

        Resource resource = resourceLoader.getResource("classpath:templates/" + "Form15H.xlsx");

        try (InputStream input = resource.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            XSSFSheet worksheet = workbook.getSheetAt(0);

            for (Form15HDTO record : form15HDTOS) {
                int totalRows = worksheet.getPhysicalNumberOfRows();
                XSSFRow row = worksheet.createRow(totalRows);
                accept15HData(record, row,assesmentYear);
                totalRows++;
            }
            logger.debug("File Writing End");

            File file = new File("Form15H" + new Date().getTime() + ".xlsx");
            OutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            
            File zipFile = new File("FORM 15 H"+System.currentTimeMillis()+".zip");
            FileOutputStream outputStream = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            zos.putNextEntry(new ZipEntry(file.getName()));
			byte[] bytes1 = Files.readAllBytes(file.toPath());
			zos.write(bytes1, 0, bytes1.length);
			zos.closeEntry();
			
			//generating error file
			if(!errors.isEmpty()) {
			File errorFile=dividendFilingCommonErrorFileService.generateErrorFile(errors);
			zos.putNextEntry(new ZipEntry(errorFile.getName()));
			byte[] bytes2 = Files.readAllBytes(errorFile.toPath());
			zos.write(bytes2, 0, bytes2.length);
			zos.closeEntry();
			}
			
			zos.close();
			outputStream.close();

            String uri = null;
            try {
                uri = blobStorage.uploadExcelToBlobWithFile(zipFile, tenantId);
            } catch (IOException | URISyntaxException | InvalidKeyException | StorageException e1) {
                logger.error("Error occured while uploading excel", e1);
                throw new RuntimeException(e1);
            }

            form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assesmentYear,
                    quarter.startingDate(assesmentYear), Form15FileType.H, Form15FileFormat.EXCEL, ReturnType.REGULAR,
                    Form15FilingStatus.GENERATED, uri, "", userName);
        } catch (Exception e) {
            form15FilingDetailsDAO.updateFilingReportStatus(deductorTan, assesmentYear,
                    quarter.startingDate(assesmentYear), Form15FileType.H, Form15FileFormat.EXCEL, ReturnType.REGULAR,
                    Form15FilingStatus.ERROR, null, e.getMessage(), userName);
        }
    }

    private void accept15HData(Form15HDTO record, XSSFRow row,Integer assesmentYear) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		int nextYear = (assesmentYear + 1) % 100;
		String uniqueIdentificationNo = "H" + RandomStringUtils.randomNumeric(9) + (assesmentYear-1) + (nextYear-1)
				+ record.getDeductorTan();

        row.createCell(0).setCellValue(uniqueIdentificationNo);
        row.createCell(1).setCellValue(StringUtils.isBlank(record.getQuarter()) ? " " : record.getQuarter());
        row.createCell(2)
                .setCellValue(StringUtils.isBlank(record.getFinancialYear()) ? " " : record.getFinancialYear());
        row.createCell(3).setCellValue(StringUtils.isBlank(record.getFilingType()) ? " " : record.getFilingType());
        row.createCell(4).setCellValue(
                StringUtils.isBlank(record.getAcknowledgementNumber()) ? " " : record.getAcknowledgementNumber());
        row.createCell(5).setCellValue(StringUtils.isBlank(record.getDeductorTan()) ? " " : record.getDeductorTan());
        row.createCell(6).setCellValue(checkDecimal(record.getAmountOfIncome()));
        row.createCell(7).setCellValue(
                Objects.isNull(record.getDeclarationDate()) ? " " : formatter.format(record.getDeclarationDate()));
        row.createCell(8).setCellValue(
                Objects.isNull(record.getIncomePaidDate()) ? " " : formatter.format(record.getIncomePaidDate()));
        row.createCell(9).setCellValue(StringUtils.isBlank(record.getAssesseeName()) ? " " : record.getAssesseeName());
        row.createCell(10).setCellValue(StringUtils.isBlank(record.getAssesseePan()) ? " " : record.getAssesseePan());
        row.createCell(11).setCellValue(
                Objects.isNull(record.getDateOfBirth()) ? " " : formatter.format(record.getDateOfBirth()));
        row.createCell(12).setCellValue(checkInteger(record.getPreviousYearOfDeclaration()));
        row.createCell(13)
                .setCellValue(StringUtils.isBlank(record.getFlatDoorBlockNo()) ? " " : record.getFlatDoorBlockNo());
        row.createCell(14).setCellValue(
                StringUtils.isBlank(record.getRoadStreetPostoffice()) ? " " : record.getRoadStreetPostoffice());
        row.createCell(15).setCellValue(
                StringUtils.isBlank(record.getNameBuildingVillage()) ? " " : record.getNameBuildingVillage());
        row.createCell(16).setCellValue(StringUtils.isBlank(record.getAreaLocality()) ? " " : record.getAreaLocality());
        row.createCell(17)
                .setCellValue(StringUtils.isBlank(record.getTownCityDistrict()) ? " " : record.getTownCityDistrict());
        row.createCell(18).setCellValue(StringUtils.isBlank(record.getState()) ? " " : record.getState());
        row.createCell(19).setCellValue(StringUtils.isBlank(record.getPinCode()) ? " " : record.getPinCode());
        row.createCell(20).setCellValue(StringUtils.isBlank(record.getEmail()) ? " " : record.getEmail());
        row.createCell(21)
                .setCellValue(StringUtils.isBlank(record.getTelephoneNumber()) ? " " : record.getTelephoneNumber());
        row.createCell(22).setCellValue(StringUtils.isBlank(record.getMobileNumber()) ? " " : record.getMobileNumber());
        row.createCell(23).setCellValue(checkBoolean(record.isAssessedToTax()));
        row.createCell(24).setCellValue(checkInteger(record.getAssessedYear()));
        row.createCell(25).setCellValue(checkDecimal(record.getDeclaredIncome()));
        row.createCell(26).setCellValue(checkDecimal(record.getTotalIncomeWhereDeclaredIncomeIncluded()));
        row.createCell(27).setCellValue(checkDecimal(record.getNumberOf15HFiled()));
        row.createCell(28).setCellValue(checkDecimal(record.getAggregateIncomeOf15HFiled()));
        row.createCell(29).setCellValue(record.getFolioNo());
        row.createCell(30)
                .setCellValue(StringUtils.isBlank(record.getNatureOfIncome()) ? " " : record.getNatureOfIncome());
        row.createCell(31).setCellValue(StringUtils.isBlank(record.getSectionOfAct()) ? " " : record.getSectionOfAct());
        row.createCell(32).setCellValue(checkDecimal(record.getAmountOfIncome()));
        row.createCell(33).setCellValue(StringUtils.isBlank(record.getRecordType()) ? " " : record.getRecordType());
    }

    private String checkBoolean(Boolean value) {
        String str = "";
        if (Objects.isNull(value)) {
            str = "No";
        } else {
            if (value.toString().equalsIgnoreCase("True")) {
                str = "Yes";
            }
            if (value.toString().equalsIgnoreCase("False")) {
                str = "No";
            }
        }
        return str;
    }

    private String checkInteger(Integer value) {
        if (Objects.isNull(value)) {
            return " ";
        }
        return value.toString();
    }

    private String checkDecimal(BigDecimal amountOfIncome) {
        String value = " ";
        if (Objects.isNull(amountOfIncome)) {
            return " ";
        } else {
            value = amountOfIncome.toString();
        }
        return value;
    }
    
    private Integer[] getMonthUsingQuarter(String quarter) {
		if (quarter.equalsIgnoreCase("Q1")) {
			return new Integer[] {4,5,6};
		} else if (quarter.equalsIgnoreCase("Q2")) {
			return new Integer[] {7, 8, 9};

		} else if (quarter.equalsIgnoreCase("Q3")) {
			return new Integer[] {10,11,12};

		} else if (quarter.equalsIgnoreCase("Q4")) {
			return new Integer[] {1,2,3};
		} else {
			throw new CustomException("Invalid quarter");
		}
	}
    
    public String generateErrorMessages(InvoiceShareholderResident dto) {
		String msg="";
		if(StringUtils.isNotBlank(dto.getForm15ghUniqueIdentificationNo())) {
			msg=msg+"Form 15 G/H Unique Identification number is Present" +"\n";
		}
		if(StringUtils.isBlank(dto.getShareholderType())) {
			msg=msg+"ShareHolde Type a Mandatory field. It should not be blank. "+"\n";
		}else {
			if ((dto.getShareholderType().equalsIgnoreCase("Company")) || (dto.getShareholderType().equalsIgnoreCase("Firm/ LLP"))) {
				msg=msg+"ShareHolde Type is  "+dto.getShareholderType()+"\n";
			}
		}
		if(dto.getDateOfBirth()==null) {
			msg=msg+"Date Of Birth is a Mandatory field. It should not be blank. "+"\n";
		}
		/*if(StringUtils.isBlank(dto.getForm15ghAcknowledgementNo())) {
			msg=msg+"\n"+"Form 15 G/H AcknowledgeMent number Is not Present";
		}*/
		/*if(StringUtils.isBlank(dto.gass())) {
			msg=msg+"\n"+"Filing type is Absent";
		}*/
		return msg;
	}

}
