import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
@Injectable({
  providedIn: 'root'
})
export class OnboardingService {
  private readonly configurationURL = `${environment.api.onboarding}deductorOnboardingInfo`;
  private readonly getConfigurationURL = `${environment.api.onboarding}getDeductorOnboardingInfo`;

  constructor(private readonly httpClient: HttpClient) {}

  postConfigurations(reqPayload: any): Observable<any> {
    return this.httpClient.post(this.configurationURL, reqPayload);
  }

  getConfigurations(pan: any): Observable<any> {
    const obj = {
      pan: pan
    };
    return this.httpClient.post<any>(this.getConfigurationURL, obj);
  }
}
