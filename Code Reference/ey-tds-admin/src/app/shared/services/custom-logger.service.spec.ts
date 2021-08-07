import { TestBed } from '@angular/core/testing';

import { CustomLoggerService } from './custom-logger.service';

describe('CustomLoggerService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CustomLoggerService = TestBed.get(CustomLoggerService);
    expect(service).toBeTruthy();
  });
});
