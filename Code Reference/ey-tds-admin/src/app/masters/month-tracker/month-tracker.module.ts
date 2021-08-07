import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { MonthTrackerRoutingModule } from './month-tracker-routing.module';
import { MonthTrackerComponent } from './month-tracker.component';
import { MonthTrackerFormComponent } from './month-tracker-form/month-tracker-form.component';
import { MultiSelectModule } from 'primeng/multiselect';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
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
  declarations: [MonthTrackerComponent, MonthTrackerFormComponent],
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
    MonthTrackerRoutingModule,
    DropdownModule,
    DialogModule,
    TabViewModule
  ]
  // schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class MonthTrackerModule {}
