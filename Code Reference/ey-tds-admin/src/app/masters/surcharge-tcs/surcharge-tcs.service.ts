import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { tap, map } from 'rxjs/operators';
import { Surchargebasis } from '@app/shared/model/surchage.model';

@Injectable({
  providedIn: 'root'
})
export class SurchargeTcsService {
  RESOURCE_URL = `${environment.api.masters}tcs/surcharge`;
  CESS_RESOURCE_URL = `${environment.api.masters}getcesstypes`;
  RESIDENTIAL_RESOURCE_URL = `${environment.api.masters}getresidentialstatus`;
  DEDUCTEE_RESOURCE_URL = `${environment.api.masters}getstatus`;
  // NOP_RESOURCE_URL = `${environment.api.masters}tcs/getnatureofincome`;
  constructor(private readonly http: HttpClient) {}

  getSurchargeMasterList(): Observable<any> {
    return this.http
      .get<any>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getSurchargeMasterById(id: number): Observable<any> {
    return this.http.get<any>(`${this.RESOURCE_URL}/${id}`).pipe(
      map(data => {
        if (
          data.data.basisOfSurchargeDetail &&
          data.data.basisOfSurchargeDetails[0].length === 0
        ) {
          data.data.basisOfSurchargeDetails[0] = new Surchargebasis();
        }

        return data.data;
      })
    );
  }

  updateSurchargeMaster(_data: any): Observable<any> {
    return this.http
      .put<any>(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  addSurchargeMaster(_data: any): Observable<any> {
    return this.http
      .post(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  getSurchargeTypeList(): Observable<any> {
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
  /* getNOPList(): Observable<any> {
    return this.http
      .get<any>(this.NOP_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  } */
}
