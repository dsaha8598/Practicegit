import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BatchService {
  BATCH_UPLOAD_RESOURCE_URL = `${environment.api.masters}master/batch`;
  BATCH_UPLOAD_RESOURCE_BATCH_URL = `${environment.api.ingestion}batch`;
  DIVIDEND_MASTER_RESOURCE_URL = `${environment.api.masters}fileUploads?fileUploadType=`;

  constructor(private readonly http: HttpClient) {}

  getBatchUploadBasedOnType(type: string, year: any): Observable<any> {
    return this.http
      .get<any>(`${this.BATCH_UPLOAD_RESOURCE_URL}/${type}/${year}`)
      .pipe(
        map((res: any) => {
          return res.data;
        })
      );
  }
  getBatchUploadBasedOnBatchType(type: string, year: any): Observable<any> {
    return this.http
      .get<any>(`${this.BATCH_UPLOAD_RESOURCE_BATCH_URL}/${type}/${year}`)
      .pipe(
        map((res: any) => {
          return res.data;
        })
      );
  }
  getDividendMasterList(type: string): Observable<any> {
    return this.http
      .get<any>(`${this.DIVIDEND_MASTER_RESOURCE_URL}${type}`)
      .pipe(
        map((res: any) => {
          return res.data;
        })
      );
  }
}
