import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IRateMasterTreaty } from '@app/shared/model/rateMaster.treaty.model';
import { environment } from '@env/environment';
import { tap, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class RateMasterTreatyService {
  private readonly RESOURCE_URL = `${environment.api.masters}dividendRateTreaties`;
  private readonly GETCOUNTRIES_URL = `${environment.api.masters}getcountries`;
  private readonly UPLOAD_RESOURCE_URL = `${environment.api.masters}dividendRateTreaties/upload/excel`;

  constructor(private readonly http: HttpClient) {}

  getRateMasterTreatyList(): Observable<any[]> {
    return this.http
      .get<any[]>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getRateMasterTreatyById(id: any): Observable<IRateMasterTreaty> {
    return this.http
      .get<any>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addRateMasterTreatyRate(
    data: IRateMasterTreaty
  ): Observable<IRateMasterTreaty> {
    return this.http
      .post<IRateMasterTreaty>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  updateRateMasterTreatyById(id: any, applicableTo: any): Observable<any> {
    let url = this.RESOURCE_URL + '/' + id + '?applicableTo=' + applicableTo;
    return this.http.put<any>(url, '').pipe(map((res: any) => res.data));
  }

  getCountries(): Observable<any> {
    return this.http
      .get<any>(this.GETCOUNTRIES_URL)
      .pipe(map((res: any) => res.data));
  }

  uploadRateMasterTreaty(data: FormData): Observable<any> {
    return this.http
      .post<FormData>(this.UPLOAD_RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
}
