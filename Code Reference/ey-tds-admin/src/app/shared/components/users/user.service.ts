import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../.../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private RolesURL = `${environment.api.onboarding}roles`;
  private userCreateURL = `${environment.api.onboarding}createuser`;
  private getUsersURL = `${environment.api.onboarding}users`;
  private getTansURL = `${environment.api.masters}getTandetailsBydeductorPan/`;
  private PAN_BASES_ROLES_RESOURCE_URL = `${environment.api.onboarding}getRolesByDeductorPan`;
  private ALL_DEDUCTOR_ROLE_BASED_DATA = `${environment.api.onboarding}deductor-tan-list`;
  private getUserDetailsURL = `${environment.api.onboarding}user/by/`;
  private getRoleDetailsURL = `${environment.api.onboarding}role/`;

  constructor(private httpclient: HttpClient) {}

  getRolesByPan(pan: string): Observable<any> {
    const obj = {
      deductorPan: pan
    };
    return this.httpclient.post(`${this.PAN_BASES_ROLES_RESOURCE_URL}`, obj);
  }

  getAllDeductorTanAndRoles(): Observable<any> {
    return this.httpclient.get(this.ALL_DEDUCTOR_ROLE_BASED_DATA);
  }

  getTans(deductorPan: any) {
    return this.httpclient.get(this.getTansURL + deductorPan);
  }

  createUser(payload: any) {
    return this.httpclient.post(this.userCreateURL, payload);
  }

  getUsers(deductorPan: any) {
    return this.httpclient.get(this.getUsersURL);
  }
  getUserByEmail(userId: any) {
    return this.httpclient.get(this.getUserDetailsURL + '/' + userId);
  }
  getUserDetails(email: any) {
    return this.httpclient.get(this.getUserDetailsURL + email);
  }
  getRoleDetails(id: any) {
    return this.httpclient.get(this.getRoleDetailsURL + id);
  }
}
