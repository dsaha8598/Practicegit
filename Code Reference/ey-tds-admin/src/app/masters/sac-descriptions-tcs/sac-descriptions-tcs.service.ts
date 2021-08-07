import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SacDescriptionsTcsService {
  private readonly SAC_RESOURCE_URL = `${environment.api.masters}tcs/hsn`;
  private readonly SAC_UPLOAD_URL = `${environment.api.masters}tcs/sac/upload/excel`;
  constructor(private readonly http: HttpClient) {}

  getcoaCodesList(pageObj: any): Observable<any> {
    return this.http
      .post<any>(this.SAC_RESOURCE_URL, pageObj)
      .pipe(map((res: any) => res.data));
  }

  uploadExcel(formData: FormData): Observable<any> {
    return this.http.post<any>(this.SAC_UPLOAD_URL, formData);
  }
}
