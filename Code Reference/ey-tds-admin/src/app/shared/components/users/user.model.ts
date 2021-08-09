export interface IUser {
  username?: string;
  email?: string;
  userId?: string;
  deductorPans?: Array<string>;
  userAccessDetails?: Array<IAccessDetails>;
}

export interface IAccessDetails {
  pan?: string;
  userTanLevelAccess?: Array<ITanAccess>;
}

export interface ITanAccess {
  tan?: string;
  roleId?: string;
  roleIdTcs?: string;
}

export class User implements IUser {
  constructor(
    public username?: string,
    public email?: string,
    public deductorPans?: Array<string>,
    public userAccessDetails?: Array<IAccessDetails>
  ) {}
}

export class AccessDetails implements IAccessDetails {
  constructor(
    public pan?: string,
    public userTanLevelAccess?: Array<ITanAccess>
  ) {}
}

export class TanAccess implements ITanAccess {
  constructor(
    public tan?: string,
    public roleId?: string,
    public roleIdTcs?: string
  ) {}
}
