export interface ICommon {
  id?: number;
  applicableFrom?: any;
  applicableTo?: any;
}

export class Common implements ICommon {
  constructor(
    public id?: number,
    public applicableFrom?: any,
    public appplicableTo?: any
  ) {}
}

export interface ITableConfig {
  field?: string;
  header?: string;
  width?: string;
  type?: string;
}

export class TableConfig implements ITableConfig {
  constructor(
    public field?: string,
    public header?: string,
    public width?: string,
    public type?: string
  ) {}
}
