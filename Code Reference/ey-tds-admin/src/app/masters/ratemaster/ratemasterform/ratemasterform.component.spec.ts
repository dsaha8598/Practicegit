import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { RatemasterformComponent } from './ratemasterform.component';
import { RatemasterService } from '../ratemaster.service';
import { By } from '@angular/platform-browser';
describe('TdsrateformComponent', () => {
  let component: RatemasterformComponent;

  let fixture: ComponentFixture<RatemasterformComponent>;
  let de: DebugElement;
  let el: HTMLElement;
  let service: RatemasterService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [RatemasterformComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(RatemasterformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement.query(By.css('form'));
    service = fixture.debugElement.injector.get(RatemasterService);
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
    component.saveNaddNewTdsRate();
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
    component.TDSForm.controls['subNatureOfPayment'].setValue(
      returnValue('fails', obj.subNatureOfPayment)
    );
    component.TDSForm.controls['Rate'].setValue(returnValue('fails', obj.Rate));
    component.TDSForm.controls['natureOfPayment'].setValue(
      returnValue('fails', obj.natureOfPayment)
    );
    component.TDSForm.controls['sacCode'].setValue(
      returnValue('fails', obj.sacCode)
    );
    component.TDSForm.controls['isPerTransactionLimitApplicable'].setValue('');
    component.TDSForm.controls['isAnnualTransactionLimitApplicable'].setValue(
      ''
    );
    component.TDSForm.controls['annualTransactionLimit'].setValue('');
    component.TDSForm.controls['perTransactionLimit'].setValue('');
    component.TDSForm.controls['deducteeStatus'].setValue('');
    component.TDSForm.controls['deducteeResidentStatus'].setValue('');
    component.TDSForm.controls['applicationFrom'].setValue(date);
    component.TDSForm.controls['applicableTo'].setValue(date);
    expect(component.TDSForm.valid).toBeFalsy();

    expect(component.TDSForm.controls['subNatureOfPayment'].valid).toBeFalsy();
    expect(component.TDSForm.controls['Rate'].valid).toBeFalsy();
    expect(component.TDSForm.controls['natureOfPayment'].valid).toBeFalsy();
    expect(component.TDSForm.controls['sacCode'].valid).toBeFalsy();
    component.saveNaddNewTdsRate();
  });

  it('section field validity', () => {
    // let errors = {};
    // let section = component. TDSForm.controls['section'];
    // errors = section.errors || {};
    // component.saveAndNewNatureOfPayment();
    // expect(errors['required']).toBeTruthy();
  });

  it('form should vaild', () => {
    component.TDSForm.controls['subNatureOfPayment'].setValue(
      returnValue('pass', obj.subNatureOfPayment)
    );
    component.TDSForm.controls['Rate'].setValue(returnValue('pass', obj.Rate));
    component.TDSForm.controls['natureOfPayment'].setValue(
      returnValue('pass', obj.natureOfPayment)
    );
    component.TDSForm.controls['sacCode'].setValue(
      returnValue('pass', obj.sacCode)
    );
    component.TDSForm.controls['isPerTransactionLimitApplicable'].setValue(
      'Yes'
    );
    component.TDSForm.controls['isAnnualTransactionLimitApplicable'].setValue(
      'Yes'
    );
    component.TDSForm.controls['annualTransactionLimit'].setValue('hello1');
    component.TDSForm.controls['perTransactionLimit'].setValue('hello2');
    component.TDSForm.controls['deducteeStatus'].setValue('NAN');
    component.TDSForm.controls['deducteeResidentStatus'].setValue('Rome');
    component.TDSForm.controls['applicationFrom'].setValue(date);
    component.TDSForm.controls['applicableTo'].setValue(date);
    expect(component.TDSForm.valid).toBeTruthy();
    component.saveNaddNewTdsRate();
    expect(component.TDSForm.reset).toBeTruthy();
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
