import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AlertService } from '@app/shared/components/alert/alert.service';
import { LoaderService } from '@app/shared/loader/loader.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
/**
 * Adds a default error handler to all requests.
 */
@Injectable({ providedIn: 'root' })
export class AlertServiceInterceptor implements HttpInterceptor {
  requestCounter = 0;
  constructor(
    private readonly alertService: AlertService,
    private readonly loaderService: LoaderService,
    private readonly loggerService: CustomLoggerService,
    private readonly authenticationService: AuthenticationService
  ) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    const started = Date.now();
    let ok: string;
    this.beginRequest();
    if (!req.headers.get('skip')) {
      return next.handle(req).pipe(
        tap(evt => {
          if (evt instanceof HttpResponse) {
            ok = 'success';
            if (req.params.get('hideLoader') !== 'true') {
              if (evt.body && evt.status) {
                if (evt.body.data) {
                  if (req.method === 'GET') {
                    this.loggerService.info('Success message');
                  } else {
                    this.loggerService.error(
                      'evt.body.statusMessage',
                      evt.body.statusMessage
                    );
                    if (evt.body.statusMessage !== 'NO ALERT') {
                      this.alertService.success(evt.body.statusMessage);
                    }
                  }
                } else {
                  this.loggerService.error('Invalid data format');
                }
              }
            }
          }
        }),
        catchError((err: any) => {
          this.loggerService.error('In Interceptor error block', err);
          console.log(err);
          if (
            err.error.message ===
              'User logged in another session and this session is invalidated' ||
            err.error.message ===
              'User session is invalidated due to session time limit'
          ) {
            this.alertService.error(err.error.message);
            setTimeout(() => {
              this.authenticationService.logout();
            }, 3000);
          } else {
            if (req.method === 'GET') {
              this.alertService.error(
                'Error occured, Please refresh and try again'
              );
            } else {
              if (err.error.message) {
                this.alertService.info(err.error.message);
              }
            }
          }

          return throwError(err);
        }),
        finalize(() => {
          const elapsed = Date.now() - started;
          const msg = `${req.method} "${req.urlWithParams}" ${ok} in ${elapsed} ms.`;
          this.endRequest();
          this.clear();
        })
      );
      // tslint:disable-next-line: no-else-after-return
    } else {
      return next.handle(req);
    }
  }

  clear(): void {
    setTimeout(() => {
      this.alertService.clear();
    }, 7000);
  }

  beginRequest(): void {
    this.requestCounter = Math.max(this.requestCounter, 0) + 1;
    if (this.requestCounter === 1) {
      this.loaderService.show();
    }
  }

  endRequest(): void {
    this.requestCounter = Math.max(this.requestCounter, 1) - 1;
    if (this.requestCounter === 0) {
      this.loaderService.hide();
    }
  }
}
