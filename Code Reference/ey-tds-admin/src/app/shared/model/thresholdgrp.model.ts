import { Common, ICommon } from './common.model';

export interface ITHRESHOLD extends ICommon {
  id?: any;
  groupName?: string;
  thresholdAmount?: number;
  applicableFrom?: Date;
  applicableTo?: Date;
  nature?: Array<String>;
}

export class ThresholdGrp extends Common implements ITHRESHOLD {
  constructor(
    public groupName?: string,
    public id?: any,
    public thresholdAmount?: number,
    public nature?: Array<String>,
    public applicableFrom?: Date,
    public applicableTo?: Date
  ) {
    super();
  }
}
