import { Common, ICommon } from './common.model';

export interface Iipfm extends ICommon {
  interestType?: string;
  rate?: string;
  finePerDay?: number;
  typeOfIntrestCalculation?: any;
}

export class ipfm extends Common implements Iipfm {
  constructor(
    public interestType?: string,
    public rate?: string,
    public finePerDay?: number,
    public typeOfIntrestCalculation?: any
  ) {
    super();
  }
}
