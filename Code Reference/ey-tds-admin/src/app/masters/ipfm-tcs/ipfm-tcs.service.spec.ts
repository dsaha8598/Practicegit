import { TestBed, async } from '@angular/core/testing';
import { IpfmTcsService } from './ipfm-tcs.service';
import { TestBedConfigure } from '@app/shared';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('IpfmTcsService', () => {
  beforeEach(async(() => {
    TestBedConfigure.formConfig([], [HttpClientTestingModule], []);
  }));
});
