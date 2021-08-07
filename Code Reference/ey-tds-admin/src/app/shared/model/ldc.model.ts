import { Common, ICommon } from './common.model';

export interface ILDC extends ICommon {
  ldcCertificateNumber?: number;
  deductorTANId?: number;
  deductorTAN?: string;
  deducteeNameId?: number;
  deducteeName?: string;
  deducteePan?: string;
  natureOfPaymentSectionId?: number;
  natureOfPaymentSection?: string;
  amount?: number;
  ldcRate?: number;
  sectionAsPerLdcDb?: number;
  amountAsPerLdcDb?: number;
  aoRateAsPerLdcDb?: number;
  applicableFromAsPerLdcDb?: Date;
  applicableToAsPerLdcDb?: Date;
  validationDate?: Date;
  limitUtilised?: number;
}

export class LDC extends Common implements ILDC {
  constructor(
    public ldcCertificateNumber?: number,
    public deductorTANId?: number,
    public deductorTAN?: string,
    public deducteeNameId?: number,
    public deducteeName?: string,
    public deducteePan?: string,
    public natureOfPaymentSectionId?: number,
    public natureOfPaymentSection?: string,
    public amount?: number,
    public ldcRate?: number,
    public sectionAsPerLdcDb?: number,
    public amountAsPerLdcDb?: number,
    public aoRateAsPerLdcDb?: number,
    public applicableFromAsPerLdcDb?: Date,
    public applicableToAsPerLdcDb?: Date,
    public validationDate?: Date,
    public limitUtilised?: number
  ) {
    super();
  }
}
