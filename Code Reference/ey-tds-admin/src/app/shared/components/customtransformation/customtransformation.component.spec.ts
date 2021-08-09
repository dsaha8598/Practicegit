import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomtransformationComponent } from './customtransformation.component';

describe('CustomtransformationComponent', () => {
  let component: CustomtransformationComponent;
  let fixture: ComponentFixture<CustomtransformationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CustomtransformationComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomtransformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
