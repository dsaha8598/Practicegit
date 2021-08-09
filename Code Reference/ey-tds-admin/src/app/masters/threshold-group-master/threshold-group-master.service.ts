import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { tap, map } from 'rxjs/operators';
import { ITHRESHOLD } from '@app/shared/model/thresholdgrp.model';

@Injectable({
  providedIn: 'root'
})
export class ThresholdGroupMasterService {
  private readonly RESOURCE_URL = `${environment.api.masters}get/threshold/limit/group`;
  private readonly GET_BY_ID = `${environment.api.masters}threshold-group`;
  private readonly GET_NATUREOFPAYMENT_URL = `${environment.api.masters}getnatureofpayment`;
  private readonly DEDUCTEE_RESOURCE_URL = `${environment.api.masters}getstatus`;
  private readonly UPDATE_URL = `${environment.api.masters}threshold-group`;

  constructor(private readonly http: HttpClient) {}
  getThresholdList(): Observable<ITHRESHOLD[]> {
    return this.http
      .get<ITHRESHOLD[]>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getthresholdById(id: any): Observable<ITHRESHOLD> {
    return this.http
      .get<any>(`${this.GET_BY_ID}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addThreshold(data: ITHRESHOLD): Observable<ITHRESHOLD> {
    return this.http
      .post<ITHRESHOLD>(this.GET_BY_ID, data)
      .pipe(map((res: any) => res.data));
  }
  updateThresholdById(data: ITHRESHOLD): Observable<ITHRESHOLD> {
    return this.http
      .put<ITHRESHOLD>(this.UPDATE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  getNatureofPaymentList(): Observable<any> {
    return this.http
      .get<any>(this.GET_NATUREOFPAYMENT_URL)
      .pipe(map((res: any) => res.data));
  }
}
