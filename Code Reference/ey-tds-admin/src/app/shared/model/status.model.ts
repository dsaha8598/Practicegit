export interface IStatus {
  id?: number;
  fileStatus?: string;
  dateOfUpload?: any;
  uploadBy?: string;
  fileName?: string;
  totalRecords?: number;
  duplicateRecords?: number;
  errorsRecords?: number;
  errorsRecordsDownloadUrl?: string;
  processedRecords?: number;
  uploadedFileDownloadUrl?: string;
  mismatchedRecords?: number;
  fileType?: string;
  sourceIdentifier?: string;
}

export class Status implements IStatus {
  constructor(
    public id?: number,
    public fileStatus?: string,
    public dateOfUpload?: any,
    public uploadBy?: string,
    public fileName?: string,
    public totalRecords?: number,
    public processedRecords?: number,
    public duplicateRecords?: number,
    public errorsRecordsDownloadUrl?: string,
    public errorsRecords?: number,
    public uploadedFileDownloadUrl?: string,
    public mismatchedRecords?: number,
    public fileType?: string,
    public sourceIdentifier?: string
  ) {}
}

export interface IRecords {
  count?: string;
  url?: string;
}

export class Records implements IRecords {
  constructor(public count?: string, public url?: string) {}
}
