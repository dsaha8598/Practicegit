package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceMetaNr;

public class InvoiceMetaNrRowMapper implements RowMapper<InvoiceMetaNr>{
	@Override
	public InvoiceMetaNr mapRow(ResultSet rs, int rowNum) throws SQLException {
		InvoiceMetaNr dto=new InvoiceMetaNr();
			dto.setInvoiceMetaNrId(rs.getInt("invoice_meta_nr_id"));
			dto.setYear(rs.getInt("assessment_year"));
			dto.setMonth(rs.getInt("assessment_month"));
			dto.setTan(rs.getString("deductor_master_tan"));
			dto.setChallanMonth(rs.getInt("challan_month"));
			dto.setAarRulingStatusIligation(rs.getString("aar_ruling_status_iligation"));
			dto.setAddress(rs.getString("address"));
			dto.setAggrementDate(rs.getDate("aggrement_date"));
			dto.setAggrementDocNumber(rs.getString("aggrement_document_number"));
			dto.setAggrementExternalCouncilViewDoc(rs.getString("aggrement_external_council_view_doc"));
			dto.setAggrementNatureRemittance(rs.getString("aggrement_nature_of_remittance"));
			dto.setAggrementCurrencyType(rs.getString("agreement_currency_amount"));
			dto.setAgreementCurrencyAmount(rs.getDouble("agreement_currency_amount"));
			dto.setAlternativeNature(rs.getString("alternative_nature"));
			dto.setAmount(rs.getDouble("amount"));
			dto.setAmountOfIncomeTaxDeducted(rs.getDouble("amount_of_income_tax_deducted"));
			dto.setAmountPaidCreditedInr(rs.getDouble("amount_paid_credited_inr"));
			dto.setAoCertificateNo(rs.getString("ao_certificate_number"));
			dto.setAoRate(rs.getDouble("applicable_from"));
			dto.setApplicableTo(rs.getDate("applicable_to"));
			dto.setAssesseeStatus(rs.getString("assessee_status"));
			dto.setBasisArrivingAmount(rs.getDouble("basis_arriving_amount"));
			dto.setCbdtCirculars(rs.getString("cbdt_circulars"));
			dto.setCertificationDocumentFile(rs.getString("certification_document_file"));
			dto.setConclusionFactsUsedConclusion(rs.getString("conclusion_facts_used_conclusion"));
			dto.setConsultationProcessNote(rs.getString("consultation_per_process_note"));
			dto.setCreatedBy(rs.getString("created_by"));
			dto.setCreatedDate(rs.getDate("created_date"));
			dto.setDtaaLobClauseImpactRelevantTransaction(rs.getString("datt_lob_clause_impact_relevant_transaction"));
			dto.setDtaaLobClauseRate(rs.getDouble("datt_lob_clause_rate"));
			dto.setDeducteeName(rs.getString("deductee_name"));
			dto.setDeducteePan(rs.getString("deductee_pan"));   
			dto.setDeducteeTin(rs.getString("deductee_tin")); 
			dto.setDetailsDiffOpinion(rs.getString("details_of_diff_opinion"));
			dto.setDocIdKsDocsRefered(rs.getString("doc_id_ks_docs_refered"));
			dto.setDocumentNumber(rs.getString("document_number"));
			dto.setDtaaExistedCountry(rs.getString("dtaa_existed_country"));
			dto.setDtaaMfnClauseOtherCountries(rs.getString("dtaa_mfn_clause_other_countries"));
			dto.setDtaaMfnClauseRate(rs.getDouble("dtaa_mfn_clause_rate"));
			dto.setDtaaRate(rs.getDouble("dtaa_rate"));
			dto.setDtaaSection(rs.getString("dtaa_section"));
			dto.setEntitledClaimRelevantDtaaArticle(rs.getString("entitled_claim_relevant_dtaa_article"));
			dto.setEntitledClaimRelevantRate(rs.getDouble("entitled_claim_relevant_rate"));
			dto.setFormTenFNationality(rs.getString("form10f_nationality"));
			dto.setFormTenFPanNumber(rs.getString("form10f_pan_number"));
			dto.setIdKsDocsRefered(rs.getString("id_ks_docs_refered"));
			dto.setIncomeTaxBusinessConcludingReason(rs.getString("income_tax_business_concluding_reason"));
			dto.setIncomeTaxBusinessRate(rs.getDouble("income_tax_business_rate"));
			dto.setIncomeTaxDtaaReason(rs.getString("income_tax_dtaa_reason"));
			
			dto.setIncometaxIndiaSection(rs.getString("incometax_india_section"));
			dto.setInrConversionRate(rs.getDouble("inr_conversion_rate"));
			dto.setInrCreditedAmount(rs.getDouble("inr_credited_amount"));
			dto.setInvoiceDate(rs.getDate("invoice_date"));
			dto.setInvoiceNatureOfRemittance(rs.getString("invoice_nature_of_remittance"));
			dto.setInvoiceNumber(rs.getString("invoice_number"));
			dto.setInvoiceNumberFile(rs.getString("invoice_number_file"));
			dto.setInvoiceReason(rs.getString("invoice_reason"));
			dto.setInvoiceUnavailbilityReason(rs.getString("invoice_unavailbility_reason"));
			dto.setIsAarApplication(rs.getInt("is_aar_application")==1?true:false);
			dto.setIsAoOrderSection(rs.getInt("is_ao_order_section_this_transaction")==1?true:false);
			dto.setIsBenificiaryEntitledCclaimDtaa(rs.getInt("is_benificiary_entitled_claim_dtaa")==1?true:false);
			dto.setIsBenificiaryQualifyDualPerson(rs.getInt("is_benificiary_qualify_dual_person")==1?true:false);
			dto.setIsCbdtCircularsRefered(rs.getInt("is_cbdt_circulars_refered")==1?true:false);
			dto.setIsCouncilViewTaken(rs.getInt("is_council_view_taken")==1?true:false);
			dto.setIsDtaaMfnClause(rs.getInt("is_datt_mfn_clause")==1?true:false);
			dto.setIsDiffClientFirmCertification(rs.getInt("is_diff_client_firm_certification")==1?true:false);
			dto.setIsDtaaExist(rs.getInt("is_dtaa_exist")==1?true:false);
			dto.setIsDtaaLobClause(rs.getInt("is_dtaa_lob_clause")==1?true:false);
			dto.setIsExtrenalCouncilViewTaken(rs.getInt("is_extrenal_council_view_taken")==1?true:false);
			dto.setIsFormTenFAvailble(rs.getInt("is_form10f_availble")==1?true:false);
			dto.setIsForm15(rs.getInt("is_form_fifteen")==1?true:false);
			
			dto.setIsIncomeTaxDtaa(rs.getInt("is_income_tax")==1?true:false);
			dto.setIsIncomeTaxBusinessReasons(rs.getInt("is_income_tax_business_reasons")==1?true:false);
			dto.setIsIncomeTaxDtaa(rs.getInt("is_income_tax_india_as_per_dtaa")==1?true:false);
			dto.setIsIncometaxAsPerIndiaTax(rs.getInt("is_incometax_as_per_india_tax")==1?true:false);
			dto.setIsInrAmount(rs.getInt("is_inr_amount")==1?true:false);
			dto.setIsInrConversionRate(rs.getInt("is_inr_conversion_rate")==1?true:false);
			dto.setIsInvoiceNoteTransaction(rs.getInt("is_invoice_note_transaction")==1?true:false);
			dto.setIsKsConsultationTaken(rs.getInt("is_ks_consultation_taken")==1?true:false);
			dto.setIsKsConsultationTakenProcessNote(rs.getInt("is_ks_consultation_taken_process_note")==1?true:false);
			dto.setIsOpinion(rs.getInt("is_opinion")==1?true:false);
			dto.setIsOthers(rs.getInt("is_others")==1?true:false);
			dto.setIsPoaltereTakenPosition(rs.getInt("is_poaltere_taken_position")==1?true:false);
			dto.setIsPossibleTakenPosition(rs.getInt("is_possible_taken_position")==1?true:false);
			dto.setIsReleavantDoc(rs.getInt("is_releavant_doc")==1?true:false);
			dto.setIsSignificantRisksIssuesTaken(rs.getInt("is_significant_risks_issues_taken")==1?true:false);
			dto.setIsTakenProcessNote(rs.getInt("is_taken_as_per_process_note")==1?true:false);
			dto.setIstransactionAgreementDoc(rs.getInt("is_transaction_agreement_doc")==1?true:false);
			dto.setIsTransactionExaminedPast(rs.getInt("is_transaction_examined_past")==1?true:false);
			dto.setIsTrc(rs.getInt("is_trc")==1?true:false);
			dto.setIstenfFuture(rs.getInt("istenf_future")==1?true:false);
			dto.setIstrcFuture(rs.getInt("istrc_future")==1?true:false);
			dto.setLineItemId(rs.getInt("line_item_id"));
			dto.setLimitUtilized(rs.getDouble("limit_utilized"));
			dto.setLobClauseSection(rs.getString("lob_clause_section"));
			dto.setMfnClauseSection(rs.getString("mfn_clause_section"));
			dto.setNatureOfPayment(rs.getString("nature_of_payment"));
			dto.setNonTaxbilityReason(rs.getString("non_taxbility_reason"));
			dto.setOtherRelevantDetails(rs.getString("other_relevant_details"));
			dto.setPossibleAvailbleForPosition(rs.getString("possible_availble_for_position"));
			dto.setPreviousTransactionRemittance(rs.getString("previous_transaction_nature_remittance"));
			dto.setProposedDateRemittance(rs.getDate("proposed_date_of_remittance"));
			dto.setQualifyDualPersonCountry(rs.getString("qualify_dual_person_country"));
			dto.setReasons(rs.getString("reasons"));
			dto.setReferedJudicialPrecedents(rs.getString("refered_judicial_precedents"));
			dto.setRelevantCountryDtaaExist(rs.getInt("relevant_country_dtaa_exist")==1?true:false);
			dto.setReleventForm15CBDoc(rs.getString("relevent_form_15cb_doc"));
			dto.setReleventOpinionDoc(rs.getString("relevent_opinion_doc"));
			dto.setReleventOthersDoc(rs.getString("relevent_others_doc"));
			dto.setRemittanceDate(rs.getDate("remittance_date"));
			dto.setRevelantDoc(rs.getString("revelant_doc"));
			dto.setSection(rs.getString("section"));
			dto.setSelectionExplanation(rs.getString("selection_explanation"));
			dto.setSiginificantRisksIssues(rs.getString("siginificant_risks_issues"));
			dto.setTenfFutureDate(rs.getDate("tenf_future_date"));
			dto.setTransactionAggrementDocReason(rs.getString("transaction_agreement_doc_reason"));
			dto.setTransactionExaminationResult(rs.getString("transaction_examination_result"));
			dto.setTransactionOpinion(rs.getInt("transaction_opinion")==1?true:false);
			dto.setTrcFutureDate(rs.getDate("trc_future_date"));
			dto.setTrcNationality(rs.getString("trc_nationality"));
			dto.setTrcResidentialsStatusPeriod(rs.getString("trc_residentials_status_period"));
			dto.setType(rs.getString("type"));

			return dto;
	}
	

}
