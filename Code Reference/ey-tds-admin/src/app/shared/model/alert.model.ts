export class Alert {
  type: string;
  message: string;
}

export enum AlertType {
  Success,
  Error,
  Info,
  Warning
}
