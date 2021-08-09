import {
  NgModule,
  CUSTOM_ELEMENTS_SCHEMA,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleRoutingModule } from './article-routing.module';
import { ArticleComponent } from './article.component';
import { ArticleformComponent } from './articleform/articleform.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TableModule } from 'primeng/table';
import { CalendarModule } from 'primeng/calendar';
import { RadioButtonModule } from 'primeng/radiobutton';
import { DropdownModule } from 'primeng/dropdown';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { SharedModule } from '@app/shared';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { TokenInterceptor } from '../../core/http/interceptor';
import { ArticleService } from './article.service';
import { DialogModule } from 'primeng/dialog';

@NgModule({
  declarations: [ArticleComponent, ArticleformComponent],
  imports: [
    CommonModule,
    ArticleRoutingModule,
    ReactiveFormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    RadioButtonModule,
    DropdownModule,
    BreadcrumbModule,
    SharedModule,
    MultiSelectModule,
    SplitButtonModule,
    TabViewModule,
    FormsModule,
    RouterModule,
    TableModule,
    CalendarModule,
    RadioButtonModule,
    BreadcrumbModule,
    SharedModule,
    DialogModule
  ],

  // providers: [
  //   ArticleService,
  //   { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  // ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA]
})
export class ArticleModule {}
