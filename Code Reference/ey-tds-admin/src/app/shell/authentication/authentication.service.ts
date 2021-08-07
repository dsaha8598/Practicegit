import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Observable, of } from 'rxjs';
import { StorageService } from './storageservice';
import { Router } from '@angular/router';
import { RouteaccessService } from './routeaccess.service';
import { MsalService, BroadcastService } from '@azure/msal-angular';
import { InteractionRequiredAuthError } from 'msal';
@Injectable()
export class AuthenticationService {
  authToken: string;
  userDetails: any;
  broadcastServiceLoginSubject: any;
  broadcastServiceLoginErrorSubject: any;
  broadcastServiceAquireErrorSubject: any;
  broadcastServiceAquireSubject: any;
  private readonly AUTHORITIES_RESOURCE_URL = `${environment.api.administration}authorities`;
  private readonly TAN_RESOURCE_URL = `${environment.api.onboarding}users`;
  private readonly ENTITY_TANS_RESOURCE_URL = `${environment.api.authorization}getusertansbyuseremail`;
  private readonly REFRESH_TOKEN_URL = `${environment.api.authorization}refresh/token`;
  constructor(
    private readonly http: HttpClient,
    private readonly storageService: StorageService,
    private readonly authService: MsalService,
    private readonly broadcastService: BroadcastService
  ) {
    this.authToken = undefined;
    this.broadcastServiceLoginErrorSubject = this.broadcastService.subscribe(
      'msal:loginFailure',
      payload => {
        console.log('msal:loginFailure', payload);
      }
    );

    this.broadcastServiceLoginSubject = this.broadcastService.subscribe(
      'msal:loginSuccess',
      payload => {
        console.log('msal:loginSuccess', payload);
        this.userDetails = this.authService.getUser();
        this.authToken = payload._token;
        this.storageService.setItem('token', payload._token);
      }
    );
  }

  renewToken(): void {
    this.authService
      .acquireTokenSilent([environment.config.clientID])
      .then((response: any) => {
        this.userDetails = this.authService.getUser();
        console.log('msal:acquireTokenSuccess', response, this.userDetails);
        this.authToken = response;
        this.storageService.setItem('token', response);
        this.refreshToken({}).subscribe(
          res => {
            console.log('token refreshed');
          },
          error => {
            console.log('error in token refreshed');
          }
        );
      })
      .catch((error: any) => {
        // if it is an InteractionRequired error, send the same request in an acquireToken call
        if (error instanceof InteractionRequiredAuthError) {
          this.authService.acquireTokenRedirect([environment.config.clientID]);
        }
      });
  }

  refreshToken(data: any): Observable<any> {
    return this.http.post<any>(`${this.REFRESH_TOKEN_URL}`, data);
  }

  getUserAccessPermissions(pan: string): Observable<any> {
    const object = {
      pan: pan
    };

    return this.http.post<any>(`${this.ENTITY_TANS_RESOURCE_URL}`, object);
  }

  getRoleAndPermissions(id: any): any {
    return this.http.get<any>(`${environment.api.onboarding}role/${id}`);
  }

  getTokenAndAuthorize(): any {
    let data = {};
    if (this.storageService.getItem('token')) {
      data = this.getAuthorities();
    }

    return data;
  }

  getTanBasedEmail(email: string): any {
    this.http.get(`${this.TAN_RESOURCE_URL}/${email}`);
  }

  getTanName(): string {
    return this.storageService.getItem('tenantId')
      ? this.storageService.getItem('tenantId')
      : 'no tenantId';
  }

  getTan(): string {
    return this.storageService.getItem('activeTan')
      ? this.storageService.getItem('activeTan')
      : 'No Tan';
  }

  getPAN(): string {
    return this.storageService.getItem('activePan')
      ? this.storageService.getItem('activePan')
      : 'No Pan';
  }

  getdeductorPan(): string {
    return this.storageService.getItem('activePan')
      ? this.storageService.getItem('activePan')
      : '';
  }

  isAuthorized(): boolean {
    return this.userDetails !== null;
  }

  getBearerToken(): string {
    return this.storageService.getItem('token');
  }

  login(): void {
    this.authService.loginRedirect();
  }

  getAuthorities(): Observable<any> {
    return this.http.get<any>(`${this.AUTHORITIES_RESOURCE_URL}`);
  }

  getUserData(): Observable<any> {
    return of(this.userDetails);
  }

  getUserName(): string {
    return this.storageService.getItem('userName')
      ? this.storageService.getItem('userName')
      : 'No Tan';
  }

  getUserEmail(): string {
    return this.storageService.getItem('userEmail')
      ? this.storageService.getItem('userEmail')
      : '';
  }

  logout(): void {
    this.storageService.clearAll();
    localStorage.clear();
    this.userDetails = null;
    this.authService.logout();
    this.broadcastService.getMSALSubject().next(1);
    if (this.broadcastServiceLoginSubject) {
      this.broadcastServiceLoginSubject.unsubscribe();
    }
    if (this.broadcastServiceLoginErrorSubject) {
      this.broadcastServiceLoginErrorSubject.unsubscribe();
    }
  }
}
