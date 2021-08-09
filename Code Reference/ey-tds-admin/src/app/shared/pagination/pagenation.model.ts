export interface IPageObject {
  data?: any;
  pagenationState?: any;
  pageSize?: any;
  isNext?: boolean;
  isPrev?: boolean;
  paginationStates?: any;
  pageNumber?: any;
  count?: any;
}

export class PageObject implements IPageObject {
  constructor(
    public data?: any,
    public pagenationState?: any,
    public pageSize?: any,
    public isNext?: boolean,
    public isPrev?: boolean,
    public paginationStates?: any,
    public pageNumber?: any,
    public count?: any
  ) {}
}
