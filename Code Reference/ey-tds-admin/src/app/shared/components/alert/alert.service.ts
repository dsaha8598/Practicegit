import { Injectable } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';
import { Alert, AlertType } from '@app/shared/model/alert.model';
import { Observable, Subject } from 'rxjs';

@Injectable()
export class AlertService {
  private readonly subject = new Subject<Alert>();
  private keepAfterRouteChange = false;

  constructor(private readonly router: Router) {
    // clear alert messages on route change unless 'keepAfterRouteChange' flag is true
    router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        if (this.keepAfterRouteChange) {
          // only keep for a single route change
          this.keepAfterRouteChange = false;
        } else {
          // clear alert messages
          this.clear();
        }
      }
    });
  }

  getAlert(): Observable<any> {
    return this.subject.asObservable();
  }

  success(message: string, keepAfterRouteChange = false): void {
    this.alert('success', message, keepAfterRouteChange);
  }

  error(message: string, keepAfterRouteChange = false): void {
    this.alert('error', message, keepAfterRouteChange);
  }

  info(message: string, keepAfterRouteChange = false): void {
    this.alert('info', message, keepAfterRouteChange);
  }

  warn(message: string, keepAfterRouteChange = false): void {
    this.alert('warning', message, keepAfterRouteChange);
  }

  alert(type: string, message: string, keepAfterRouteChange = false): void {
    this.keepAfterRouteChange = keepAfterRouteChange;
    this.subject.next({ type, message } as Alert);
  }

  clear(): void {
    // clear alerts
    this.subject.next();
  }
}
