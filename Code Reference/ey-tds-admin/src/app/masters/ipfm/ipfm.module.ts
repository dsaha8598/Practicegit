import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
//import { HttpModule } from '@angular/common/http';
import { IpfmRoutingModule } from './ipfm-routing.module';
import { IpfmComponent } from './ipfm.component';
import { IpfmformComponent } from './ipfmform/ipfmform.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { DialogModule } from 'primeng/dialog';
import { SharedModule } from '@app/shared';
import { FormsModule } from '@angular/forms';
import { IpfmService } from './ipfm.service';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '../../core/http/interceptor';
@NgModule({
  declarations: [IpfmComponent, IpfmformComponent],
  imports: [
    CommonModule,
    IpfmRoutingModule,
    ReactiveFormsModule,
    SplitButtonModule,
    DialogModule,
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
export class IpfmModule {}
