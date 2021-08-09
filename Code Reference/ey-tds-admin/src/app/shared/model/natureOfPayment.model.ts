import { Common, ICommon } from './common.model';

export interface INatureOfPayment extends ICommon {
  section?: string;
  nature?: string;
  displayValue?: string;
  keywords?: string;
  isSubNaturePaymentApplies?: boolean;
  subNaturePaymentMasters?: ISubNaturePayment;
}

export class NatureOfPayment extends Common implements INatureOfPayment {
  constructor(
    public section?: string,
    public nature?: string,
    public displayValue?: string,
    public keywords?: string,
    public isSubNaturePaymentApplies?: boolean,
    public subNaturePaymentMasters?: ISubNaturePayment
  ) {
    super();
  }
}

export interface ISubNaturePayment {
  id?: number;
  nature?: string;
}

export class SubNaturePayment implements ISubNaturePayment {
  constructor(public id?: number, public nature?: string) {}
}
