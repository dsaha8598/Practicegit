import { TestBed } from '@angular/core/testing';

import { TdsrateService } from './tdsrate.service';

describe('TdsrateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TdsrateService = TestBed.get(TdsrateService);
    expect(service).toBeTruthy();
  });
});
