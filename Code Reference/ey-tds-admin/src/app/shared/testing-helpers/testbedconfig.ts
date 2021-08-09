import { TestBed } from '@angular/core/testing';
import { BrowserModule } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';
import { CalendarModule } from 'primeng/calendar';
import { RadioButtonModule } from 'primeng/radiobutton';
import { BreadcrumbModule } from 'primeng/breadcrumb';
// import { SharedModule } from '@app/shared';
import { MultiSelectModule } from 'primeng/multiselect';
import { SplitButtonModule } from 'primeng/splitbutton';
import { TabViewModule } from 'primeng/tabview';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
export class TestBedConfigure {
  public static formConfig(
    declarations: any[],
    imports: any[],
    providers: any[]
  ) {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        BrowserModule,
        RouterTestingModule,
        CalendarModule,
        RadioButtonModule,
        BreadcrumbModule,
        MultiSelectModule,
        SplitButtonModule,
        TabViewModule,
        // SharedModule,
        FormsModule
      ].concat(imports),
      declarations: declarations,
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
      providers: [].concat(providers)
    }).compileComponents();
  }

  public static listConfig(
    declarations: any[],
    imports: any[],
    providers: any[]
  ) {
    TestBed.configureTestingModule({
      imports: [
        BrowserModule,
        RouterTestingModule,
        TableModule,
        PaginatorModule,
        ReactiveFormsModule,
        CalendarModule
      ].concat(imports),
      declarations: declarations,
      schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
      providers: [].concat(providers)
    }).compileComponents();
  }
  public static getCurrentDate() {
    const currentDate = new Date();
    return currentDate.toISOString().substring(0, 10);
  }
}
