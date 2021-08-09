import { CommonModule } from '@angular/common';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NgModule,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { RadioButtonModule } from 'primeng/radiobutton';
import { NatureofpaymentRoutingModule } from './natureofpayment-routing.module';
import { NatureofpaymentComponent } from './natureofpayment.component';
import { NatureofpaymentformComponent } from './natureofpaymentform/natureofpaymentform.component';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { SharedModule } from '@app/shared';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { DialogModule } from 'primeng/dialog';
import { TabViewModule } from 'primeng/tabview';
import { NatureofpaymentService } from './natureofpayment.service';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '../../core/http/interceptor';

@NgModule({
  declarations: [NatureofpaymentComponent, NatureofpaymentformComponent],
  imports: [
    CommonModule,
    MultiSelectModule,
    SplitButtonModule,
    TabViewModule,
    NatureofpaymentRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    RadioButtonModule,
    BreadcrumbModule,
    SharedModule,
    DialogModule
    // HttpClientModule
  ],
  // providers: [
  //   NatureofpaymentService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class NatureofpaymentModule {}
