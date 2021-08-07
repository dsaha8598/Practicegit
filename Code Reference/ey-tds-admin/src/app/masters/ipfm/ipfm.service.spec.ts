import { TestBed, async } from '@angular/core/testing';
import { IpfmService } from './ipfm.service';
import { TestBedConfigure } from '@app/shared';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('IpfmService', () => {
  beforeEach(async(() => {
    TestBedConfigure.formConfig([], [HttpClientTestingModule], []);
  }));
});
