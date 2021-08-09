import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { RadioButtonModule } from 'primeng/radiobutton';
import { DropdownModule } from 'primeng/dropdown';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { MultiSelectModule } from 'primeng/multiselect';
import { SharedModule } from '@app/shared';
import { DialogModule } from 'primeng/dialog';
import { FileUploadModule } from 'primeng/fileupload';
import { DeductorGroupRoutingModule } from './deductor-group-routing.module';
import { DeductorGroupComponent } from './deductor-group.component';
import { DeductorGroupFormComponent } from './deductor-group-form/deductor-group-form.component';
import { SidebarModule } from 'primeng/sidebar';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { DeductorGroupService } from './deductor-groupservice.service';

@NgModule({
  declarations: [DeductorGroupComponent, DeductorGroupFormComponent],
  imports: [
    CommonModule,
    NgMultiSelectDropDownModule.forRoot(),
    SidebarModule,
    CommonModule,
    ReactiveFormsModule,
    DropdownModule,
    RouterModule,
    CalendarModule,
    TableModule,
    RadioButtonModule,
    BreadcrumbModule,
    MessagesModule,
    MessageModule,
    SplitButtonModule,
    TabViewModule,
    MultiSelectModule,
    FormsModule,
    SharedModule,
    DialogModule,
    SidebarModule,
    FileUploadModule,
    DeductorGroupRoutingModule
  ],
  providers: [DeductorGroupService]
})
export class DeductorGroupModule {}
