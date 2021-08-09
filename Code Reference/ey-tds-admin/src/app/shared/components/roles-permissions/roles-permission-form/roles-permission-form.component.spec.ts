import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RolesPermissionFormComponent } from './roles-permission-form.component';

describe('RolesPermissionFormComponent', () => {
  let component: RolesPermissionFormComponent;
  let fixture: ComponentFixture<RolesPermissionFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RolesPermissionFormComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RolesPermissionFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
