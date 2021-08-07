import { Common, ICommon } from './common.model';

export interface IArticle extends ICommon {
  articleName?: string;
  articleNumber?: string;
  articleRate?: number;
  country?: string;
  isInclusionOrExclusion?: boolean;
  articleMasterConditions?: IArticleConditions[];
}

export class Article extends Common implements IArticle {
  constructor(
    public articleName?: string,
    public articleNumber?: string,
    public articleRate?: number,
    public country?: string,
    public isInclusionOrExclusion?: boolean,
    public articleMasterConditions?: IArticleConditions[]
  ) {
    super();
  }
}

export interface IArticleConditions {
  condition?: string;
  id?: number;
  articleMasterDetailedConditions?: IConditions[];
  isDetailedConditionApplicable?: boolean;
}

export class ArticleConditions implements IArticleConditions {
  constructor(
    public condition?: string,
    public id?: number,
    public isDetailedConditionApplicable?: boolean,
    public articleMasterDetailedConditions?: IConditions[]
  ) {}
}

export interface IConditions {
  id?: number;
  condition?: number;
}

export class Conditions implements IConditions {
  constructor(public id?: number, public condition?: number) {
    this.id = 0;
    this.condition = 0;
  }
}
