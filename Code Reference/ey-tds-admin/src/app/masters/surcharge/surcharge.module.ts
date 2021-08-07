import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { SurchargeRoutingModule } from './surcharge-routing.module';
import { SurchargeComponent } from './surcharge.component';
import { SurchargeformComponent } from './surchargeform/surchargeform.component';
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
import { SurchargeService } from './surcharge.service';
@NgModule({
  declarations: [SurchargeComponent, SurchargeformComponent],
  imports: [
    CommonModule,
    MultiSelectModule,
    SurchargeRoutingModule,
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
export class SurchargeModule {}
