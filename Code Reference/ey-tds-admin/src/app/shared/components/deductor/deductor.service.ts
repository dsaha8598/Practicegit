import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DeductorService {
  DEDUCTOR_URL = `${environment.api.onboarding}deductor`;
  GETRESIDENTIALSTATUS_URL = `${environment.api.masters}getresidentialstatus`;
  GETDEDUCTOR_URL = `${environment.api.masters}getdeductortype`;
  GETMODEOFPAYMENT_URL = `${environment.api.masters}getmodeofpayment`;
  GETSTATUS_URL = `${environment.api.masters}getstatus`;
  //GETSTATES_URL = `${environment.api.masters}getallstates`;
  GETSTATES_URL = `${environment.api.masters}getstates`;
  GETCOUNTRIES_URL = `${environment.api.masters}getcountries`;
  GETCITIES_URL = `${environment.api.masters}getcitys`;
  GETDUPLICATEPANSTATUS_URL = `${environment.api.onboarding}pan/status`;
  CUST_TRANSFORM_DATA_URL = `${environment.api.onboarding}deductorOnboardingInfo/customTransformation`;
  EXCEL_UPLOAD_URL = `${environment.api.onboarding}deductor/upload/excel`;
  GETDIVIDENDDEDUCTOR_URL = `${environment.api.masters}dividendDeductorTypes`;

  constructor(private http: HttpClient) {}

  getDeductor(): Observable<any> {
    return this.http
      .get<any>(this.DEDUCTOR_URL)
      .pipe(map((res: any) => res.data));
  }
  getDeductorByPan(id: any) {
    return this.http
      .get<any>(`${this.DEDUCTOR_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addDeductor(data: any) {
    return this.http
      .post<any>(this.DEDUCTOR_URL, data)
      .pipe(map((res: any) => res.data));
  }

  getPanStatus(value: any): Observable<any> {
    return this.http
      .get<any>(this.GETDUPLICATEPANSTATUS_URL + '?DEDUCTOR-PAN=' + value)
      .pipe(map((res: any) => res.data));
  }

  getDividendDeductorType(): Observable<any> {
    return this.http
      .get<any>(this.GETDIVIDENDDEDUCTOR_URL)
      .pipe(map((res: any) => res.data));
  }
  upload(payload: any): Observable<any> {
    return this.http
      .post<any>(this.EXCEL_UPLOAD_URL, payload)
      .pipe(map((res: any) => res.data));
  }

  updateDeductorById(data: any) {
    return this.http
      .put<any>(this.DEDUCTOR_URL, data)
      .pipe(map((res: any) => res.data));
  }
  getResidentialStatus(): Observable<any> {
    return this.http
      .get<any>(this.GETRESIDENTIALSTATUS_URL)
      .pipe(map((res: any) => res.data));
  }
  getDeductorType(): Observable<any> {
    return this.http
      .get<any>(this.GETDEDUCTOR_URL)
      .pipe(map((res: any) => res.data));
  }
  getModeOfPayment(): Observable<any> {
    return this.http
      .get<any>(this.GETMODEOFPAYMENT_URL)
      .pipe(map((res: any) => res.data));
  }
  getStatus(): Observable<any> {
    return this.http
      .get<any>(this.GETSTATUS_URL)
      .pipe(map((res: any) => res.data));
  }
  getStates(country: any): Observable<any> {
    return this.http
      .get<any>(`${this.GETSTATES_URL}/${country}`)
      .pipe(map((res: any) => res.data));
  }
  getCities(state: any): Observable<any> {
    return this.http
      .get<any>(`${this.GETCITIES_URL}/${state}`)
      .pipe(map((res: any) => res.data));
  }

  getCountries(): Observable<any> {
    return this.http
      .get<any>(this.GETCOUNTRIES_URL)
      .pipe(map((res: any) => res.data));
  }
  postCustmTranfromData(obj: any): Observable<any> {
    return this.http.post<any>(this.CUST_TRANSFORM_DATA_URL, obj);
  }
}
