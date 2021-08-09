import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionRateMappingTCSComponent } from './section-rate-mapping-tcs.component';

describe('SectionRateMappingTCSComponent', () => {
  let component: SectionRateMappingTCSComponent;
  let fixture: ComponentFixture<SectionRateMappingTCSComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SectionRateMappingTCSComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SectionRateMappingTCSComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
