import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { TdsrateRoutingModule } from './tdsrate-routing.module';
import { TdsrateComponent } from './tdsrate.component';
import { TdsrateformComponent } from './tdsrateform/tdsrateform.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
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
import { TokenInterceptor } from '../../core/http/interceptor';
import { TdsrateService } from './tdsrate.service';
import { DialogModule } from 'primeng/dialog';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
@NgModule({
  declarations: [TdsrateComponent, TdsrateformComponent],
  imports: [
    CommonModule,
    NgMultiSelectDropDownModule.forRoot(),
    TdsrateRoutingModule,
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
export class TdsrateModule {}
