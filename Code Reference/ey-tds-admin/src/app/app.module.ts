import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NgModule,
  APP_INITIALIZER
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CoreModule } from '@app/core';
import { SharedModule } from '@app/shared';
import { environment } from '@env/environment';
import { NgbModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { Angulartics2Module } from 'angulartics2';
import { LZStringModule, LZStringService } from 'ng-lz-string';
import { CookieService } from 'ngx-cookie-service';
import { LoggerModule, NgxLoggerLevel } from 'ngx-logger';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NotfoundComponent } from './notfound/notfound.component';
import { ShellModule } from './shell/shell.module';
import { AccordionModule } from 'primeng/accordion';
import { ConfigAssetLoaderService } from './configurations.service';
import { DialogModule } from 'primeng/dialog';
import { MsalConfig, MsalModule } from '@azure/msal-angular';
import { MSAL_CONFIG } from '@azure/msal-angular/dist/msal.service';

export function initApp(): MsalConfig {
  var request = new XMLHttpRequest();
  request.open('GET', `${environment.serverUrl}/administration/props`, false); // request application settings synchronous
  request.send(null);
  const response = JSON.parse(request.responseText);
  environment.config.clientID = response.clientID;
  environment.config.authority = response.authority;
  environment.config.redirectUri = response.redirectUri;
  environment.config.postLogoutRedirectUri = response.postLogoutRedirectUri;
  environment.config.unprotectedResources = response.unprotectedResources;
  environment.config.validateAuthority = response.validateAuthority;
  localStorage.setItem('hostname', response.hostName);
  console.log('response', response);
  return environment.config;
}

export function appInit(configService: ConfigAssetLoaderService) {
  return () => configService.loadWebSockets();
}
@NgModule({
  imports: [
    BrowserModule,
    LoggerModule.forRoot({
      serverLoggingUrl: environment.api.logger,
      level: NgxLoggerLevel.DEBUG,
      serverLogLevel: NgxLoggerLevel.ERROR
    }),
    FormsModule,
    TranslateModule.forRoot(),
    NgbModule.forRoot(),
    BrowserAnimationsModule,
    CoreModule,
    HttpClientModule,
    SharedModule,
    ShellModule,
    AccordionModule,
    TableModule,
    Angulartics2Module.forRoot([]),
    AppRoutingModule,
    BreadcrumbModule,
    NgbTooltipModule,
    SplitButtonModule,
    TabViewModule,
    DialogModule,
    MultiSelectModule,
    MsalModule,
    LZStringModule // must be imported as the last module as it contains the fallback route
  ],
  declarations: [AppComponent, NotfoundComponent],
  providers: [
    {
      provide: MSAL_CONFIG, // MsalService needs config, this provides it.
      useFactory: initApp
    },
    {
      provide: APP_INITIALIZER,
      useFactory: appInit,
      multi: true,
      deps: [ConfigAssetLoaderService]
    },
    CookieService,
    LZStringService,
    ConfigAssetLoaderService
  ],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule {}
