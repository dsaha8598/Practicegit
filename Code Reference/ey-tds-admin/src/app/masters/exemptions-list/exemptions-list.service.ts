import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ExemptionService {
  private readonly GET_LIST_URL = `${environment.api.masters}all/exemption`;
  private readonly UPLOAD_URL = `${environment.api.masters}exemptionList/upload/excel`;
  constructor(private readonly http: HttpClient) {}

  getList(pageObj: any): Observable<any> {
    return this.http
      .post<any>(this.GET_LIST_URL, pageObj)
      .pipe(map((res: any) => res.data));
  }

  uploadExcel(formData: FormData): Observable<any> {
    return this.http.post<any>(this.UPLOAD_URL, formData);
  }
}
