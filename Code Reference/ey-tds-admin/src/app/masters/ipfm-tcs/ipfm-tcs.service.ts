import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Iipfm } from '@app/shared/model/ipfm.model';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class IpfmTcsService {
  RESOURCE_URL = `${environment.api.masters}tcs/fine-rate`;

  constructor(private readonly http: HttpClient) {}

  getIpfm(): Observable<Iipfm[]> {
    return this.http
      .get<Iipfm[]>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getIpfmById(id: number): Observable<Iipfm> {
    return this.http
      .get<Iipfm>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }
  addIpfm(data: Iipfm): Observable<Iipfm> {
    return this.http
      .post<Iipfm>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  updateIpfm(data: Iipfm): Observable<Iipfm> {
    return this.http
      .put<Iipfm>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
}