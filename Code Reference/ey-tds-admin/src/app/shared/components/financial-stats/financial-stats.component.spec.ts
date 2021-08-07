import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FinancialStatsComponent } from './financial-stats.component';

describe('FinancialStatsComponent', () => {
  let component: FinancialStatsComponent;
  let fixture: ComponentFixture<FinancialStatsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [FinancialStatsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FinancialStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
