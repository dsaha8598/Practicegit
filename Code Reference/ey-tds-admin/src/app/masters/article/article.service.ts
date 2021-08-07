import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import {
  IArticle,
  ArticleConditions,
  Conditions
} from '@app/shared/model/article.model';
import { tap, map } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  RESOURCE_URL = `${environment.api.masters}article`;
  private readonly GETCOUNTRIES_URL = `${environment.api.masters}getcountries`;
  private readonly UPLOAD_URL = `${environment.api.masters}articlemaster/upload/excel`;
  constructor(private http: HttpClient) {}

  getArticlelist() {
    return this.http.get(this.RESOURCE_URL).pipe(map((res: any) => res.data));
  }

  getArticlelistById(id: number): Observable<any> {
    return this.http.get<IArticle>(`${this.RESOURCE_URL}/${id}`).pipe(
      tap(data => {
        if (
          data.data &&
          data.data.articleMasterConditions &&
          data.data.articleMasterConditions[0].articleMasterDetailedConditions
            .length === 0
        ) {
          data.data.articleMasterConditions[0].articleMasterDetailedConditions[0] = new Conditions();
        }
        return data.data;
      })
    );
  }

  upload(payload: any): Observable<any> {
    return this.http
      .post<any>(this.UPLOAD_URL, payload)
      .pipe(map((res: any) => res.data));
  }

  updateArticlelistById(_data: any) {
    return this.http
      .put<any>(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  addArticlelist(_data: any) {
    return this.http
      .post(this.RESOURCE_URL, _data)
      .pipe(map((res: any) => res.data));
  }

  getCountries(): Observable<any> {
    return this.http
      .get<any>(this.GETCOUNTRIES_URL)
      .pipe(map((res: any) => res.data));
  }
}
