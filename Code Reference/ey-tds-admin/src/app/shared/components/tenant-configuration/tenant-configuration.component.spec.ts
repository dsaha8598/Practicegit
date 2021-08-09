import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TenantConfigurationComponent } from './tenant-configuration.component';

describe('TenantConfigurationComponent', () => {
  let component: TenantConfigurationComponent;
  let fixture: ComponentFixture<TenantConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [TenantConfigurationComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TenantConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
