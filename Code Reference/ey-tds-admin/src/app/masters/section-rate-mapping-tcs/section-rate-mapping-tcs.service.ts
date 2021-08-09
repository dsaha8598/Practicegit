import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class SectionRateMappingService {
  private readonly SECTION_RATE_MAPPING_RESOURCE_URL = `${environment.api.administration}tcs/redis-data/lookup`;
  constructor(private httpClient: HttpClient) {}

  getSectionRateMaps(key: any) {
    return this.httpClient.get(
      this.SECTION_RATE_MAPPING_RESOURCE_URL + `/${key}`
    );
  }
}
