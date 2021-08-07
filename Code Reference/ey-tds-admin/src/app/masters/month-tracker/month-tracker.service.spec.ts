import { TestBed } from '@angular/core/testing';

import { MonthTrackerService } from './month-tracker.service';

describe('MonthTrackerService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MonthTrackerService = TestBed.get(MonthTrackerService);
    expect(service).toBeTruthy();
  });
});
