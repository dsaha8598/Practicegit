import { Common, ICommon } from './common.model';

export interface ICess extends ICommon {
  cessRate?: number;
  cessTypeId?: number;
  cessTypeName?: string;
  isCessApplicable?: boolean;
  bocNatureOfPayment?: boolean;
  bocDeducteeStatus?: boolean;
  bocInvoiceSlab?: boolean;
  bocDeducteeResidentialStatus?: boolean;
  basisOfCessDetails?: ICessbasis[];
}

export class Cess extends Common implements ICess {
  constructor(
    public cessRate?: number,
    public cessTypeId?: number,
    public cessTypeName?: string,
    public isCessApplicable?: boolean,
    public bocNatureOfPayment?: boolean,
    public bocDeducteeStatus?: boolean,
    public bocInvoiceSlab?: boolean,
    public bocDeducteeResidentialStatus?: boolean,
    public basisOfCessDetails?: ICessbasis[]
  ) {
    super();
  }
}

export interface ICessbasis {
  natureOfPaymentMasterId?: string;
  natureOfPayment?: string;
  nature?: string;
  cessTypeId?: number;
  cessTypeName?: string;
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

export class Cessbasis implements ICessbasis {
  constructor(
    public natureOfPaymentMasterId?: string,
    public natureOfPayment?: string,
    public nature?: string,
    public cessTypeId?: number,
    public cessTypeName?: string,
    public deducteeStatusId?: number,
    public deducteeStatus?: string,
    public status?: string,
    public deducteeResidentialStatusId?: number,
    public deducteeResidentialStatus?: string,
    public residentStatus?: string,
    public invoiceSlabFrom?: number,
    public invoiceSlabTo?: number,
    public rate?: number
  ) {}
}
