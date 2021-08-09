import { Common, ICommon } from './common.model';

export interface IDeductee extends ICommon {
  deducteeCode?: string;
  deducteeName?: string;
  deducteeStatusId?: string;
  deducteeResidentialStatusId?: string;
  deductorPan?: string;
  deducteePAN?: string;
  isTRCAvailable?: boolean;
  trcApplicableFrom?: any;
  trcApplicableTo?: any;
  trcFile?: any;
  isTenFAvailable?: boolean;
  tenFApplicableFrom?: any;
  tenFApplicableTo?: any;
  tenFFile?: any;
  weatherPEInIndia?: boolean;
  wpeApplicableFrom?: any;
  wpeApplicableTo?: any;
  wpeFile?: any;
  noPEDocumentAvaliable?: boolean;
  noPEFile?: any;
  isPOEMavailable?: boolean;
  poemApplicableFrom?: any;
  poemApplicableTo?: any;
  // countryOfResidenceId?: string;
  deducteeTin?: string;
  defaultRate?: number;
  address?: any;
  emailAddress?: string;
  phoneNumber?: string;
  section?: string;
  rate?: number;
  isDeducteeHasAdditionalSections?: boolean;
  additionalSections?: IAdditionalSections[];
}

export class Deductee extends Common implements IDeductee {
  constructor(
    public deducteeCode?: string,
    public deducteeName?: string,
    public deducteeStatusId?: string,
    public deducteeResidentialStatusId?: string,
    public deductorPan?: string,
    public deducteePAN?: string,
    public isTRCAvailable?: boolean,
    public trcApplicableFrom?: any,
    public trcApplicableTo?: any,
    public trcFile?: any,
    public isTenFAvailable?: boolean,
    public tenFApplicableFrom?: any,
    public tenFApplicableTo?: any,
    public tenFFile?: any,
    public weatherPEInIndia?: boolean,
    public wpeApplicableFrom?: any,
    public wpeApplicableTo?: any,
    public wpeFile?: any,
    public noPEDocumentAvaliable?: boolean,
    public noPEFile?: any,
    public isPOEMavailable?: boolean,
    public poemApplicableFrom?: any,
    public poemApplicableTo?: any,
    // public countryOfResidenceId?: string,
    public deducteeTin?: string,
    public defaultRate?: number,
    public address?: any,
    public emailAddress?: string,
    public phoneNumber?: string,
    public section?: string,
    public rate?: number,
    public isDeducteeHasAdditionalSections?: boolean,
    public additionalSections?: IAdditionalSections[]
  ) {
    super();
  }
}

export interface IAdditionalSections {
  section?: string;
  rate?: number;
}

export class AdditionalSections implements IAdditionalSections {
  constructor(public section?: string, public rate?: number) {
    this.section = null;
    this.rate = 0;
  }
}
