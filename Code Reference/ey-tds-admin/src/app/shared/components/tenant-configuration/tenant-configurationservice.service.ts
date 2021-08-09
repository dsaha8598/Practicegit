import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class TenantConfigurationService {
  private getTenantConfigURL = `${environment.api.administration}getTenantConfigDetailsByTenantId`;
  private createTenantURL = `${environment.api.authorization}createtenant`;
  private databaseConfigurationURL = `${environment.api.authorization}tenantconfig`;
  constructor(private httpclient: HttpClient) {}

  getTenantConfigDetails(tenantId: any) {
    return this.httpclient.get(this.getTenantConfigURL + '/' + tenantId);
  }

  postTenant(payload: any) {
    return this.httpclient.post(this.createTenantURL, payload);
  }

  postConfigurations(payload: any) {
    return this.httpclient.post(this.databaseConfigurationURL, payload);
  }

  postBlobConfiguration(payload: any) {
    return this.httpclient.post(this.databaseConfigurationURL, payload);
  }

  postSFTPConfiguration(payload: any) {
    return this.httpclient.post(this.databaseConfigurationURL, payload);
  }
}
