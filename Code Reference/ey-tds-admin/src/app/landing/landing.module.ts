import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { LandingRoutingModule } from './landing-routing.module';
import { LandingComponent } from './landing.component';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { TokenInterceptor } from '@app/core/http/interceptor';
import { DialogModule } from 'primeng/dialog';

@NgModule({
  declarations: [LandingComponent],
  imports: [CommonModule, LandingRoutingModule, DialogModule]
  // providers: [
  //   AuthenticationService,
  //   {
  //     provide: HTTP_INTERCEPTORS,
  //     useClass: TokenInterceptor,
  //     multi: true
  //   }
  // ]
})
export class LandingModule {}
