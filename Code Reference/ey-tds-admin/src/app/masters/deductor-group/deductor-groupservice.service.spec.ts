import { TestBed } from '@angular/core/testing';

import { DeductorGroupserviceService } from './deductor-groupservice.service';

describe('DeductorGroupserviceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: DeductorGroupserviceService = TestBed.get(
      DeductorGroupserviceService
    );
    expect(service).toBeTruthy();
  });
});
