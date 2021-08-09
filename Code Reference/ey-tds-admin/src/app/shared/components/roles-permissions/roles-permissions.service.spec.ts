import { TestBed } from '@angular/core/testing';

import { RolesPermissionsService } from './roles-permissions.service';

describe('RolesPermissionsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RolesPermissionsService = TestBed.get(
      RolesPermissionsService
    );
    expect(service).toBeTruthy();
  });
});
