import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CessformComponent } from './cessform.component';

describe('CessformComponent', () => {
  let component: CessformComponent;
  let fixture: ComponentFixture<CessformComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CessformComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CessformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
