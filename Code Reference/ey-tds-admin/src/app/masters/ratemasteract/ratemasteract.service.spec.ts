import { TestBed } from '@angular/core/testing';

import { RateMasterActService } from './ratemasteract.service';

describe('DividendrateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RateMasterActService = TestBed.get(RateMasterActService);
    expect(service).toBeTruthy();
  });
});
