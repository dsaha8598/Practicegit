import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { RouteaccessService } from '@app/shell/authentication/routeaccess.service';
import { StorageService } from '@app/shell/authentication/storageservice';
import { getLocaleDateTimeFormat } from '@angular/common';

@Component({
  selector: 'ey-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {
  isAuthenticated: boolean;
  displayTanModel: boolean;
  tanList: Array<string>;
  isNoTanFound: boolean;
  selectedPan: any;
  roles: Array<string>;
  logintime: any;
  logintime14minLater: any;
  display: boolean;
  constructor(
    private readonly storageService: StorageService,
    private readonly router: Router,
    private readonly authenticationService: AuthenticationService,
    private readonly routeAccess: RouteaccessService
  ) {}
  ngOnInit(): void {
    this.tanList = [];
    this.roles = [];
    if (this.storageService.getItem('token')) {
      this.goToDashboard();
    }
  }

  login(): void {
    this.authenticationService.login();
  }

  goToDashboard(): void {
    if (this.authenticationService.isAuthorized()) {
      this.authenticationService.getTokenAndAuthorize().subscribe(
        (data: any) => {
          data = data.data;
          if (!data.roles) {
            this.errorHandler();
          } else {
            this.roles = data.rolesAndPermissions
              ? data.rolesAndPermissions
              : [];
            this.storageService.setItem('userEmail', data.userName);
            this.routeAccess.setAuthorities(this.roles, data.roles[0]);
            this.router
              .navigate(['/dashboard/masters'])
              .then()
              .catch();
          }
        },
        (error: any) => {
          this.errorHandler();
        }
      );
      console.log(
        'isAuthenticated : ' + this.authenticationService.isAuthorized()
      );
    }
  }

  errorHandler(): void {
    this.router
      .navigate(['/unauthorized'])
      .then()
      .catch();
  }
}
