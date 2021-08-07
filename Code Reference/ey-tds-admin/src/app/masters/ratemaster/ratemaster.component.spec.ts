import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { RatemasterComponent } from './ratemaster.component';
import { RatemasterService } from './ratemaster.service';
describe('TdsrateComponent', () => {
  let component: RatemasterComponent;
  let fixture: ComponentFixture<RatemasterComponent>;
  let service: RatemasterService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [RatemasterComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(RatemasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(RatemasterService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
