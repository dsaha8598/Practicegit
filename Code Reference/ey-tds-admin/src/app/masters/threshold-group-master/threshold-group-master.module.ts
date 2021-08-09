import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThresholdGroupMasterRoutingModule } from './threshold-group-master-routing.module';
import { ThresholdGroupMasterComponent } from './threshold-group-master.component';
import { ThresholdgrpformComponent } from './thresholdgrpform/thresholdgrpform.component';
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
import { ThresholdGroupMasterService } from './threshold-group-master.service';
import { DialogModule } from 'primeng/dialog';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
@NgModule({
  declarations: [ThresholdGroupMasterComponent, ThresholdgrpformComponent],
  imports: [
    CommonModule,
    NgMultiSelectDropDownModule.forRoot(),
    ThresholdGroupMasterRoutingModule,
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
export class ThresholdGroupMasterModule {}
