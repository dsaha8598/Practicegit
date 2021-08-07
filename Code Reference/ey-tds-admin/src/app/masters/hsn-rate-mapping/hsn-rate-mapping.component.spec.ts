import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HsnRateMappingComponent } from './hsn-rate-mapping.component';

describe('HsnRateMappingComponent', () => {
  let component: HsnRateMappingComponent;
  let fixture: ComponentFixture<HsnRateMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [HsnRateMappingComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HsnRateMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
