import { TestBed } from '@angular/core/testing';
import { RatemasterService } from './ratemaster.service';

describe('TdsrateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RatemasterService = TestBed.get(RatemasterService);
    expect(service).toBeTruthy();
  });
});
