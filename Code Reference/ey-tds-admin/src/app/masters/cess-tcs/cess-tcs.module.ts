import { CommonModule } from '@angular/common';
import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';

import { CessTcsRoutingModule } from './cess-tcs-routing.module';
import { CessTcsComponent } from './cess-tcs.component';
import { CessTcsformComponent } from './cess-tcsform/cess-tcsform.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TableModule } from 'primeng/table';
import { CalendarModule } from 'primeng/calendar';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { TabViewModule } from 'primeng/tabview';
import { SplitButtonModule } from 'primeng/splitbutton';
import { MessageModule } from 'primeng/message';
import { MessagesModule } from 'primeng/messages';
import { SharedModule } from '@app/shared';
import { CheckboxModule } from 'primeng/checkbox';

import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '../../core/http/interceptor';
import { CessTcsService } from './cess-tcs.service';

@NgModule({
  declarations: [CessTcsComponent, CessTcsformComponent],
  imports: [
    CommonModule,
    CessTcsRoutingModule,
    ReactiveFormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    RadioButtonModule,
    DropdownModule,
    BreadcrumbModule,
    MessagesModule,
    MessageModule,
    SplitButtonModule,
    TabViewModule,
    MultiSelectModule,
    FormsModule,
    SharedModule,
    CheckboxModule,
    HttpClientModule
  ],
  // providers: [
  //   CessService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class CessTcsModule {}
