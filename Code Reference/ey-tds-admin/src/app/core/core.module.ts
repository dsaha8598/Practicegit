import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  HTTP_INTERCEPTORS,
  HttpClient,
  HttpClientModule
} from '@angular/common/http';
import { RouteReuseStrategy, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RouteReusableStrategy } from './route-reusable-strategy';
import { AuthenticationService } from '../shell/authentication/authentication.service';
import { I18nService } from './i18n.service';
import { HttpService } from './http/http.service';
import { HttpCacheService } from './http/http-cache.service';
import { ApiPrefixInterceptor } from './http/api-prefix.interceptor';
import { ErrorHandlerInterceptor } from './http/error-handler.interceptor';
import { CacheInterceptor } from './http/cache.interceptor';
import { AlertServiceInterceptor } from './http/alert-service.interceptor';
import { TokenInterceptor } from './http/interceptor';
import { BrowserModule } from '@angular/platform-browser';
import {
  NgbDateAdapter,
  NgbDateNativeAdapter
} from '@ng-bootstrap/ng-bootstrap';
import { LoaderService } from '@app/shared/loader/loader.service';
import { AlertService } from '@app/shared/components/alert/alert.service';
import { AuthGuard } from '@app/shell/authentication/auth.guard';

@NgModule({
  imports: [CommonModule, BrowserModule, TranslateModule, RouterModule],
  providers: [
    I18nService,
    HttpCacheService,
    AuthenticationService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AlertServiceInterceptor,
      multi: true
    },
    ApiPrefixInterceptor,
    ErrorHandlerInterceptor,
    CacheInterceptor,
    {
      provide: RouteReuseStrategy,
      useClass: RouteReusableStrategy
    },
    AlertService,
    AuthGuard,
    LoaderService,
    { provide: NgbDateAdapter, useClass: NgbDateNativeAdapter },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    }
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        `${parentModule} has already been loaded. Import Core module in the AppModule only.`
      );
    }
  }
}
