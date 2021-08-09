import { CommonModule } from '@angular/common';
import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';

import { CessRoutingModule } from './cess-routing.module';
import { CessComponent } from './cess.component';
import { CessformComponent } from './cessform/cessform.component';
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
import { CessService } from './cess.service';

@NgModule({
  declarations: [CessComponent, CessformComponent],
  imports: [
    CommonModule,
    CessRoutingModule,
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
export class CessModule {}
