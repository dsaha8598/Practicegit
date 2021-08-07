package com.ey.in.tds.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.DeductorTanDetails;
import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.common.domain.FilingMinistryCode;
import com.ey.in.tds.common.domain.FilingMinorHeadCode;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.challan.calculation.NopCessRateSurageRateDTO;
import com.ey.in.tds.common.dto.challan.fineratemaster.FineRateMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.core.util.ApiStatus;

@FeignClient(url = "${feign.masters.app.url}", name = "masters")
public interface MastersClient {

	// Deductor Record based on Deductor Id
	@GetMapping(value = "/deductor-user/{pan}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorMasterRecord(
			@PathVariable(value = "pan") String pan);

	// Deductor TAN Details based on Deductor Id
	@GetMapping(value = "/get-deductor-tan/{id}")
	public ResponseEntity<ApiStatus<List<DeductorTanDetails>>> getDeductorTanDetailsBasedOnDeductorId(
			@PathVariable(value = "id") Long id);

	// Nature Of Payment based on Section
	@GetMapping(value = "/nature-of-payment-section/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfPaymentMasterRecord(
			@PathVariable(value = "id") Long id);

	// Nature Of Payment based on Section
	@GetMapping(value = "/tcs/nature-of-income-section/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NatureOfPaymentMasterDTO>> getNatureOfIncomeMasterRecord(
			@PathVariable(value = "id") Long id);
	// BasisOfSurcharge

	@GetMapping(value = "/nature-of-payment-section/section/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<NopCessRateSurageRateDTO>> getNatureOfPaymentMasterRecordBasedonSection(
			@PathVariable(value = "section") String section);

	// BasisOfSurcharge get Surcharge Rate based on nature of Payment Id
	@GetMapping("/basis-of-surcharge-nature-of-payment/{id}")
	public ResponseEntity<ApiStatus<BasisOfSurchargeDetails>> getNatureOfPaymentBasedOnNatureOfPaymentId(
			@PathVariable(value = "id") Long id);

	// BasisOfCess get Cess Rate based on nature of Payment Id
	@GetMapping(value = "/basis-of-cess-nature-of-payment/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<BasisOfCessDetails>> getBasisOfCessNatureOfPaymentBasedonNatureOfPaymentId(
			@PathVariable(value = "id") Long id);

	// Late Filing from Fine Rate Master Table
	@GetMapping(value = "/fine-rate-section/{filing}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FineRateMasterDTO>> getFineRateMasterBasedonLateFiling(
			@PathVariable(value = "filing") String filing);

	@GetMapping(path = "/deductor-data/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public DeductorMasterDTO getDeductorMasterData(@RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable(value = "id") Long id);

	@GetMapping(value = "/filing/deductorcollector")
	public ResponseEntity<ApiStatus<List<FilingDeductorCollector>>> getAlldeductorCollector(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId);

	@GetMapping(value = "/filing/ministrycode")
	public ResponseEntity<ApiStatus<List<FilingMinistryCode>>> getAllMinistryCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId);

	@GetMapping(value = "/filing/minorheadcode")
	public ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>> getAllMinorHeadCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId);

	@GetMapping(value = "/filing/sectioncode")
	public ResponseEntity<ApiStatus<List<FilingSectionCode>>> getAllSectionCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId);

	@GetMapping(value = "/filing/statecode")
	public ResponseEntity<ApiStatus<List<FilingStateCode>>> getAllStateCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId);

	@GetMapping(value = "/filling/deductorCollector/{catagoryDescription}")
	public ResponseEntity<ApiStatus<String>> getCatagoryValue(@PathVariable String catagoryDescription);

	@GetMapping(value = "/filling/{categoryValue}")
	public ResponseEntity<ApiStatus<String>> getCatagoryDescription(@PathVariable String categoryValue);

	@PostMapping("/getTreatyByCountry")
	public ResponseEntity<ApiStatus<DividendRateTreaty>> getTreatyBenefit(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId, @RequestBody Country country);

	@GetMapping("/getcountries")
	public ResponseEntity<ApiStatus<List<Country>>> getCountries();

	@GetMapping("/shareholderTypes")
	public ResponseEntity<ApiStatus<List<ShareholderType>>> getShareholderTypes();

	@GetMapping(value = "/tcs/nature-of-income", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<NatureOfPaymentMasterDTO>>> tcsFindAll();
}
