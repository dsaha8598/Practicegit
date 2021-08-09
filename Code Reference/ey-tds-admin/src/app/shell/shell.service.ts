import { Route, Routes } from '@angular/router';
import { ShellComponent } from './shell.component';
import { AuthGuard } from '@app/shell/authentication/auth.guard';

/**
 * Provides helper methods to create routes.
 */
export class Shell {
  /**
   * Creates routes using the shell component and authentication.
   * @param routes The routes to add.
   * @return {Route} The new route using shell as the base.
   */
  static childRoutes(routes: Routes): Route {
    return {
      path: 'dashboard',
      component: ShellComponent,
      children: routes,
      // canActivate: [AuthGuard],
      // Reuse ShellComponent instance when navigating between child views
      data: {
        reuse: true,
        authorities: ['HAS_ACCESS']
      }
    };
  }
}
