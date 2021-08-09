import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  RESOURCE_URL = `${environment.api.administration}redis-data/fetch`;
  EXCEPTION_RESOURCE_URL = `${environment.api.administration}test/exception`;
  EXCEPTION_CUSTOM_RESOURCE_URL = `${environment.api.administration}test/custom-exception`;
  FLUSH_RESOURCE_URL = `${environment.api.administration}redis-data/flush`;
  REFRESH_RESOURCE_URL = `${environment.api.administration}redis-data/refresh`;
  TCS_REFRESH_RESOURCE_URL = `${environment.api.administration}tcs/redis-data/refresh`;
  TCS_COLLECTEE_EXEMPT_URL = `${environment.api.administration}tcs/redis-data/refresh`;

  NOTIFY_TENANT_RESOURCE_URL = `${environment.api.administration}notify/new/tenant`;
  BUILD_NUMBER_RESOURCE_URL = `${environment.api.administration}build-number`;
  constructor(private readonly http: HttpClient) {}

  getresponse(): Observable<any> {
    return this.http.get<any>(this.RESOURCE_URL);
  }

  buildNumber(): Observable<any> {
    return this.http.get<any>(this.BUILD_NUMBER_RESOURCE_URL);
  }
  refreshCollecteeExemptStatus(): Observable<any> {
    return this.http.get<any>(
      `${this.TCS_COLLECTEE_EXEMPT_URL}/COLLECTEE_EXEMPT_STATUS`
    );
  }
  refreshNatureOfPayment(): Observable<any> {
    return this.http.get<any>(`${this.REFRESH_RESOURCE_URL}/NATUREOFPAYMENT`);
  }

  refreshNatureOfIncome(): Observable<any> {
    return this.http.get<any>(
      `${this.TCS_REFRESH_RESOURCE_URL}/NATUREOFINCOME`
    );
  }

  refreshTdsMonthTracker(): Observable<any> {
    return this.http.get<any>(`${this.REFRESH_RESOURCE_URL}/TDSMONTHTRACKER`);
  }

  refreshTcsMonthTracker(): Observable<any> {
    return this.http.get<any>(
      `${this.TCS_REFRESH_RESOURCE_URL}/TCSMONTHTRACKER`
    );
  }

  refreshChartOfAccounts(): Observable<any> {
    return this.http.get<any>(`${this.REFRESH_RESOURCE_URL}/TDSCLASSIFICATION`);
  }

  refreshTcsChartOfAccounts(): Observable<any> {
    return this.http.get<any>(
      `${this.TCS_REFRESH_RESOURCE_URL}/TCSCLASSIFICATION`
    );
  }

  refreshTenantInfo(): Observable<any> {
    return this.http.get<any>(`${this.REFRESH_RESOURCE_URL}/TENANTINFO`);
  }

  notifyNewTenant(): Observable<any> {
    return this.http.get<any>(this.NOTIFY_TENANT_RESOURCE_URL);
  }

  flushData(): Observable<any> {
    return this.http.get<any>(this.FLUSH_RESOURCE_URL);
  }

  getTestException(): Observable<any> {
    return this.http.get<any>(this.EXCEPTION_RESOURCE_URL);
  }

  getCustomException(): Observable<any> {
    return this.http.get<any>(this.EXCEPTION_CUSTOM_RESOURCE_URL);
  }
}
