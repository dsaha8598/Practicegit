import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot
} from '@angular/router';

import { AuthenticationService } from './authentication.service';
import { RouteaccessService } from './routeaccess.service';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly authService: AuthenticationService,
    private readonly routerAccessService: RouteaccessService,
    private readonly router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    if (this.authService.isAuthorized()) {
      if (this.routerAccessService.hasAccess(route.data.authorities)) {
        return true;
      }

      this.router
        .navigate(['/unauthorized'])
        .then()
        .catch();

      return true;
    }

    this.router
      .navigate(['/unauthorized'])
      .then()
      .catch();

    return;
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    return this.canActivate(childRoute, state);
  }
}
