import { TestBed } from '@angular/core/testing';

import { CessService } from './cess.service';

describe('CessService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CessService = TestBed.get(CessService);
    expect(service).toBeTruthy();
  });
});
