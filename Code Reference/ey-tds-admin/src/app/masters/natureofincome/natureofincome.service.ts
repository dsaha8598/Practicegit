import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { INatureOfPayment } from '@app/shared/model/natureOfPayment.model';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NatureofincomeService {
  RESOURCE_URL = `${environment.api.masters}tcs/nature-of-income`;
  constructor(private readonly http: HttpClient) {}

  getNatureOfCollectionList(): Observable<any> {
    return this.http
      .get<any>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getNatureOfCollectionById(id: number): Observable<INatureOfPayment> {
    return this.http
      .get<INatureOfPayment>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  updateNatureOfCollectionById(data: any): Observable<INatureOfPayment> {
    return this.http
      .put<INatureOfPayment>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }

  addNatureOfCollection(data: any): Observable<INatureOfPayment> {
    return this.http
      .post<INatureOfPayment>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
}
