import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RatemasterRoutingModule } from './ratemaster-routing.module';
import { RatemasterComponent } from './ratemaster.component';
import { RatemasterformComponent } from './ratemasterform/ratemasterform.component';
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
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
@NgModule({
  declarations: [RatemasterComponent, RatemasterformComponent],
  imports: [
    CommonModule,
    NgMultiSelectDropDownModule.forRoot(),
    RatemasterRoutingModule,
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
    SharedModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class RatemasterModule {}
