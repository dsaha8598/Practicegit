import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeductorGroupComponent } from './deductor-group.component';

describe('DeductorGroupComponent', () => {
  let component: DeductorGroupComponent;
  let fixture: ComponentFixture<DeductorGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeductorGroupComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeductorGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
