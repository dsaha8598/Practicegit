import { TestBed } from '@angular/core/testing';

import { PagenationService } from './pagenation.service';

describe('PagenationService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: PagenationService = TestBed.get(PagenationService);
    expect(service).toBeTruthy();
  });
});
