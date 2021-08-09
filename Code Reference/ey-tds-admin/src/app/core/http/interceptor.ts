import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';
import { AuthenticationService } from '../../shell/authentication/authentication.service';

@Injectable({ providedIn: 'root' })
export class TokenInterceptor implements HttpInterceptor {
  constructor(private readonly authenticationService: AuthenticationService) {}
  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${this.authenticationService.getBearerToken()}`,
        'TAN-NUMBER': this.authenticationService.getTan(),
        'X-USER-EMAIL': this.authenticationService.getUserEmail(),
        'DEDUCTOR-PAN': this.authenticationService.getPAN(),
        'X-TENANT-ID': this.authenticationService.getTanName()
      },
      withCredentials: true
    });

    return next.handle(request);
  }
}
