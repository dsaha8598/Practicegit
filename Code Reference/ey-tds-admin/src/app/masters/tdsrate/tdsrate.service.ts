import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ITDSRate } from '@app/shared/model/tds.model';
import { environment } from '@env/environment';
import { tap, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TdsrateService {
  private readonly RESOURCE_URL = `${environment.api.masters}tds`;
  private readonly GET_NATUREOFPAYMENT_URL = `${environment.api.masters}getnatureofpayment`;
  private readonly GET_SUBNATUREOFPAYMENT_URL = `${environment.api.masters}getsubnatureofpayment`;
  private readonly RESIDENTIAL_RESOURCE_URL = `${environment.api.masters}getresidentialstatus`;
  private readonly DEDUCTEE_RESOURCE_URL = `${environment.api.masters}getstatus`;
  private readonly UPLOAD_URL = `${environment.api.masters}nature-of-payment/upload/excel`;

  constructor(private readonly http: HttpClient) {}
  getTds(): Observable<ITDSRate[]> {
    return this.http
      .get<ITDSRate[]>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getTdsById(id: any): Observable<ITDSRate> {
    return this.http
      .get<any>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addTds(data: ITDSRate): Observable<ITDSRate> {
    return this.http
      .post<ITDSRate>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  updateTdsById(data: ITDSRate): Observable<ITDSRate> {
    return this.http
      .put<ITDSRate>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  getNatureofPaymentList(): Observable<any> {
    return this.http
      .get<any>(this.GET_NATUREOFPAYMENT_URL)
      .pipe(map((res: any) => res.data));
  }
  getSubNatureofPaymentList(): Observable<any> {
    return this.http
      .get<any>(this.GET_SUBNATUREOFPAYMENT_URL)
      .pipe(map((res: any) => res.data));
  }

  getResidentialStatusList(): Observable<any> {
    return this.http
      .get<any>(this.RESIDENTIAL_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  uploadExcel(data: any): Observable<any> {
    return this.http
      .post<any>(this.UPLOAD_URL, data)
      .pipe(map((res: any) => res.data));
  }
  getDeducteeStatusList(): Observable<any> {
    return this.http
      .get<any>(this.DEDUCTEE_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
}
