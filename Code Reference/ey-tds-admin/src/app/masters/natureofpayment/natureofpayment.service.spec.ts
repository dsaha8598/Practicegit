import { TestBed, async } from '@angular/core/testing';

import { NatureofpaymentService } from './natureofpayment.service';
import { TestBedConfigure } from '@app/shared';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('NatureofpaymentService', () => {
  beforeEach(async(() => {
    TestBedConfigure.formConfig([], [HttpClientTestingModule], []);
  }));
});
