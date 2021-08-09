import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusviewComponent } from './statusview.component';

describe('StatusviewComponent', () => {
  let component: StatusviewComponent;
  let fixture: ComponentFixture<StatusviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [StatusviewComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
