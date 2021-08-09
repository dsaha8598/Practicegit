import { Injectable } from '@angular/core';
import { NavigationStart, Router } from '@angular/router';
import { Alert, AlertType } from '@app/shared/model/alert.model';
import { Observable, Subject } from 'rxjs';
import { distinctUntilChanged } from 'rxjs/operators';

@Injectable()
export class LoaderService {
  private readonly subject = new Subject<boolean>();
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
          this.hide();
        }
      }
    });
  }

  getLoader(): Observable<any> {
    return this.subject.asObservable().pipe(distinctUntilChanged());
  }

  show(): void {
    this.keepAfterRouteChange = true;
    this.subject.next(true);
  }

  hide(): void {
    this.subject.next();
  }
}
