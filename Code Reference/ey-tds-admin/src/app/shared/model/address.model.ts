export interface IAddress {
  areaLocality?: string;
  id?: number;
  flatDoorBlockNo?: string;
  nameBuildingVillage?: string;
  pinCode?: string;
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
    public roadStreetPostoffice?: string,
    public stateId?: number,
    public stateName?: string,
    public countryId?: number,
    public countryName?: string,
    public townCityDistrict?: string
  ) {}
}
