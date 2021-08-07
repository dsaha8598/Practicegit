import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class RolesPermissionsService {
  private ROLEPERMISSIONSRESOURCE_URL = `${environment.api.onboarding}role/permission`;
  private ROLEPERMISSIONSRESOURCE_TCS_URL = `${environment.api.onboarding}tcs/role/permissionGet `;
  private GET_ROLES_RESOURCE_URL = `${environment.api.onboarding}role`;
  private readonly PERMISSIONSRESOURCE_URL = `${environment.api.administration}permission`;
  private readonly PERMISSIONS_URL = `${environment.api.onboarding}rolepermission`;
  private ROLES_RESOURCE_URL = `${environment.api.administration}rolepermissions`;
  constructor(private httpClient: HttpClient) {}

  getRoles() {
    return this.httpClient.get(this.ROLES_RESOURCE_URL);
  }

  createRolePermission(payload: any) {
    return this.httpClient.post(this.ROLEPERMISSIONSRESOURCE_URL, payload);
  }

  getRolesPermission(id: any) {
    return this.httpClient.get(this.GET_ROLES_RESOURCE_URL + '/' + id);
  }

  createPermission(payload: any) {
    return this.httpClient.post(this.PERMISSIONS_URL, payload);
  }

  getPermissions() {
    return this.httpClient.get(this.PERMISSIONSRESOURCE_URL);
  }
}
