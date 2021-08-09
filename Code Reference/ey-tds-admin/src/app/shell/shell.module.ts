import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '@app/core/http/interceptor';
import { SharedModule } from '@app/shared';
import { environment } from '@env/environment';
import { MsAdalAngular6Module } from 'microsoft-adal-angular6';
import { AuthGuard } from './authentication/auth.guard';
import { HeaderComponent } from './header/header.component';
import { ShellComponent } from './shell.component';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { MsalModule, MsalInterceptor, MsalGuard } from '@azure/msal-angular';

@NgModule({
  imports: [
    CommonModule,
    TranslateModule,
    NgbModule,
    RouterModule,
    SharedModule,
    // AdalModule.forRoot(environment.config)
    // MsalModule.forRoot(environment.config)
    MsalModule.forRoot(environment.config)
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    }
  ],
  declarations: [HeaderComponent, ShellComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class ShellModule {}
