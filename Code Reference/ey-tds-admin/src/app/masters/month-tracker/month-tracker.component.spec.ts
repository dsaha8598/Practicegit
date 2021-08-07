import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthTrackerComponent } from './month-tracker.component';

describe('MonthTrackerComponent', () => {
  let component: MonthTrackerComponent;
  let fixture: ComponentFixture<MonthTrackerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MonthTrackerComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthTrackerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
