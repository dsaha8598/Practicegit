import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class MasterService {
  private readonly SPARKS_API_SYNC_DB_URL = `${environment.api.ingestion}spark/notebook/powerbi`;
  private readonly ROLE_SYNC_URL = `${environment.api.administration}permissions/resettodefaults`;

  constructor(private readonly http: HttpClient) {}

  getCallAPI(): Observable<any> {
    return this.http.get<any>(`${this.SPARKS_API_SYNC_DB_URL}`);
  }

  syncRoles(): Observable<any> {
    return this.http.post<any>(`${this.ROLE_SYNC_URL}`, {});
  }
}
