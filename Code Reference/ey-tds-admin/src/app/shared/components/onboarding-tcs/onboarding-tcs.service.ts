import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
@Injectable({
  providedIn: 'root'
})
export class OnboardingServiceTcs {
  private readonly saveconfigurationURL = `${environment.api.onboarding}collectorOnboardingInfo`;
  private readonly getConfigurationURL = `${environment.api.onboarding}getCollectorOnboardingInfo`;
  private readonly getLookUpDataForOnboardingURL = `${environment.api.masters}getAllLookupValues`;

  constructor(private readonly httpClient: HttpClient) {}

  postConfigurations(reqPayload: any): Observable<any> {
    return this.httpClient.post(this.saveconfigurationURL, reqPayload);
  }

  getConfigurations(pan: any): Observable<any> {
    const obj = {
      pan: pan
    };
    return this.httpClient.post<any>(this.getConfigurationURL, obj);
  }
  getCollectorOnboardingInfo(): Observable<any> {
    return this.httpClient.get<any>(this.getLookUpDataForOnboardingURL);
  }
}
