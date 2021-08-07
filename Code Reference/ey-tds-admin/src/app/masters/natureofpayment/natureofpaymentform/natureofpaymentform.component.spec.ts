import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { NatureofpaymentformComponent } from './natureofpaymentform.component';
import { NatureofpaymentService } from '../natureofpayment.service';
import { By } from '@angular/platform-browser';
import { AnyARecord } from 'dns';

describe('NatureofpaymentformComponent', () => {
  let component: NatureofpaymentformComponent;

  let fixture: ComponentFixture<NatureofpaymentformComponent>;
  let de: DebugElement;
  let el: HTMLElement;
  let service: NatureofpaymentService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [NatureofpaymentformComponent],
      [BrowserDynamicTestingModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(NatureofpaymentformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement.query(By.css('form'));
    service = fixture.debugElement.injector.get(NatureofpaymentService);
  });
  var date = new Date();
  var obj = {
    section: 9,
    natureOfPayment: 120,
    displayValue: 10,
    keywords: 30,
    applicableFrom: date,
    applicableTo: date,
    subNatureOfPayment: 'Yes'
  };

  it('should be created', async(() => {
    expect(component).toBeTruthy();
  }));

  it('should summited true', async(() => {
    component.saveAndNewNatureOfPayment();
    expect(component.submitted).toBeTruthy();
  }));

  // it('should call onsumbit method', async(() => {
  //   fixture.detectChanges();
  //   spyOn(component, 'saveAndNewNatureOfPayment');
  //    el = fixture.debugElement.query(By.css('button')).nativeElement;
  //    el.click();
  //   expect(component.paymentForm.valid).toHaveBeenCalledTimes(0);
  // }));

  it('form should invaild', () => {
    component.paymentForm.controls['section'].setValue('');
    component.paymentForm.controls['natureOfPayment'].setValue('');
    component.paymentForm.controls['displayValue'].setValue('');
    component.paymentForm.controls['keywords'].setValue('');
    component.paymentForm.controls['applicableFrom'].setValue('');
    component.paymentForm.controls['applicableTo'].setValue('');
    component.paymentForm.controls['subNatureOfPayment'].setValue('');
    expect(component.paymentForm.valid).toBeFalsy();
  });

  it('section field validity', () => {
    let errors = {};
    let section = component.paymentForm.controls['section'];
    errors = section.errors || {};
    component.saveAndNewNatureOfPayment();
    expect(errors['required']).toBeTruthy();
  });

  it('should fail if section(109) ,natureOfPayment(120),displayValue(10),keywords(30) greater then there declared value', async(() => {
    component.paymentForm.controls['section'].setValue(
      returnValue('fails', obj.section)
    );
    component.paymentForm.controls['natureOfPayment'].setValue(
      returnValue('fails', obj.natureOfPayment)
    );
    component.paymentForm.controls['displayValue'].setValue(
      returnValue('fails', obj.displayValue)
    );
    component.paymentForm.controls['keywords'].setValue(
      returnValue('fails', obj.keywords)
    );
    component.paymentForm.controls['applicableFrom'].setValue(date);
    component.paymentForm.controls['applicableTo'].setValue(date);
    component.paymentForm.controls['subNatureOfPayment'].setValue('Yes');
    expect(component.paymentForm.valid).toBeFalsy();
    expect(component.paymentForm.controls['section'].valid).toBeFalsy();
    expect(component.paymentForm.controls['natureOfPayment'].valid).toBeFalsy();
    expect(component.paymentForm.controls['displayValue'].valid).toBeFalsy();
    expect(component.paymentForm.controls['keywords'].valid).toBeFalsy();
    component.saveAndNewNatureOfPayment();
  }));

  it('form should vaild', () => {
    component.paymentForm.controls['section'].setValue(
      returnValue('pass', obj.section)
    );
    component.paymentForm.controls['natureOfPayment'].setValue(
      returnValue('pass', obj.natureOfPayment)
    );
    component.paymentForm.controls['displayValue'].setValue(
      returnValue('pass', obj.displayValue)
    );
    component.paymentForm.controls['keywords'].setValue(
      returnValue('pass', obj.keywords)
    );
    component.paymentForm.controls['applicableFrom'].setValue(date);
    component.paymentForm.controls['applicableTo'].setValue(date);
    component.paymentForm.controls['subNatureOfPayment'].setValue('Yes');
    expect(component.paymentForm.valid).toBeTruthy();
    component.saveAndNewNatureOfPayment();
    expect(component.paymentForm.reset).toBeTruthy();
  });

  it('form valid with save click and routes channge', () => {
    component.paymentForm.controls['section'].setValue(
      returnValue('pass', obj.section)
    );
    component.paymentForm.controls['natureOfPayment'].setValue(
      returnValue('pass', obj.natureOfPayment)
    );
    component.paymentForm.controls['displayValue'].setValue(
      returnValue('pass', obj.displayValue)
    );
    component.paymentForm.controls['keywords'].setValue(
      returnValue('pass', obj.keywords)
    );
    component.paymentForm.controls['applicableFrom'].setValue(date);
    component.paymentForm.controls['applicableTo'].setValue(date);
    component.paymentForm.controls['subNatureOfPayment'].setValue('Yes');
    expect(component.paymentForm.valid).toBeTruthy();
    component.saveNatureOfPayment();
    expect(component.paymentForm.reset).toBeTruthy();
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
