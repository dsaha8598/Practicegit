import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MonthTrackerTcsRoutingModule } from './month-tracker-tcs-routing.module';
import { MonthTrackerTcsComponent } from './month-tracker-tcs.component';
import { MonthTrackerTcsFormComponent } from './month-tracker-tcsform/month-tracker-tcsform.component';
import { MultiSelectModule } from 'primeng/multiselect';
import { TableModule } from 'primeng/table';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { RouterModule } from '@angular/router';
import { SharedModule } from '@app/shared';
import { DropdownModule } from 'primeng/dropdown';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/compiler/src/core';

@NgModule({
  declarations: [MonthTrackerTcsComponent, MonthTrackerTcsFormComponent],
  imports: [
    CommonModule,
    MultiSelectModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    BreadcrumbModule,
    SharedModule,
    MonthTrackerTcsRoutingModule,
    DropdownModule
  ]
  // schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class MonthTrackerTcsModule {}
