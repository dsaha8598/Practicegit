import { TestBed, async } from '@angular/core/testing';

import { CesstypeService } from './cesstype.service';
import { TestBedConfigure } from '@app/shared';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CesstypeService', () => {
  beforeEach(async(() => {
    TestBedConfigure.formConfig([], [HttpClientTestingModule], []);
  }));

  // it('should be created', () => {
  //   const service: CesstypeService = TestBed.get(CesstypeService);
  //   expect(service).toBeTruthy();
  // });
});
