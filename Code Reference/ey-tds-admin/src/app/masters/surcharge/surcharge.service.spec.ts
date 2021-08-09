import { TestBed } from '@angular/core/testing';

import { SurchargeService } from './surcharge.service';

describe('SurchargeService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SurchargeService = TestBed.get(SurchargeService);
    expect(service).toBeTruthy();
  });
});
