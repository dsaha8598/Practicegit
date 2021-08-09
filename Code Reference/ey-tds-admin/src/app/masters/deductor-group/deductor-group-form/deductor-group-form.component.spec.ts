import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeductorGroupFormComponent } from './deductor-group-form.component';

describe('DeductorGroupFormComponent', () => {
  let component: DeductorGroupFormComponent;
  let fixture: ComponentFixture<DeductorGroupFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeductorGroupFormComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeductorGroupFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
