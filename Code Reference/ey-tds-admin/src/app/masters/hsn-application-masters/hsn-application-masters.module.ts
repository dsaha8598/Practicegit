import { CommonModule } from '@angular/common';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NgModule,
  NO_ERRORS_SCHEMA
} from '@angular/core';

import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SharedModule } from '@app/shared';
import { PaginatorModule } from 'primeng/paginator';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { MessageModule } from 'primeng/message';
import { MessagesModule } from 'primeng/messages';
import { MultiSelectModule } from 'primeng/multiselect';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
import { HSNClientRoutingModule } from './hsn-application-masters-routing.module';
import { HSNApplicationMastersComponent } from './hsn-application-masters.component';
import { HSNFormComponent } from './hsn-form/hsn-form.component';
import { HSNApplicationMastersService } from './hsn-application-masters.service';
import { TokenInterceptor } from '@app/core/http/interceptor';
import { AlertServiceInterceptor } from '@app/core';
import { DialogModule } from 'primeng/dialog';

@NgModule({
  declarations: [HSNApplicationMastersComponent, HSNFormComponent],
  imports: [
    CommonModule,
    DropdownModule,
    RouterModule,
    HSNClientRoutingModule,
    TableModule,
    MessagesModule,
    MessageModule,
    ReactiveFormsModule,
    CalendarModule,
    BreadcrumbModule,
    SplitButtonModule,
    TabViewModule,
    MultiSelectModule,
    FormsModule,
    SharedModule,
    DialogModule,
    RadioButtonModule,
    SplitButtonModule,
    PaginatorModule
  ],
  // providers: [
  //   DeducteeService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
  //   {
  //     provide: HTTP_INTERCEPTORS,
  //     useClass: AlertServiceInterceptor,
  //     multi: true
  //   }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class HSNApplicationMastersModule {}
