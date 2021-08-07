import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ICessType } from '@app/shared/model/cesstype.model';
import { environment } from '@env/environment';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CesstypeTcsService {
  RESOURCE_URL = `${environment.api.masters}tcs/cesstype`;
  constructor(
    private readonly http: HttpClient,
    private logger: CustomLoggerService
  ) {}

  getCessTypeList(): Observable<Array<ICessType>> {
    return this.http
      .get<Array<ICessType>>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getCessDatabyId(id: number): Observable<ICessType> {
    return this.http
      .get<ICessType>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  saveCessTypeList(_data: ICessType): Observable<ICessType> {
    return this.http
      .post<ICessType>(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  updateCessTypeList(_data: ICessType): Observable<ICessType> {
    this.logger.debug('jahjahsja', _data);
    return this.http
      .put<ICessType>(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }
}
