import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { TdsrateComponent } from './tdsrate.component';
import { TdsrateService } from './tdsrate.service';
describe('TdsrateComponent', () => {
  let component: TdsrateComponent;
  let fixture: ComponentFixture<TdsrateComponent>;
  let service: TdsrateService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [TdsrateComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(TdsrateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(TdsrateService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
