export interface INonResident {
  lineItemId?: string;
  type?: string;
  assessmentMonth?: any;
  assessmentYear?: any;
  dtaaSection: string;
  documentNumber?: string;
  incometaxIndiaSection?: string;
  lobClauseSection?: string;
  mfnClauseSection?: string;
  section?: string;
  previousTransactionRemittance?: string;
  amountOfIncomeTaxDeducted?: number;
  basisArrivingAmount?: number;
  isInvoiceNoteTransaction?: boolean;
  invoiceUnavailbilityReason?: string;
  nonTaxbilityReason?: string;
  invoiceNumber?: string;
  invoiceNumberFile?: string;
  invoiceReason?: string;
  invoiceDate?: Date;
  invoiceNatureOfRemittance?: string;
  isOpinion?: boolean;
  isForm15?: boolean;
  isOthers?: boolean;
  istransactionAgreementDoc?: boolean;
  aggrementDocNumber?: string;
  aggrementNatureRemittance?: string;
  agreementCurrencyAmount?: number;
  dtaaExistedCountry?: string;
  aggrementDate?: Date;
  aggrementCurrencyType?: string;
  isInrAmount?: boolean;
  amountPaidCreditedInr?: string;
  proposedDateRemittance?: string;
  transactionAggrementDocReason?: string;
  reasons?: string;
  isInrConversionRate?: boolean;
  inrConversionRate?: number;
  inrCreditedAmount?: number;
  remittanceDate?: Date;
  isIndiaTax?: boolean;
  isAoOrderSection?: boolean;
  aoMasterId?: any;
  aoCertificate?: string;
  natureOfPayment?: string;
  amount?: number;
  aoRate?: number;
  applicableFrom?: Date;
  applicableTo?: Date;
  limitUtilized?: number;
  transactionOpinion?: boolean;
  isReleavantDoc?: boolean;
  releventOpinionDoc?: any;
  releventForm15CBDoc?: any;
  releventOthersDoc?: any;
  isDtaaExist?: boolean;
  relevantCountryDtaaExist?: boolean;
  isQualifyDualPerson?: boolean;
  isTrc?: boolean;
  qualifyDualPersonCountry?: string;
  AssesseeStatus?: string;
  deducteeTin?: string;
  trcResidentialsStatusPeriod?: string;
  address?: string;
  trcNationality?: string;
  isFormTenFAvailble?: boolean;
  formTenFPanNumber?: string;
  formTenFNationality?: string;
  isEntitledClaimDtaa?: boolean;
  entitledClaimRelevantDtaaArticle?: string;
  entitledClaimRelevantRate?: number;
  isIncomeTaxBusinessReasons: boolean;
  incomeTaxBusinessRate?: number;
  incomeTaxBusinessConcludingReason?: string;
  isDtaaLobClause?: boolean;
  dtaaLobClauseImpactRelevantTransaction?: string;
  dtaaLobClauseRate?: number;
  isDtaaMfnClause?: boolean;
  dtaaMfnClauseOtherCountries?: string;
  dtaaMfnClauseRate?: number;
  isIncomeTaxDtaa?: boolean;
  incomeTaxDtaaReason?: string;
  dtaaRate?: number;
  alternativeNature?: string;
  factsUsedConclusion?: string;
  isTransactionExaminedPast?: boolean;
  transactionExaminationResult?: string;
  referedJudicialPrecedents?: string;
  docIdKsDocsRefered?: string;
  idKsDocsRefered?: string;
  isKsConsultationTakenProcessNote?: boolean;
  selectionExplanation?: string;
  isKsConsultationTaken?: boolean;
  revelantDoc?: string;
  certificationDocumentFile?: string;
  isExtrenalCouncilViewTaken?: boolean;
  aggrementExternalCouncilViewDoc?: string;
  isAarApplication?: boolean;
  aarRulingStatusIligation?: boolean;
  isCbdtCircularsRefered?: boolean;
  cbdtCirculars?: boolean;
  isPossibleTakenPosition?: boolean;
  possibleAvailbleForPosition?: string;
  isSignificantRisksIssuesTaken?: boolean;
  siginificantRisksIssues?: string;
  isDiffClientFirmCertification?: boolean;
  detailsDiffOpinion?: string;
  otherRelevantDetails?: string;
  releventForm15CBDocUrl?: string;
  releventOpinionDocUrl?: string;
  releventOthersDocUrl?: string;
  istrcFuture?: boolean;
  istenfFuture?: boolean;
  trcFutureDate?: any;
  tenfFutureDate?: any;
  documentPostingDate?: any;
}

