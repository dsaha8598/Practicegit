import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CurrencyConvertorMasterService {
  PDF_UPLOAD_RESOURCE_URL = `${environment.api.masters}read/currency/import`;
  CURRENCY_RESOURCE_URL = `${environment.api.masters}currency`;
  FILTERED_CURRENCY_URL = `${environment.api.masters}currency/by/`;
  CURRENCY_STATUS_DATA_URL = `${environment.api.masters}currency/status`;
  constructor(private readonly http: HttpClient) {}

  uploadPdfs(formData: FormData): Observable<any> {
    return this.http.post<any>(this.PDF_UPLOAD_RESOURCE_URL, formData);
  }

  getFilteredCurrency(date: any): Observable<any> {
    return this.http.get(this.FILTERED_CURRENCY_URL + date);
  }

  getCurrencyRatesMasterList(): Observable<any> {
    return this.http
      .get<any>(this.CURRENCY_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getCurrencyStatusData(): Observable<any> {
    return this.http
      .get<any>(this.CURRENCY_STATUS_DATA_URL)
      .pipe(map((res: any) => res.data));
  }
}
