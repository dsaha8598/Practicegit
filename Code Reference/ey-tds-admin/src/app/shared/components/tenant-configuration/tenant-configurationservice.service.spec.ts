import { TestBed } from '@angular/core/testing';
import { TenantConfigurationService } from './tenant-configurationservice.service';

describe('TenantConfigurationserviceService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TenantConfigurationService = TestBed.get(
      TenantConfigurationService
    );
    expect(service).toBeTruthy();
  });
});