export class NonResident implements INonResident {
  constructor(
    public lineItemId?: string,
    public type?: string,
    public assessmentMonth?: any,
    public assessmentYear?: any,
    public section?: string,
    public previousTransactionRemittance?: string,
    public isInvoiceNoteTransaction?: boolean,
    public invoiceUnavailbilityReason?: string,
    public invoiceNatureOfRemittance?: string,
    public istransactionAgreementDoc?: boolean,
    public invoiceReason?: string,
    public aggrementDocNumber?: string,
    public aggrementNatureRemittance?: string,
    public agreementCurrencyAmount?: number,
    public dtaaSection: string = '',
    public incometaxIndiaSection?: string,
    public lobClauseSection?: string,
    public mfnClauseSection?: string,
    public aggrementDate?: Date,
    public nonTaxbilityReason?: string,
    public amountOfIncomeTaxDeducted?: number,
    public basisArrivingAmount?: number,
    public documentNumber?: string,
    public aggrementCurrencyType?: string,
    public isInrAmount?: boolean,
    public amountPaidCreditedInr?: string,
    public proposedDateRemittance?: string,
    public transactionAggrementDocReason?: string,
    public reasons?: string,
    public isInrConversionRate?: boolean,
    public inrConversionRate?: number,
    public inrCreditedAmount?: number,
    public remittanceDate?: Date,
    public isIndiaTax?: boolean,
    public isAoOrderSection?: boolean,
    public certificationDocumentFile?: string,
    public aoCertificate?: string,
    public natureOfPayment?: string,
    public amount?: number,
    public aoRate?: number,
    public applicableFrom?: Date,
    public applicableTo?: Date,
    public limitUtilized?: number,
    public transactionOpinion?: boolean,
    public isReleavantDoc?: boolean,
    public releventOpinionDoc?: any,
    public releventForm15CBDoc?: any,
    public releventOthersDoc?: any,
    public isDtaaExist?: boolean,
    public relevantCountryDtaaExist?: boolean,
    public isQualifyDualPerson?: boolean,
    public isTrc?: boolean,
    public istrcFuture?: boolean,
    public istenfFuture?: boolean,
    public trcFutureDate?: any,
    public tenfFutureDate?: any,
    public documentPostingDate?: any,
    public qualifyDualPersonCountry?: string,
    public AssesseeStatus?: string,
    public deducteeTin?: string,
    public dtaaExistedCountry?: string,
    public trcResidentialsStatusPeriod?: string,
    public address?: string,
    public trcNationality?: string,
    public isFormTenFAvailble?: boolean,
    public formTenFPanNumber?: string,
    public formTenFNationality?: string,
    public isOpinion: boolean = false,
    public isForm15: boolean = false,
    public isOthers: boolean = false,
    public isEntitledClaimDtaa?: boolean,
    public entitledClaimRelevantDtaaArticle?: string,
    public entitledClaimRelevantRate?: number,
    public isIncomeTaxBusinessReasons: boolean = true,
    public incomeTaxBusinessRate?: number,
    public incomeTaxBusinessConcludingReason?: string,
    public isDtaaLobClause?: boolean,
    public dtaaLobClauseImpactRelevantTransaction?: string,
    public dtaaLobClauseRate?: number,
    public isDtaaMfnClause?: boolean,
    public DtaaMfnClauseOtherCountries?: string,
    public dtaaMfnClauseRate?: number,
    public isIncomeTaxDtaa?: boolean,
    public incomeTaxDtaaReason?: string,
    public dtaaRate?: any,
    public alternativeNature?: string,
    public factsUsedConclusion?: string,
    public isTransactionExaminedPast?: boolean,
    public transactionExaminationResult?: string,
    public referedJudicialPrecedents?: string,
    public docIdKsDocsRefered?: string,
    public idKsDocsRefered?: string,
    public isKsConsultationTakenProcessNote?: boolean,
    public selectionExplanation?: string,
    public isKsConsultationTaken?: boolean,
    public revelantDoc?: string,
    public isExtrenalCouncilViewTaken?: boolean,
    public aggrementExternalCouncilViewDoc?: string,
    public isAarApplication?: boolean,
    public aarRulingStatusIligation?: boolean,
    public isCbdtCircularsRefered?: boolean,
    public cbdtCirculars?: boolean,
    public isPossibleTakenPosition?: boolean,
    public possibleAvailbleForPosition?: string,
    public isSignificantRisksIssuesTaken?: boolean,
    public siginificantRisksIssues?: string,
    public isDiffClientFirmCertification?: boolean,
    public detailsDiffOpinion?: string,
    public otherRelevantDetails?: string,
    public aoMasterId?: string,
    public releventForm15CBDocUrl: string = 'javascript:;',
    public releventOpinionDocUrl: string = 'javascript:;',
    public releventOthersDocUrl: string = 'javascript:;'
  ) {}
}
