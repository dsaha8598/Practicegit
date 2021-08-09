import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { NGXLogger } from 'ngx-logger';
import { StorageService } from '@app/shell/authentication/storageservice';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class CustomLoggerService {
  constructor(
    private readonly loggerService: NGXLogger,
    private readonly storageService: StorageService,
    private readonly authenticationService: AuthenticationService
  ) {
    this.loggerService.setCustomHttpHeaders(
      new HttpHeaders({
        Authorization: `Bearer ${this.storageService.getItem('token')}`,
        skip: 'true'
      })
    );
  }

  info(...args: any): void {
    this.loggerService.info(args);
  }

  debug(...args: any): void {
    this.loggerService.debug(args);
  }

  error(...args: any): void {
    this.loggerService.error(args);
  }
}
