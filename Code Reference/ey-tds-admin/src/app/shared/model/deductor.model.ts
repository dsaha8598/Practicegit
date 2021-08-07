import { Common, ICommon } from './common.model';

export interface IDeductor extends ICommon {
  address?: IAddress[];
  deductorCode?: string;
  deductorName?: string;
  deductorResidentialStatusId?: number;
  deductorStatusId?: number;
  deductorTypeId?: number;
  deductorTypeName?: string;
  doesDeductorHasMorethanOneBranch?: boolean;
  dueDateOfTaxPayment?: any;
  email?: string;
  modeOfPaymentId?: number;
  modeOfPaymentType?: string;
  pan?: string;
  phoneNumber?: string;
  provision?: string;
  residentialStatus?: string;
  status?: string;
  tanList?: ITanlist[];
}

export class Deductor extends Common implements IDeductor {
  constructor(
    public address?: IAddress[],
    public deductorCode?: string,
    public deductorName?: string,
    public deductorResidentialStatusId?: number,
    public deductorStatusId?: number,
    public deductorTypeId?: number,
    public deductorTypeName?: string,
    public doesDeductorHasMorethanOneBranch?: boolean,
    public dueDateOfTaxPayment?: any,
    public email?: string,
    public modeOfPaymentId?: number,
    public modeOfPaymentType?: string,
    public pan?: string,
    public phoneNumber?: string,
    public provision?: string,
    public residentialStatus?: string,
    public status?: string,
    public tanList?: ITanlist[]
  ) {
    super();
  }
}

export interface IAddress {
  areaLocality?: string;
  id?: number;
  flatDoorBlockNo?: string;
  nameBuildingVillage?: string;
  pinCode?: string;
  stdCode?: string;
  roadStreetPostoffice?: string;
  stateId?: number;
  stateName?: string;
  countryId?: number;
  countryName?: string;
  townCityDistrict?: string;
}

export class Address implements IAddress {
  constructor(
    public areaLocality?: string,
    public id?: number,
    public flatDoorBlockNo?: string,
    public nameBuildingVillage?: string,
    public pinCode?: string,
    public stdCode?: string,
    public roadStreetPostoffice?: string,
    public stateId?: number,
    public stateName?: string,
    public countryId?: number,
    public countryName?: string,
    public townCityDistrict?: string
  ) {}
}

export interface ITanlist {
  areaLocality?: string;
  flatDoorBlockNo?: string;
  id?: number;
  nameBuildingVillage?: string;
  pinCode?: string;
  stdCode?: string;
  roadStreetPostoffice?: string;
  stateId?: number;
  stateName?: string;
  countryId?: number;
  countryName?: string;
  tan?: string;
  townCityDistrict?: string;
}

export class Tanlist implements ITanlist {
  constructor(
    public areaLocality?: string,
    public flatDoorBlockNo?: string,
    public id?: number,
    public nameBuildingVillage?: string,
    public pinCode?: string,
    public stdCode?: string,
    public roadStreetPostoffice?: string,
    public stateId?: number,
    public stateName?: string,
    public countryId?: number,
    public countryName?: string,
    public tan?: string,
    public townCityDistrict?: string
  ) {}
}
