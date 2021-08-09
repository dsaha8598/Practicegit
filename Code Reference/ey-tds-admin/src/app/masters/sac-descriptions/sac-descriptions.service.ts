import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SacDescriptionsService {
  private readonly SAC_RESOURCE_URL = `${environment.api.masters}sac`;
  private readonly SAC_UPLOAD_URL = `${environment.api.masters}sac/upload/excel`;
  constructor(private readonly http: HttpClient) {}

  getcoaCodesList(): Observable<any> {
    return this.http
      .get<any>(this.SAC_RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }

  uploadExcel(formData: FormData): Observable<any> {
    return this.http.post<any>(this.SAC_UPLOAD_URL, formData);
  }
}
