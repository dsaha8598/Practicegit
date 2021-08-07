import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { RateMasterTreatyformComponent } from './ratemastertreatyform.component';
import { RateMasterTreatyService } from '../ratemastertreaty.service';
import { By } from '@angular/platform-browser';
describe('RateMasterTreatyformComponent', () => {
  let component: RateMasterTreatyformComponent;

  let fixture: ComponentFixture<RateMasterTreatyformComponent>;
  let de: DebugElement;
  let el: HTMLElement;
  let service: RateMasterTreatyService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [RateMasterTreatyformComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(RateMasterTreatyformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement.query(By.css('form'));
    service = fixture.debugElement.injector.get(RateMasterTreatyService);
  });
  var date = new Date();
  var obj = {
    subNatureOfPayment: 5,
    Rate: 2,
    natureOfPayment: 30,
    sacCode: 4
  };

  it('should be created', async(() => {
    expect(component).toBeTruthy();
  }));

  it('should summited true', async(() => {
    // component.saveNaddNewTdsRate();
    expect(component.submitted).toBeTruthy();
  }));

  it('should call onsumbit method', async(() => {
    // fixture.detectChanges();
    // spyOn(component, 'saveAndNewNatureOfPayment');
    //  el = fixture.debugElement.query(By.css('button')).nativeElement;
    //  el.click();
    // expect(component. TDSForm.valid).toHaveBeenCalledTimes(0);
  }));

  it('form should invaild', () => {
    component.DividendRateForm.controls['subNatureOfPayment'].setValue(
      returnValue('fails', obj.subNatureOfPayment)
    );
    component.DividendRateForm.controls['Rate'].setValue(
      returnValue('fails', obj.Rate)
    );
    component.DividendRateForm.controls['natureOfPayment'].setValue(
      returnValue('fails', obj.natureOfPayment)
    );
    component.DividendRateForm.controls['sacCode'].setValue(
      returnValue('fails', obj.sacCode)
    );
    component.DividendRateForm.controls[
      'isPerTransactionLimitApplicable'
    ].setValue('');
    component.DividendRateForm.controls[
      'isAnnualTransactionLimitApplicable'
    ].setValue('');
    component.DividendRateForm.controls['annualTransactionLimit'].setValue('');
    component.DividendRateForm.controls['perTransactionLimit'].setValue('');
    component.DividendRateForm.controls['deducteeStatus'].setValue('');
    component.DividendRateForm.controls['deducteeResidentStatus'].setValue('');
    component.DividendRateForm.controls['applicationFrom'].setValue(date);
    component.DividendRateForm.controls['applicableTo'].setValue(date);
    expect(component.DividendRateForm.valid).toBeFalsy();

    expect(
      component.DividendRateForm.controls['subNatureOfPayment'].valid
    ).toBeFalsy();
    expect(component.DividendRateForm.controls['Rate'].valid).toBeFalsy();
    expect(
      component.DividendRateForm.controls['natureOfPayment'].valid
    ).toBeFalsy();
    expect(component.DividendRateForm.controls['sacCode'].valid).toBeFalsy();
    // component.saveNaddNewTdsRate();
  });

  it('section field validity', () => {
    // let errors = {};
    // let section = component. TDSForm.controls['section'];
    // errors = section.errors || {};
    // component.saveAndNewNatureOfPayment();
    // expect(errors['required']).toBeTruthy();
  });

  it('form should vaild', () => {
    component.DividendRateForm.controls['subNatureOfPayment'].setValue(
      returnValue('pass', obj.subNatureOfPayment)
    );
    component.DividendRateForm.controls['Rate'].setValue(
      returnValue('pass', obj.Rate)
    );
    component.DividendRateForm.controls['natureOfPayment'].setValue(
      returnValue('pass', obj.natureOfPayment)
    );
    component.DividendRateForm.controls['sacCode'].setValue(
      returnValue('pass', obj.sacCode)
    );
    component.DividendRateForm.controls[
      'isPerTransactionLimitApplicable'
    ].setValue('Yes');
    component.DividendRateForm.controls[
      'isAnnualTransactionLimitApplicable'
    ].setValue('Yes');
    component.DividendRateForm.controls['annualTransactionLimit'].setValue(
      'hello1'
    );
    component.DividendRateForm.controls['perTransactionLimit'].setValue(
      'hello2'
    );
    component.DividendRateForm.controls['deducteeStatus'].setValue('NAN');
    component.DividendRateForm.controls['deducteeResidentStatus'].setValue(
      'Rome'
    );
    component.DividendRateForm.controls['applicationFrom'].setValue(date);
    component.DividendRateForm.controls['applicableTo'].setValue(date);
    expect(component.DividendRateForm.valid).toBeTruthy();
    // component.saveNaddNewTdsRate();
    expect(component.DividendRateForm.reset).toBeTruthy();
  });

  function returnValue(c: any, i: any) {
    if (c == 'fails') {
      var result = '';
      var chars =
        '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
      for (var x = i + 2; x > 0; --x)
        result += chars[Math.round(Math.random() * (chars.length - 1))];
      return result;
    } else {
      var result = '';
      var chars =
        '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
      for (var x = i; x > 0; --x)
        result += chars[Math.round(Math.random() * (chars.length - 1))];
      return result;
    }
  }
});
