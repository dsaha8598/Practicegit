import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { Router } from '@angular/router';
import { RouteaccessService } from './authentication/routeaccess.service';

@Component({
  selector: 'app-shell',
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss']
})
export class ShellComponent implements OnInit {
  constructor(
    private readonly authenticationService: AuthenticationService,
    private readonly router: Router,
    private readonly routerAccess: RouteaccessService
  ) {}

  ngOnInit(): void {
    //   if (this.authenticationService.isAuthorized()) {
    //     if (!this.routerAccess.hasAccess(['SUPER ADMIN'])) {
    //       this.router
    //         .navigate(['/dashboard/home'])
    //         .then()
    //         .catch();
    //     } else {
    //       this.router
    //         .navigate(['/dashboard/masters'])
    //         .then()
    //         .catch();
    //     }
    //   }
  }
}
