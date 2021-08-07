import { TestBed, async } from '@angular/core/testing';
import { NatureofcollectionService } from './natureofcollection.service';
import { TestBedConfigure } from '@app/shared';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('NatureofcollectionService', () => {
  beforeEach(async(() => {
    TestBedConfigure.formConfig([], [HttpClientTestingModule], []);
  }));
});
