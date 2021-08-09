import { Common, ICommon } from './common.model';

export interface IAO extends ICommon {
  aoCertificateNumber?: number;
  deductorTANId?: number;
  deductorTAN?: string;
  deducteeNameId?: number;
  deducteeName?: string;
  deducteePAN?: string;
  natureOfPaymentSectionId?: number;
  natureOfPaymentSection?: number;
  amount?: number;
  aoRate?: number;
  sectionAsPerAoDb?: number;
  amountAsPerAoDb?: number;
  aoRateAsPerAoDb?: number;
  applicableFromAsPerAoDb?: Date;
  applicableToAsPerAoDb?: Date;
  validationDate?: Date;
  limitUtilised?: number;
}

export class AO extends Common implements IAO {
  constructor(
    public aoCertificateNumber?: number,
    public deductorTANId?: number,
    public deductorTAN?: string,
    public deducteeNameId?: number,
    public deducteeName?: string,
    public deducteePAN?: string,
    public natureOfPaymentSectionId?: number,
    public natureOfPaymentSection?: number,
    public amount?: number,
    public aoRate?: number,
    public sectionAsPerAoDb?: number,
    public amountAsPerAoDb?: number,
    public aoRateAsPerAoDb?: number,
    public applicableFromAsPerAoDb?: Date,
    public applicableToAsPerAoDb?: Date,
    public validationDate?: Date,
    public limitUtilised?: number
  ) {
    super();
  }
}
