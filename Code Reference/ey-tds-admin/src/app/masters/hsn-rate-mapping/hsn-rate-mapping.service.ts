import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class HSNRateMappingService {
  private readonly HSN_RATE_MAPPING_RESOURCE_URL = `${environment.api.masters}tcs/hsn/search`;
  private readonly HSN_APPLICATION_MAPPING_RESOURCE_URL = `${environment.api.masters}tds/hsn/search`;
  constructor(private httpClient: HttpClient) {}

  getHsnSearch(key: any) {
    return this.httpClient.get(this.HSN_RATE_MAPPING_RESOURCE_URL + `/${key}`);
  }

  getTdsHsnSearch(key: any) {
    return this.httpClient.get(
      this.HSN_APPLICATION_MAPPING_RESOURCE_URL + `/${key}`
    );
  }
}
