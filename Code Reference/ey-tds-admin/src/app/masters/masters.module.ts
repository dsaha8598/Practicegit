import { CommonModule } from '@angular/common';
import {
  CUSTOM_ELEMENTS_SCHEMA,
  NgModule,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '@app/shared';
import { MastersRoutingModule } from './masters-routing.module';
import { MastersComponent } from './masters.component';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { TabViewModule } from 'primeng/tabview';
import { TableModule } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { SplitButtonModule } from 'primeng/splitbutton';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SacDescriptionsComponent } from './sac-descriptions/sac-descriptions.component';
import { SacDescriptionsTcsComponent } from './sac-descriptions-tcs/sac-descriptions-tcs.component';
import { SectionRateMappingComponent } from './section-rate-mapping/section-rate-mapping.component';
import { HsnRateMappingComponent } from './hsn-rate-mapping/hsn-rate-mapping.component';
import { SectionRateMappingService } from './section-rate-mapping/section-rate-mapping.service';
import { HSNRateMappingService } from './hsn-rate-mapping/hsn-rate-mapping.service';
//import { HSNApplicationMastersComponent } from './hsn-application-masters/hsn-application-masters.component';
import { SectionRateMappingTCSComponent } from './section-rate-mapping-tcs/section-rate-mapping-tcs.component';
import { ExemptionsListComponent } from './exemptions-list/exemptions-list.component';
@NgModule({
  declarations: [
    MastersComponent,
    SacDescriptionsComponent,
    SacDescriptionsTcsComponent,
    SectionRateMappingComponent,
    SectionRateMappingTCSComponent,
    HsnRateMappingComponent,
    //  HSNApplicationMastersComponent,
    ExemptionsListComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    RouterModule,
    SplitButtonModule,
    MastersRoutingModule,
    ConfirmDialogModule,
    MultiSelectModule,
    BreadcrumbModule,
    TableModule,
    DropdownModule,
    FormsModule,
    ReactiveFormsModule,
    DialogModule,
    SharedModule,
    TabViewModule
  ],
  providers: [SectionRateMappingService, HSNRateMappingService],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class MastersModule {}
