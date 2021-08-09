import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthTrackerFormComponent } from './month-tracker-form.component';

describe('MonthTrackerFormComponent', () => {
  let component: MonthTrackerFormComponent;
  let fixture: ComponentFixture<MonthTrackerFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MonthTrackerFormComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthTrackerFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
