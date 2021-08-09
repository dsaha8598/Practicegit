import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MonthTrackerService {
  RESOURCE_URL = `${environment.api.masters}monthly-tracker`;
  private readonly UPLOAD_URL = `${environment.api.masters}monthly-tracker/upload/excel`;

  constructor(private readonly http: HttpClient) {}

  getMonthlyTrackerList(): Observable<any> {
    return this.http
      .get<any>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getMonthlyTrackerById(id: number): Observable<any> {
    return this.http
      .get<any>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  updateMonthlyTrackerById(data: any): Observable<any> {
    return this.http
      .put<any>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }

  addMonthlyTracker(data: any): Observable<any> {
    return this.http
      .post<any>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  upload(payload: any): Observable<any> {
    return this.http
      .post<any>(this.UPLOAD_URL, payload)
      .pipe(map((res: any) => res.data));
  }
}
