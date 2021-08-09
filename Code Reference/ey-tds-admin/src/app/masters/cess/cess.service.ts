import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CessService {
  RESOURCE_URL = `${environment.api.masters}cess`;
  CESS_RESOURCE_URL = `${environment.api.masters}getcesstypes`;
  RESIDENTIAL_RESOURCE_URL = `${environment.api.masters}getresidentialstatus`;
  DEDUCTEE_RESOURCE_URL = `${environment.api.masters}getstatus`;
  NOP_RESOURCE_URL = `${environment.api.masters}getnatureofpayment`;
  constructor(private readonly http: HttpClient) {}

  getCessMasterList(): Observable<any> {
    return this.http
      .get<any>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getCessMasterById(id: number): Observable<any> {
    return this.http
      .get<any>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  updateCessMaster(_data: any): Observable<any> {
    return this.http
      .put<any>(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  addCessMaster(_data: any): Observable<any> {
    return this.http
      .post(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  getCessTypeList(): Observable<any> {
    return this.http
      .get<any>(this.CESS_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }

  getResidentialStatusList(): Observable<any> {
    return this.http
      .get<any>(this.RESIDENTIAL_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }

  getDeducteeStatusList(): Observable<any> {
    return this.http
      .get<any>(this.DEDUCTEE_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getNOPList(): Observable<any> {
    return this.http
      .get<any>(this.NOP_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
}
