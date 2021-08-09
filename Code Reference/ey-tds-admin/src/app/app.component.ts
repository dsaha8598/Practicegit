import { Component, HostListener, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { DialogModule } from 'primeng/dialog';
import { Subject } from 'rxjs';
import { filter, map, mergeMap } from 'rxjs/operators';

import { I18nService } from '@app/core';
import { environment } from '@env/environment';

import { BroadcastService, MsalService } from '@azure/msal-angular';
import { Logger } from 'msal';
import { AuthenticationService } from './shell/authentication/authentication.service';
import { StorageService } from './shell/authentication/storageservice';
import { ConfigAssetLoaderService } from './configurations.service';
// const log = new Logger('App');

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  loginCounter: number;
  loginInterval: any;
  display: boolean;
  logoutCounter: number;
  logoutInterval: any;
  constructor(
    private readonly storageService: StorageService,
    private readonly authenticationService: AuthenticationService,
    private readonly configurationsService: ConfigAssetLoaderService
  ) {
    this.logoutCounter = 60;
  }

  @HostListener('window:mousemove') refreshUserState(): void {
    if (this.logoutCounter >= 60) {
      this.loginCounter = 0;
      this.counter();
    }
  }
  @HostListener('window:keydown') refreshUserStateKey(): void {
    if (this.logoutCounter >= 60) {
      this.loginCounter = 0;
      this.counter();
    }
  }
  ngOnInit(): void {
    this.counter();
    console.log('environment configuration', environment);
  }

  triggerPopup(): void {
    this.display = true;
    this.expiryCounter();
  }

  expiryCounter(): void {
    this.logoutCounter = 60;
    clearInterval(this.logoutInterval);
    this.logoutInterval = setInterval(() => {
      this.logoutCounter -= 1;
      if (this.logoutCounter <= 0) {
        clearInterval(this.logoutInterval);
        console.log('user session is inactive for 15min hence logging out');
        this.logoutSession();
      }
    }, 1000);
  }

  continueSession(): void {
    this.logoutCounter = 60;
    this.authenticationService.renewToken();
    this.counter();
    clearInterval(this.logoutInterval);
    this.display = false;
  }

  logoutSession(): void {
    this.display = false;
    this.authenticationService.logout();
  }

  counter(): void {
    if (this.storageService.getItem('token')) {
      this.loginCounter = 0;
      clearInterval(this.loginInterval);
      this.loginInterval = setInterval(() => {
        this.loginCounter += 1000;
        if (this.loginCounter >= 840000) {
          this.triggerPopup();
          console.log('user session is inactive for 14min');
          clearInterval(this.loginInterval);
        }
      }, 1000);
    } else {
      console.log('user not loggedin');
    }
  }
}
