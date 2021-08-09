import { TestBed } from '@angular/core/testing';

import { CurrencyConvertorMasterService } from './currency-convertor-master.service';

describe('CurrencyConvertorMasterService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CurrencyConvertorMasterService = TestBed.get(
      CurrencyConvertorMasterService
    );
    expect(service).toBeTruthy();
  });
});
