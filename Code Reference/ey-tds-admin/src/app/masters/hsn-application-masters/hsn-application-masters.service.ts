import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HSNApplicationMastersService {
  private readonly HSN_RESOURCE_URL = `${environment.api.masters}tds/hsn`;
  private readonly HSN_UPLOAD_URL = `${environment.api.masters}tds/hsn/upload/excel`;
  private readonly HSN_RATE_MAPPING_RESOURCE_URL = `${environment.api.onboarding}hsn/search`;
  private readonly HSN_CREATE = `${environment.api.masters}tds/hsncode  `;
  private readonly HSN_GET_BY_ID = `${environment.api.masters}tds/hsn`;
  private readonly HSN_UPDATE = `${environment.api.masters}tds/hsn`;
  private readonly GET_SECTION_URL = `${environment.api.masters}nop/sections`;
  constructor(private readonly http: HttpClient) {}

  getcoaCodesList(pageObj: any): Observable<any> {
    return this.http
      .post<any>(this.HSN_RESOURCE_URL, pageObj)
      .pipe(map((res: any) => res.data));
  }
  createHSN(FormData: any): Observable<any> {
    return this.http
      .post<any>(this.HSN_CREATE, FormData)
      .pipe(map((res: any) => res.data));
  }
  getHSNById(id: any): Observable<any> {
    return this.http
      .get<any>(`${this.HSN_GET_BY_ID}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  update(FormData: any): Observable<any> {
    return this.http
      .put<any>(this.HSN_UPDATE, FormData)
      .pipe(map((res: any) => res.data));
  }

  uploadExcel(formData: FormData): Observable<any> {
    return this.http.post<any>(this.HSN_UPLOAD_URL, formData);
  }

  getSectionList(): Observable<any> {
    return this.http
      .get<any>(this.GET_SECTION_URL)
      .pipe(map((res: any) => res.data));
  }

  getNatureofPaymentList(section: any): Observable<any> {
    return this.http
      .get<any>(this.GET_SECTION_URL + `/${section}`)
      .pipe(map((res: any) => res.data));
  }
}
