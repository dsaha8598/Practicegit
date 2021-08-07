import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { RateMasterTreatyComponent } from './ratemastertreaty.component';
import { RateMasterTreatyService } from './ratemastertreaty.service';
describe('RateMasterTreatyComponent', () => {
  let component: RateMasterTreatyComponent;
  let fixture: ComponentFixture<RateMasterTreatyComponent>;
  let service: RateMasterTreatyService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [RateMasterTreatyComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(RateMasterTreatyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(RateMasterTreatyService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
