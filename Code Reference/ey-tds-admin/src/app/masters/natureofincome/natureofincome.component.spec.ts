import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { NatureofcollectionComponent } from './natureofcollection.component';
import { NatureofcollectionService } from './natureofcollection.service';
describe('NatureofcollectionComponent', () => {
  let component: NatureofcollectionComponent;
  let fixture: ComponentFixture<NatureofcollectionComponent>;
  let service: NatureofcollectionService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [NatureofcollectionComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(NatureofcollectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(NatureofcollectionService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
