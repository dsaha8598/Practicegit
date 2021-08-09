import { ICommon, Common } from './common.model';

export interface ICessType extends ICommon {
  cessType?: string;
}

export class CessType extends Common implements ICessType {
  constructor(public cessType?: string) {
    super();
  }
}
