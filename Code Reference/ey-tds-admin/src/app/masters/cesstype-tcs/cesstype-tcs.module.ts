import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NgModule,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SharedModule } from '@app/shared';
import { NgbDatepicker, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { MessageModule } from 'primeng/message';
import { MessagesModule } from 'primeng/messages';
import { MultiSelectModule } from 'primeng/multiselect';
import { PaginatorModule } from 'primeng/paginator';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
import { ToastModule } from 'primeng/toast';
import { TokenInterceptor } from '../../core/http/interceptor';
import { CesstypeTcsRoutingModule } from './cesstype-tcs-routing.module';
import { CesstypeTcsComponent } from './cesstype-tcs.component';
import { CesstypeTcsService } from './cesstype-tcs.service';
import { CesstypeTcsformComponent } from './cesstype-tcsform/cesstype-tcsform.component';

@NgModule({
  declarations: [CesstypeTcsformComponent, CesstypeTcsComponent],
  imports: [
    CommonModule,
    DropdownModule,
    RouterModule,
    CesstypeTcsRoutingModule,
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
    SharedModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class CesstypeTcsModule {}
