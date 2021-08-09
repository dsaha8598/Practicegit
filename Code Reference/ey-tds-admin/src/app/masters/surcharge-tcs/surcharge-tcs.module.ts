import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SurchargeTcsRoutingModule } from './surcharge-tcs-routing.module';
import { SurchargeTcsComponent } from './surcharge-tcs.component';
import { SurchargeTcsformComponent } from './surcharge-tcsform/surcharge-tcsform.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { RadioButtonModule } from 'primeng/radiobutton';
import { CheckboxModule } from 'primeng/checkbox';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { SharedModule } from '@app/shared';
import { DropdownModule } from 'primeng/dropdown';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '../../core/http/interceptor';
import { SurchargeTcsService } from './surcharge-tcs.service';
@NgModule({
  declarations: [SurchargeTcsComponent, SurchargeTcsformComponent],
  imports: [
    CommonModule,
    MultiSelectModule,
    SurchargeTcsRoutingModule,
    ReactiveFormsModule,
    TabViewModule,
    FormsModule,
    SplitButtonModule,
    RouterModule,
    CalendarModule,
    TableModule,
    DropdownModule,
    RadioButtonModule,
    CheckboxModule,
    BreadcrumbModule,
    SharedModule
    // HttpClientModule
  ],
  // providers: [
  //   SurchargeService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class SurchargeTcsModule {}
