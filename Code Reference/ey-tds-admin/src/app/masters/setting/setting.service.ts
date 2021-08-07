import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Injectable({
  providedIn: 'root'
})
export class SettingService {
  Setting_URL: 'http://localhost:8080/api/nature-of-payment-masters/create';
  constructor(private http: HttpClient, private logger: CustomLoggerService) {}

  addNotification(data: any) {
    this.logger.debug('here is the data', data);
    return this.http.post<any>(this.Setting_URL, JSON.stringify(data));
  }
}
