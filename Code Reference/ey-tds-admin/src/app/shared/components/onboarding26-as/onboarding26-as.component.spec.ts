import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Onboarding26ASComponent } from './onboarding26-as.component';

describe('Onboarding26ASComponent', () => {
  let component: Onboarding26ASComponent;
  let fixture: ComponentFixture<Onboarding26ASComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [Onboarding26ASComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Onboarding26ASComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
