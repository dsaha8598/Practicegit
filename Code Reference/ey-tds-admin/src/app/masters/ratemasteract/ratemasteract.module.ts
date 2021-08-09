import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RateMasterActRoutingModule } from './ratemasteract-routing.module';
import { RateMasterActComponent } from './ratemasteract.component';
import { RateMasterActformComponent } from './ratemasteractform/ratemasteractform.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { RadioButtonModule } from 'primeng/radiobutton';
import { DropdownModule } from 'primeng/dropdown';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { SharedModule } from '@app/shared';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
//import { TokenInterceptor } from '../../core/http/interceptor';
import { RateMasterActService } from './ratemasteract.service';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { DialogModule } from 'primeng/dialog';
@NgModule({
  declarations: [RateMasterActComponent, RateMasterActformComponent],
  imports: [
    CommonModule,
    NgMultiSelectDropDownModule.forRoot(),
    RateMasterActRoutingModule,
    ReactiveFormsModule,
    RouterModule,
    CalendarModule,
    TableModule,
    RadioButtonModule,
    DropdownModule,
    PaginatorModule,
    BreadcrumbModule,
    MultiSelectModule,
    SplitButtonModule,
    TabViewModule,
    SharedModule,
    DialogModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class RateMasterActModule {}
