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
import { CesstypeRoutingModule } from './cesstype-routing.module';
import { CesstypeComponent } from './cesstype.component';
import { CesstypeService } from './cesstype.service';
import { CesstypeformComponent } from './cesstypeform/cesstypeform.component';

@NgModule({
  declarations: [CesstypeformComponent, CesstypeComponent],
  imports: [
    CommonModule,
    DropdownModule,
    RouterModule,
    CesstypeRoutingModule,
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
    DialogModule
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class CesstypeModule {}
