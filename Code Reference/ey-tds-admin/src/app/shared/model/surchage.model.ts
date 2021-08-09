import { Common, ICommon } from './common.model';

export interface ISurcharge extends ICommon {
  surchargeRate?: number;
  isSurchargeApplicable?: boolean;
  bocNatureOfPayment?: boolean;
  bocDeducteeStatus?: boolean;
  bocInvoiceSlab?: boolean;
  bocDeducteeResidentialStatus?: boolean;
  basisOfSurchargeDetails?: ISurchargebasis[];
}

export class Surcharge extends Common implements ISurcharge {
  constructor(
    public surchargeRate?: number,
    public isSurchargeApplicable?: boolean,
    public bocNatureOfPayment?: boolean,
    public bocDeducteeStatus?: boolean,
    public bocInvoiceSlab?: boolean,
    public bocDeducteeResidentialStatus?: boolean,
    public basisOfSurchargeDetails?: ISurchargebasis[]
  ) {
    super();
  }
}

export interface ISurchargebasis {
  natureOfPaymentMasterId?: number;
  natureOfPayment?: string;
  nature?: string;
  deducteeStatusId?: number;
  deducteeStatus?: string;
  status?: string;
  deducteeResidentialStatusId?: number;
  deducteeResidentialStatus?: string;
  residentStatus?: string;
  invoiceSlabFrom?: number;
  invoiceSlabTo?: number;
  rate?: number;
}

export class Surchargebasis implements ISurchargebasis {
  constructor(
    public natureOfPaymentMasterId?: number,
    public natureOfPayment?: string,
    public nature?: string,
    public deducteeStatusId?: number,
    public deducteeStatus?: string,
    public status?: string,
    public deducteeResidentialStatusId?: number,
    public deducteeResidentialStatus?: string,
    public residentStatus?: string,
    public invoiceSlabFrom?: number,
    public invoiceSlabTo?: number,
    public rate?: number
  ) {
    (this.natureOfPaymentMasterId = 0),
      (this.natureOfPayment = null),
      (this.nature = null),
      (this.deducteeStatusId = 0),
      (this.deducteeStatus = null),
      (this.status = null),
      (this.deducteeResidentialStatusId = 0),
      (this.deducteeResidentialStatus = null),
      (this.residentStatus = null),
      (this.invoiceSlabFrom = 0),
      (this.invoiceSlabTo = 0),
      (this.rate = 0);
  }
}
