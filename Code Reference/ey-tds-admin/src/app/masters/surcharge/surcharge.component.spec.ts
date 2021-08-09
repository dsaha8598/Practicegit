// import { async, ComponentFixture, TestBed } from '@angular/core/testing';

// import { SurchargeComponent } from './surcharge.component';

// describe('SurchargeComponent', () => {
//   let component: SurchargeComponent;
//   let fixture: ComponentFixture<SurchargeComponent>;

//   beforeEach(async(() => {
//     TestBed.configureTestingModule({
//       declarations: [SurchargeComponent]
//     }).compileComponents();
//   }));

//   beforeEach(() => {
//     fixture = TestBed.createComponent(SurchargeComponent);
//     component = fixture.componentInstance;
//     fixture.detectChanges();
//   });

//   it('should create', () => {
//     expect(component).toBeTruthy();
//   });
// });
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { SurchargeComponent } from './surcharge.component';
import { SurchargeService } from './surcharge.service';
describe('SurchargeComponent', () => {
  let component: SurchargeComponent;
  let fixture: ComponentFixture<SurchargeComponent>;
  let service: SurchargeService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [SurchargeComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(SurchargeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    service = fixture.debugElement.injector.get(SurchargeService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
