package com.ey.in.tds.feign.client;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ey.in.tcs.common.domain.TCSLedger;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.dto.dividend.InvoiceShareholderNonResidentDTO;
import com.ey.in.tds.common.ingestion.response.dto.InvoiceLineItemResponseDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceLineItemDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.returns.bot.domain.Form15CBDetails;

@FeignClient(url = "${feign.ingestion.app.url}", name = "ingestion")
public interface IngestionClient {

	@GetMapping(value = "/invoice/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>> getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable(value = "assessmentYear") int assessmentYear,
			@PathVariable(value = "challanMonth") int challanMonth,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@PathVariable(value = "challanPaid") boolean challanPaid);

	@GetMapping(value = "/invoice/nonResident/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>> getNonResidentInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable(value = "assessmentYear") int assessmentYear,
			@PathVariable(value = "challanMonth") int challanMonth,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@PathVariable(value = "challanPaid") boolean challanPaid);

	@PostMapping("/create")
	public InvoiceLineItem createInvoiceLineItem(@RequestBody InvoiceLineItem invoiceLineItem);
	
	@GetMapping(value = "invoice/yearTanBsrCode")
	public ResponseEntity<ApiStatus<List<InvoiceLineItem>>> invoiceByTanYearBSRCodeSerialNoAndDate(
			@RequestParam("assessmentYear") Integer year, @RequestParam("TAN") String deductorTan,
			@RequestParam("ChallanPaid") Boolean challanPaid,
			@RequestParam("ResidentIndicator") boolean isForNonResidents, @RequestParam("BSRCode") String bsrCode,
			@RequestParam("RecieptSerialNo") String receiptSerailNo, @RequestParam("RecieptDate") String receiptDate,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("isForm16") Boolean isForm16);
	
	@GetMapping(value = "invpoice/year/Tan/challanMonths")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemResponseDTO>>> getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(
			@RequestParam("assessmentYear")int assessmentYear,@RequestParam("challanMonths")int[] challanMonths,
			@RequestParam("deductorTan")List<String> deductorTan,@RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents")boolean isForNonResidents);
	
	@GetMapping(value = "advance/serialNo/BSRCode/recieptDate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<AdvanceDTO>>> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(
			@RequestParam("year")Integer assessmentYear,@RequestParam("deductorTan")String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,@RequestParam("isForNonResidents") boolean isForNonResidents,
		    @RequestParam("bsrCode")String bsrCode,@RequestParam("receiptSerailNo") String receiptSerailNo,
			@RequestParam("receiptDate")String receiptDate, @RequestHeader(value = "X-TENANT-ID") String tenantId);
	
	@GetMapping(value = "provision/year/tan/BSRCode")
	public ResponseEntity<ApiStatus<List<ProvisionDTO>>> getProvisionLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("deductorTan") String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents") boolean isForNonResidents, @RequestParam("bsrCode") String bsrCode,
			@RequestParam("receiptSerailNo") String receiptSerailNo, @RequestParam("receiptDate") String receiptDate,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);
	
	@GetMapping(value = "tcs/invoice/yearTanBsrCode")
	public ResponseEntity<ApiStatus<List<TCSLedger>>> invoiceByTanYearBSRCodeSerialNoAndDateTCS(
			@RequestParam("assessmentYear") Integer year, @RequestParam("TAN") String deductorTan,
			@RequestParam("ChallanPaid") Boolean challanPaid,
			@RequestParam("ResidentIndicator") boolean isForNonResidents, @RequestParam("BSRCode") String bsrCode,
			@RequestParam("RecieptSerialNo") String receiptSerailNo, @RequestParam("RecieptDate") String receiptDate,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("months") List<Integer> months,
			@RequestParam(value = "section", required = false) String section);
	
	@GetMapping(value = "tcs/payment/serialNo/BSRCode/recieptDate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TcsPaymentDTO>>> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDateTCS(
			@RequestParam("year")Integer assessmentYear,@RequestParam("deductorTan")String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,@RequestParam("isForNonResidents") boolean isForNonResidents,
		    @RequestParam("bsrCode")String bsrCode,@RequestParam("receiptSerailNo") String receiptSerailNo,
			@RequestParam("receiptDate")String receiptDate,@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("months") List<Integer> months,
			@RequestParam(value = "section", required = false) String section);
	
	@PostMapping(value = "/invoices-shareholder/nonresident")
    public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> getNonResidentShareholderFor15CB(
            @RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader("X-TENANT-ID") String tenantId,
            @RequestParam String dateOfPosting, @RequestParam("year") Integer year);

    @PostMapping(value = "/invoices-shareholder/resident", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiStatus<List<InvoiceShareholderResident>>> getResidentShareholderFor15GH(
            @RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId, @RequestParam("quarter") String quarter,
            @RequestParam("assessmentYear") int assessmentYear);

    @GetMapping(value = "/invoices-shareholder/nonresident-cb/{assessmentYear}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> getNonResidentShareholderForCBGenerated(
            @RequestHeader("X-TENANT-ID") String tenantId, @PathVariable(value = "assessmentYear") int financialYear);

    @PutMapping("/invoices-shareholder/nonresident-cb")
    public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> updateNonResidentShareholder(@RequestHeader("X-TENANT-ID") String tenantId,
                                                                                                    @RequestBody List<Form15CBDetails> form15CBDetails);
    @PutMapping("/invoices-shareholder/nonresident-cb/{id}")
    public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> updateNonResidentShareholder(@RequestHeader("X-TENANT-ID") String tenantId,@RequestHeader("DEDUCTOR-PAN") String deductorPan,
                                                                                                       @PathVariable Integer id);
    
	@GetMapping(value = "/invoice-shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceShareholderNonResidentDTO>> getNonResidentialShareholder(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable Integer id,
			@RequestHeader(value = "X-TENANT-ID") String tenantId);
	
	@PostMapping(value = "/invoices/shareholder/resident")
	public ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>> getResidentShareholders(
			@RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody Map<String, String> receiptObj);
	
	@PostMapping(value = "/invoices/shareholder/nonresident")
	public ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>> getNonResidentShareholders(
			@RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody Map<String, String> receiptObj);
    
    

}
