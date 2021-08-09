import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';

import { MenuItem } from 'primeng/api';
import { RouteaccessService } from '@app/shell/authentication/routeaccess.service';
import { StorageService } from '@app/shell/authentication/storageservice';

@Component({
  selector: 'ey-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  navigation: any;
  items: MenuItem[];
  userName: string;
  role: string;
  isTCSActive: boolean;
  constructor(
    private readonly authenticationService: AuthenticationService,
    private readonly routeAccessService: RouteaccessService,
    private readonly router: Router,
    private readonly storageService: StorageService
  ) {}

  ngOnInit(): void {
    this.userName = this.authenticationService.getUserName();
    this.role = this.routeAccessService.getRole();
    this.loadItems();
    this.loadNavigation();
  }

  loadItems(): void {
    this.items = [
      {
        items: [
          /*  {
            label: 'Settings',
            command: () => {
              this.navigateToSettings();
            }
          }, */
          {
            label: 'Logout',
            command: () => {
              this.logout();
            }
          }
        ]
      }
    ];
  }

  loadNavigation(): void {
    this.isTCSActive =
      this.storageService.getItem('moduleScopeSelected') === 'TCS';
    this.navigation = [
      {
        label: 'Home',
        href: '/dashboard/home',
        authority: 'HOME'
      },
      {
        label: 'Masters',
        href: '/dashboard/masters',
        authority: 'MASTERS'
      },
      {
        label: 'Validation',
        href: this.switchURLsBasedonModuleLoaded('/dashboard/validation'),
        authority: this.setPermissionsBasedOnModule('VALIDATION')
      },
      {
        label: 'Transactions',
        href: this.switchURLsBasedonModuleLoaded('/dashboard/transactions'),
        authority: this.setPermissionsBasedOnModule('TRANSACTIONS')
      },
      {
        label: 'Challans',
        href: this.switchURLsBasedonModuleLoaded('/dashboard/challans'),
        authority: this.setPermissionsBasedOnModule('CHALLANS')
      },
      {
        label: 'Filing',
        href: '/dashboard/filing',
        authority: 'FILING'
      },
      {
        label: 'Dashboard',
        href: '/dashboard/dashboards',
        authority: 'DASHBOARD'
      },
      {
        label: 'Reports',
        href: this.switchURLsBasedonModuleLoaded('/dashboard/reports'),
        authority: this.setPermissionsBasedOnModule('REPORTS')
      },
      {
        label: 'Admin',
        href: '/dashboard/admin',
        authority: 'SUPER ADMIN'
      }
    ];
  }

  switchURLsBasedonModuleLoaded(url: string): string {
    url = this.isTCSActive ? `${url}-tcs` : url;
    return url;
  }

  setPermissionsBasedOnModule(permission: string): string {
    permission = this.isTCSActive ? `TCS_${permission}` : permission;
    return permission;
  }

  logout(): void {
    this.authenticationService.logout();
  }

  /*  navigateToSettings(): void {
    this.router
      .navigate(['/dashboard/settings'])
      .then()
      .catch();
  } */
}
