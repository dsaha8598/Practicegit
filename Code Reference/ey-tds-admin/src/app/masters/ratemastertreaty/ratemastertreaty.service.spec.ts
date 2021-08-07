import { TestBed } from '@angular/core/testing';

import { RateMasterTreatyService } from './ratemastertreaty.service';

describe('DividendrateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RateMasterTreatyService = TestBed.get(
      RateMasterTreatyService
    );
    expect(service).toBeTruthy();
  });
});
