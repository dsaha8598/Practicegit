import { TestBed } from '@angular/core/testing';

import { SectionRateMappingService } from './section-rate-mapping.service';

describe('SectionRateMappingService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SectionRateMappingService = TestBed.get(
      SectionRateMappingService
    );
    expect(service).toBeTruthy();
  });
});
