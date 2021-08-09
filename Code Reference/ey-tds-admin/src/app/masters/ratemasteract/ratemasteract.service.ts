import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IRateMasterAct } from '@app/shared/model/rateMaster.act.model';
import { environment } from '@env/environment';
import { tap, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RateMasterActService {
  private readonly RESOURCE_URL = `${environment.api.masters}dividendRateActs`;
  private readonly GETDEDUCTOR_URL = `${environment.api.masters}dividendDeductorTypes`;
  private readonly RESIDENTIAL_RESOURCE_URL = `${environment.api.masters}getresidentialstatus`;
  private readonly GETSHAREHOLDERCATEGORY_URL = `${environment.api.masters}shareholderCategories`;
  private readonly DIVIDENDSHAREHOLDERCATEGORY_URL = `${environment.api.masters}dividendInstrumentsMappings?dividendDeductorTypeId=`;
  private readonly SECTION_URL = `${environment.api.masters}dividendInstrumentsMappings`;
  private readonly UPLOAD_RESOURCE_URL = `${environment.api.masters}dividendRateActs/upload/excel`;

  constructor(private readonly http: HttpClient) {}
  getRateMasterActList(): Observable<any[]> {
    return this.http
      .get<any[]>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getRateMasterActById(id: any): Observable<IRateMasterAct> {
    return this.http
      .get<any>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addRateMasterActData(data: IRateMasterAct): Observable<IRateMasterAct> {
    return this.http
      .post<IRateMasterAct>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  updateRateMasterActById(id: any, applicableTo: any): Observable<any> {
    let url = this.RESOURCE_URL + '/' + id + '?applicableTo=' + applicableTo;
    return this.http.put<any>(url, '').pipe(map((res: any) => res.data));
  }

  getResidentialStatusList(): Observable<any> {
    return this.http
      .get<any>(this.RESIDENTIAL_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }

  getDeductorType(): Observable<any> {
    return this.http
      .get<any>(this.GETDEDUCTOR_URL)
      .pipe(map((res: any) => res.data));
  }
  getShareholderCategoryALLlist(): Observable<any> {
    return this.http
      .get<any>(this.GETSHAREHOLDERCATEGORY_URL)
      .pipe(map((res: any) => res.data));
  }

  getShareholderCategoryList(id: any): Observable<any> {
    return this.http
      .get<any>(`${this.DIVIDENDSHAREHOLDERCATEGORY_URL}${id}`)
      .pipe(map((res: any) => res.data));
  }
  getSection(deductorID: any, shareholderId: any, residentStatus: any) {
    const URL =
      this.SECTION_URL +
      '?dividendDeductorTypeId=' +
      deductorID +
      '&shareholderCategoryId=' +
      shareholderId +
      '&residentialStatus=' +
      residentStatus;
    return this.http.get<any>(URL).pipe(map((res: any) => res.data));
  }
  uploadRateMasterAct(data: FormData): Observable<any> {
    return this.http
      .post<FormData>(this.UPLOAD_RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
}
