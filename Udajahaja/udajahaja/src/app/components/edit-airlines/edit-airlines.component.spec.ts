import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditAirlinesComponent } from './edit-airlines.component';

describe('EditAirlinesComponent', () => {
  let component: EditAirlinesComponent;
  let fixture: ComponentFixture<EditAirlinesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditAirlinesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditAirlinesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
