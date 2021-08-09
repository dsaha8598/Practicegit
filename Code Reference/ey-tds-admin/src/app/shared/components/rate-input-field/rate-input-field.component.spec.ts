import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RateInputFieldComponent } from './rate-input-field.component';

describe('RateInputFieldComponent', () => {
  let component: RateInputFieldComponent;
  let fixture: ComponentFixture<RateInputFieldComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RateInputFieldComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RateInputFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
