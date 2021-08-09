import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { CesstypeComponent } from './cesstype.component';
import { CesstypeService } from './cesstype.service';

describe('CesstypeComponent', () => {
  let component: CesstypeComponent;
  let fixture: ComponentFixture<CesstypeComponent>;
  let service: CesstypeService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [CesstypeComponent],
      [BrowserDynamicTestingModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(CesstypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(CesstypeService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('should expect lenght of array should be equal to data on service call', () => {
  //   service.getCessTypeList().subscribe(
  //     result => {
  //       expect(component.cessTypelist.length).toEqual(result.body.length);
  //     },
  //     error => {
  //       expect(error).toBe(error);
  //     }
  //   );
  // });
  // it('should expect getData function will result should be equal to data on service call', () => {
  //   component.getData();
  // });
});
