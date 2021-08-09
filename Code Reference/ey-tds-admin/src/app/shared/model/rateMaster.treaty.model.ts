import { Common, ICommon } from './common.model';

export interface IRateMasterTreaty extends ICommon {
  sno?: number;
  countryname?: string;
  taxTreatyClause?: number;
  mfnclause?: string;
  mfnClauseExists?: boolean;
  mfnClauseDisplay?: any;
  mfnAvailedCompanyTaxRate?: number;
  mfnAvailedNonCompanyTaxRate?: number;
  mfnNotAvailedNonCompanyTaxRate?: number;
  mfnNotAvailedCompanyTaxRate?: number;
  mfnNotSelectedAvailedToComp?: number;
  mfnNotSelectedAvailedToNonComp?: number;
  mliArticle8Applicable?: boolean;
  mliPptConditionSatisfied?: boolean;
  mliSlobConditionSatisfied?: boolean;

  mliArticle8ApplicableDisplay?: any;
  mliPptConditionSatisfiedDisplay?: any;
  mliSlobConditionSatisfiedDisplay?: any;

  foreignCompShareholdingInIndComp?: string;
  shareholdingPeriod?: string;
  residentShareholdingIndivisual?: string;
  dividendLowerTaxRate?: boolean;
  benificialOwner?: Boolean;
  dividendImmovableProperty?: Boolean;
  PERIOD_OF_SHAREHOLDING?: string;
  SHAREHOLDING_IN_FOREIGN_COMPANY?: string;
  IS_DIVIDEND_TAXABLE_AT_A_RATE?: boolean;
  BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL?: boolean;
  DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY?: boolean;
  countryId?: number;
  countrySpecificRules?: string;
  applicableFrom?: string;
  applicableTo?: string;
  IS_DIVIDEND_TAXABLE_AT_A_RATE_Display?: any;
  BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display?: any;
  DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY?: any;
  mfnAvailedCompanyTaxRateDisplay?: any;
  mfnAvailedNonCompanyTaxRateDisplay?: any;
  mfnNotAvailedNonCompanyTaxRateDisplay?: any;
  mfnNotAvailedCompanyTaxRateDisplay?: any;
}

export class RateMasterTreaty extends Common implements IRateMasterTreaty {
  constructor(
    public sno?: number,
    public countryname?: string,
    public taxTreatyClause?: number,
    public mfnclause?: string,
    public mfnClauseExists?: boolean,
    public mfnAvailedCompanyTaxRate?: number,
    public mfnAvailedNonCompanyTaxRate?: number,
    public mfnNotAvailedNonCompanyTaxRate?: number,
    public mfnNotAvailedCompanyTaxRate?: number,
    public mliArticle8Applicable?: boolean,
    public mliPptConditionSatisfied?: boolean,
    public mliSlobConditionSatisfied?: boolean,
    public foreignCompShareholdingInIndComp?: string,
    public shareholdingPeriod?: string,
    public residentShareholdingIndivisual?: string,
    public dividendLowerTaxRate?: boolean,
    public benificialOwner?: Boolean,
    public dividendImmovableProperty?: Boolean,
    public PERIOD_OF_SHAREHOLDING?: string,
    public SHAREHOLDING_IN_FOREIGN_COMPANY?: string,
    public IS_DIVIDEND_TAXABLE_AT_A_RATE?: boolean,
    public BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL?: boolean,
    public DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY?: boolean,
    public countryId?: number,
    public countrySpecificRules?: string,
    public applicableFrom?: string,
    public applicableTo?: string,
    public mfnClauseDisplay?: any,
    public mliArticle8ApplicableDisplay?: any,
    public mliPptConditionSatisfiedDisplay?: any,
    public mliSlobConditionSatisfiedDisplay?: any,
    public IS_DIVIDEND_TAXABLE_AT_A_RATE_Display?: any,
    public BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display?: any,
    public DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY?: any,
    public mfnAvailedCompanyTaxRateDisplay?: any,
    public mfnAvailedNonCompanyTaxRateDisplay?: any,
    public mfnNotAvailedNonCompanyTaxRateDisplay?: any,
    public mfnNotAvailedCompanyTaxRateDisplay?: any
  ) {
    super();
  }
}
