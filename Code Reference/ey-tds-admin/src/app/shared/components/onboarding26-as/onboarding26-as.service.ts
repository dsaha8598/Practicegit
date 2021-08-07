import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
@Injectable({
  providedIn: 'root'
})
export class OnboardingService26AS {
  private readonly saveconfigurationURL = `${environment.api.onboarding}26ASOnboardingInfo`;
  private readonly getConfigurationURL = `${environment.api.onboarding}get26ASOnboardingInfo`;
  private readonly getLookUpDataForOnboardingURL = `${environment.api.masters}getAllLookupValues26AS`;

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
  get26ASOnboardingInfo(): Observable<any> {
    return this.httpClient.get<any>(this.getLookUpDataForOnboardingURL);
  }
}
