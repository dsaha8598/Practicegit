import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrencyConvertorMasterComponent } from './currency-convertor-master.component';

describe('CurrencyConvertorMasterComponent', () => {
  let component: CurrencyConvertorMasterComponent;
  let fixture: ComponentFixture<CurrencyConvertorMasterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CurrencyConvertorMasterComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CurrencyConvertorMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
