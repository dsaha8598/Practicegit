import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RedirectToRouteComponent } from './redirect-to-route.component';

describe('RedirectToRouteComponent', () => {
  let component: RedirectToRouteComponent;
  let fixture: ComponentFixture<RedirectToRouteComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RedirectToRouteComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RedirectToRouteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
