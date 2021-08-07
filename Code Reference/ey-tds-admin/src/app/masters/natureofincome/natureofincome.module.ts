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
import { NatureofincomeRoutingModule } from './natureofincome-routing.module';
import { NatureofincomeComponent } from './natureofincome.component';
import { NatureofincomeformComponent } from './natureofincomeform/natureofincomeform.component';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { SharedModule } from '@app/shared';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';

@NgModule({
  declarations: [NatureofincomeComponent, NatureofincomeformComponent],
  imports: [
    CommonModule,
    MultiSelectModule,
    SplitButtonModule,
    TabViewModule,
    NatureofincomeRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    RadioButtonModule,
    BreadcrumbModule,
    SharedModule
    // HttpClientModule
  ],
  // providers: [
  //   NatureofpaymentService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class NatureofincomeModule {}
