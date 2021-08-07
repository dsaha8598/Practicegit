import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { IpfmTcsRoutingModule } from './ipfm-tcs-routing.module';
import { IpfmTcsComponent } from './ipfm-tcs.component';
import { IpfmTcsformComponent } from './ipfm-tcsform/ipfm-tcsform.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { SharedModule } from '@app/shared';
import { FormsModule } from '@angular/forms';
import { IpfmTcsService } from './ipfm-tcs.service';
@NgModule({
  declarations: [IpfmTcsComponent, IpfmTcsformComponent],
  imports: [
    CommonModule,
    IpfmTcsRoutingModule,
    ReactiveFormsModule,
    SplitButtonModule,
    FormsModule,
    MultiSelectModule,
    TabViewModule,
    RouterModule,
    CalendarModule,
    TableModule,
    RadioButtonModule,
    BreadcrumbModule,
    SharedModule
    //    HttpModule
  ],
  // providers: [
  //   IpfmService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class IpfmTcsModule {}
