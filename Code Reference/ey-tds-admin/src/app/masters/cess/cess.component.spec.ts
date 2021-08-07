import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CessComponent } from './cess.component';

describe('CessComponent', () => {
  let component: CessComponent;
  let fixture: ComponentFixture<CessComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CessComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
