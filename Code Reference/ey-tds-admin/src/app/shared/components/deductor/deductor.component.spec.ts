import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { DeductorComponent } from './deductor.component';
import { DeductorService } from './deductor.service';
describe('NatureofpaymentComponent', () => {
  let component: DeductorComponent;
  let fixture: ComponentFixture<DeductorComponent>;
  let service: DeductorService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [DeductorComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(DeductorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(DeductorService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
