import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
//import { TreeNode } from '@angular/router/src/utils/tree';
import { AlertServiceInterceptor } from '@app/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';
import { TagInputModule } from 'ngx-chips';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { CalendarModule } from 'primeng/calendar';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { FileUploadModule } from 'primeng/fileupload';
import { MenuModule } from 'primeng/menu';
import { MultiSelectModule } from 'primeng/multiselect';
import { OrganizationChartModule } from 'primeng/organizationchart';
import { ProgressBarModule } from 'primeng/progressbar';
import { RadioButtonModule } from 'primeng/radiobutton';
import { SidebarModule } from 'primeng/sidebar';
import { SplitButtonModule } from 'primeng/splitbutton';
import { StepsModule } from 'primeng/steps';
import { TableModule } from 'primeng/table';
import { TabViewModule } from 'primeng/tabview';
import { AlertComponent } from './components/alert/alert.component';
import { BatchUploadTableComponent } from './components/batch-upload-table/batch-upload-table.component';
import { BreadcrumbsComponent } from './components/breadcrumbs/breadcrumbs.component';
import { DeductorComponent } from './components/deductor/deductor.component';
import { DeductorformComponent } from './components/deductorform/deductorform.component';
import { DownloadTemplateComponent } from './components/download-template/download-template.component';
import { EmptystateComponent } from './components/emptystate/emptystate.component';
import { FileUploaderComponent } from './components/file-uploader/file-uploader.component';
import { FinancialStatsComponent } from './components/financial-stats/financial-stats.component';
import { FooterComponent } from './components/footer/footer.component';
import { MonthDropdownComponent } from './components/month-dropdown/month-dropdown.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { NavpillsComponent } from './components/navpills/navpills.component';
import { OnboardingComponent } from './components/onboarding/onboarding.component';
import { OnboardingComponentTcs } from './components/onboarding-tcs/onboarding-tcs.component';
import { RolesPermissionFormComponent } from './components/roles-permissions/roles-permission-form/roles-permission-form.component';
import { RolesPermissionsComponent } from './components/roles-permissions/roles-permissions.component';
import { RolesPermissionTcsFormComponent } from './components/roles-permissions-tcs/roles-permission-tcs-form/roles-permission-tcs-form.component';
import { RolesPermissionsTcsComponent } from './components/roles-permissions-tcs/roles-permissions-tcs.component';
import { StatusviewComponent } from './components/statusview/statusview.component';
import { TenantConfigurationComponent } from './components/tenant-configuration/tenant-configuration.component';
import { TileComponent } from './components/tile/tile.component';
import { UsersComponent } from './components/users/users.component';
import { YearDropdownComponent } from './components/year-dropdown/year-dropdown.component';
import { ConvertlocalamountPipe } from './convertlocalamount.pipe';
import { HasAnyAuthorityDirective } from './directives/hasauthority.directive';
import { NumberlengthvalidatorDirective } from './directives/numberlengthvalidator.directive';
import { RatedecimalfloterDirective } from './directives/ratedecimalfloter.directive';
import { JsonbeautifierPipe } from './jsonbeautifier.pipe';
import { LoaderComponent } from './loader/loader.component';
import { PaginationComponent } from './pagination/pagination.component';
import { SafePipePipe } from './safe-pipe.pipe';
import { RedirectToRouteComponent } from './components/redirect-to-route/redirect-to-route.component';
import { DownloadLinkComponent } from './components/download-link/download-link.component';
import { CustomtransformationComponent } from './components/customtransformation/customtransformation.component';
import { DownloadKeywordsComponent } from './components/download-keywords/download-keywords.component';
import { CustomCheckboxComponent } from './components/custom-checkbox/custom-checkbox.component';
import { DownloadReportComponent } from './components/download-report/download-report.component';
import { ToprecisionPipe } from './toprecision.pipe';
import { DownloadNOPComponent } from './components/download-nop/download-nop.component';
import { DownloadFilingComponent } from './components/download-filing/download-filing.component';
import { RateInputFieldComponent } from './components/rate-input-field/rate-input-field.component';
import { Onboarding26ASComponent } from './components/onboarding26-as/onboarding26-as.component';
@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ProgressBarModule,
    BreadcrumbModule,
    NgbModule,
    DialogModule,
    DropdownModule,
    ReactiveFormsModule,
    FormsModule,
    MenuModule,
    ReactiveFormsModule,
    RouterModule,
    CalendarModule,
    TableModule,
    RadioButtonModule,
    DropdownModule,
    BreadcrumbModule,
    StepsModule,
    SplitButtonModule,
    OrganizationChartModule,
    TabViewModule,
    MultiSelectModule,
    FormsModule,
    NgMultiSelectDropDownModule.forRoot(),
    DialogModule,
    SidebarModule,
    FileUploadModule,
    StepsModule,
    CheckboxModule,
    TagInputModule
  ],
  declarations: [
    LoaderComponent,
    NavbarComponent,
    NavpillsComponent,
    TileComponent,
    RolesPermissionsComponent,
    RolesPermissionFormComponent,
    RolesPermissionsTcsComponent,
    RolesPermissionTcsFormComponent,
    StatusviewComponent,
    BreadcrumbsComponent,
    AlertComponent,
    NumberlengthvalidatorDirective,
    HasAnyAuthorityDirective,
    FinancialStatsComponent,
    BatchUploadTableComponent,
    ConvertlocalamountPipe,
    EmptystateComponent,
    DownloadNOPComponent,
    BatchUploadTableComponent,
    OnboardingComponent,
    OnboardingComponentTcs,
    TenantConfigurationComponent,
    UsersComponent,
    FooterComponent,
    DownloadTemplateComponent,
    MonthDropdownComponent,
    YearDropdownComponent,
    JsonbeautifierPipe,
    FileUploaderComponent,
    RatedecimalfloterDirective,
    RateInputFieldComponent,
    SafePipePipe,
    DeductorComponent,
    DeductorformComponent,
    PaginationComponent,
    RedirectToRouteComponent,
    DownloadLinkComponent,
    CustomtransformationComponent,
    DownloadKeywordsComponent,
    CustomCheckboxComponent,
    DownloadReportComponent,
    ToprecisionPipe,
    DownloadFilingComponent,
    Onboarding26ASComponent
  ],
  exports: [
    DownloadReportComponent,
    CustomCheckboxComponent,
    LoaderComponent,
    NavbarComponent,
    NavpillsComponent,
    TileComponent,
    BreadcrumbsComponent,
    NgbModule,
    AlertComponent,
    LoaderComponent,
    RatedecimalfloterDirective,
    NumberlengthvalidatorDirective,
    HasAnyAuthorityDirective,
    FinancialStatsComponent,
    ConvertlocalamountPipe,
    BatchUploadTableComponent,
    EmptystateComponent,
    BatchUploadTableComponent,
    OnboardingComponent,
    DownloadNOPComponent,
    RolesPermissionsComponent,
    RolesPermissionFormComponent,
    RolesPermissionsTcsComponent,
    RolesPermissionTcsFormComponent,
    TenantConfigurationComponent,
    UsersComponent,
    FooterComponent,
    DownloadTemplateComponent,
    MonthDropdownComponent,
    YearDropdownComponent,
    JsonbeautifierPipe,
    FileUploaderComponent,
    RateInputFieldComponent,
    SafePipePipe,
    ToprecisionPipe,
    DeductorComponent,
    DeductorformComponent,
    PaginationComponent,
    RedirectToRouteComponent,
    DownloadLinkComponent,
    DownloadFilingComponent,
    DownloadKeywordsComponent
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class SharedModule {}
