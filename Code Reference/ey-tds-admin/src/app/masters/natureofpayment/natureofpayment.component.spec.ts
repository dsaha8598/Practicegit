import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { NatureofpaymentComponent } from './natureofpayment.component';
import { NatureofpaymentService } from './natureofpayment.service';
describe('NatureofpaymentComponent', () => {
  let component: NatureofpaymentComponent;
  let fixture: ComponentFixture<NatureofpaymentComponent>;
  let service: NatureofpaymentService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [NatureofpaymentComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(NatureofpaymentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(NatureofpaymentService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
