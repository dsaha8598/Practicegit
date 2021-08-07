import { Common, ICommon } from './common.model';

export interface IRateMasterAct extends ICommon {
  dividendDeductorTypeId?: string;
  shareholderCategoryId?: string;
  residentialStatus?: string;
  section?: number;
  tdsRate?: number;
  exemptionThreshold?: number;
  applicableFrom?: string;
  applicableTo?: string;
  dividendDeductorType?: string;
  shareholderCategory?: string;
  exemptionThresholdDisplay?: any;
}

export class RateMasterAct extends Common implements IRateMasterAct {
  constructor(
    public dividendDeductorTypeId?: string,
    public shareholderCategoryId?: string,
    public residentialStatus?: string,
    public section?: number,
    public tdsRate?: number,
    public exemptionThreshold?: number,
    public applicableFrom?: string,
    public applicableTo?: string,
    public dividendDeductorType?: string,
    public shareholderCategory?: string,
    public exemptionThresholdDisplay?: any
  ) {
    super();
  }
}
