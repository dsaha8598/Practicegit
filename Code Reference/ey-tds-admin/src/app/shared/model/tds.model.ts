import { Common, ICommon } from './common.model';

export interface ITDSRate extends ICommon {
  isAnnualTransactionLimitApplicable?: boolean;
  annualTransactionLimit?: number;
  isSubNaturePaymentMaster?: boolean;
  natureOfPaymentId?: number;
  natureOfPaymentMaster?: string;
  isPerTransactionLimitApplicable?: boolean;
  perTransactionLimit?: number;
  rate?: number;
  deducteeResidentialStatusId?: number;
  residentialStatusName?: string;
  saccode?: string;
  deducteeStatusId?: number;
  statusName?: string;
  subNatureOfPaymentId?: number;
  subNaturePaymentMaster?: string;
}

export class TDSRate extends Common implements ITDSRate {
  constructor(
    public isAnnualTransactionLimitApplicable?: boolean,
    public annualTransactionLimit?: number,
    public isSubNaturePaymentMaster?: boolean,
    public natureOfPaymentId?: number,
    public natureOfPaymentMaster?: string,
    public isPerTransactionLimitApplicable?: boolean,
    public perTransactionLimit?: number,
    public rate?: number,
    public deducteeResidentialStatusId?: number,
    public residentialStatusName?: string,
    public saccode?: string,
    public deducteeStatusId?: number,
    public statusName?: string,
    public subNatureOfPaymentId?: number,
    public subNaturePaymentMaster?: string
  ) {
    super();
  }
}
