import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { FileUploadModule } from 'primeng/fileupload';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { SharedModule } from '@app/shared';
import { TabViewModule } from 'primeng/tabview';
import { MultiSelectModule } from 'primeng/multiselect';
import { CheckboxModule } from 'primeng/checkbox';
import { CalendarModule } from 'primeng/calendar';
import { SplitButtonModule } from 'primeng/splitbutton';
import { CurrencyConvertorMasterRoutingModule } from './currency-convertor-master-routing.module';
import { CurrencyConvertorMasterComponent } from './currency-convertor-master.component';
import { DatePipe } from '@angular/common';
@NgModule({
  declarations: [CurrencyConvertorMasterComponent],
  imports: [
    CommonModule,
    DialogModule,
    DropdownModule,
    CheckboxModule,
    MultiSelectModule,
    CalendarModule,
    SplitButtonModule,
    TabViewModule,
    FileUploadModule,
    FormsModule,
    ReactiveFormsModule,
    CurrencyConvertorMasterRoutingModule,
    SharedModule,
    TableModule
  ],
  providers: [DatePipe],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class CurrencyConvertorMasterModule {}
