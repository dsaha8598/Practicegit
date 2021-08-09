import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { RateMasterActComponent } from './ratemasteract.component';
import { RateMasterActService } from './ratemasteract.service';
describe('RateMasterActComponent', () => {
  let component: RateMasterActComponent;
  let fixture: ComponentFixture<RateMasterActComponent>;
  let service: RateMasterActService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [RateMasterActComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(RateMasterActComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(RateMasterActService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
