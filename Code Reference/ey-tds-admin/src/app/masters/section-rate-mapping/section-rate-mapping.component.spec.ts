import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SectionRateMappingComponent } from './section-rate-mapping.component';

describe('SectionRateMappingComponent', () => {
  let component: SectionRateMappingComponent;
  let fixture: ComponentFixture<SectionRateMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SectionRateMappingComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SectionRateMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
