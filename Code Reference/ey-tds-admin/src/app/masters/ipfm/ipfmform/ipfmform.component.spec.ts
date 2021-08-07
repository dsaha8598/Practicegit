import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, DebugElement } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { IpfmformComponent } from './ipfmform.component';
import { IpfmService } from '../ipfm.service';
import { By } from '@angular/platform-browser';
describe('IpfmformComponent', () => {
  let component: IpfmformComponent;

  let fixture: ComponentFixture<IpfmformComponent>;
  let de: DebugElement;
  let el: HTMLElement;
  let service: IpfmService;
  beforeEach(async(() => {
    TestBedConfigure.listConfig(
      [IpfmformComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(IpfmformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement.query(By.css('form'));
    service = fixture.debugElement.injector.get(IpfmService);
  });

  it('should be created', async(() => {
    expect(component).toBeTruthy();
  }));

  it('should summited true', async(() => {
    // component.saveAndNewNatureOfPayment();
    // expect(component.submitted).toBeTruthy();
  }));

  it('should call onsumbit method', async(() => {
    // fixture.detectChanges();
    // spyOn(component, 'saveAndNewNatureOfPayment');
    //  el = fixture.debugElement.query(By.css('button')).nativeElement;
    //  el.click();
    // expect(component.paymentForm.valid).toHaveBeenCalledTimes(0);
  }));

  it('form should invaild', () => {
    // component.paymentForm.controls['section'].setValue('');
    // component.paymentForm.controls['natureOfPayment'].setValue('');
    // component.paymentForm.controls['displayValue'].setValue('');
    // component.paymentForm.controls['keywords'].setValue('');
    // component.paymentForm.controls['applicableFrom'].setValue('');
    // component.paymentForm.controls['applicableTo'].setValue('');
    // component.paymentForm.controls['subNatureOfPayment'].setValue('');
    // expect(component.paymentForm.valid).toBeFalsy();
  });

  it('section field validity', () => {
    // let errors = {};
    // let section = component.paymentForm.controls['section'];
    // errors = section.errors || {};
    // component.saveAndNewNatureOfPayment();
    // expect(errors['required']).toBeTruthy();
  });

  it('form should vaild', () => {
    // component.paymentForm.controls['section'].setValue('hello');
    // component.paymentForm.controls['natureOfPayment'].setValue('2123');
    // component.paymentForm.controls['displayValue'].setValue('sdd');
    // component.paymentForm.controls['keywords'].setValue('dasdas');
    // component.paymentForm.controls['applicableFrom'].setValue('22/10/2019');
    // component.paymentForm.controls['applicableTo'].setValue('12/12/2020');
    // component.paymentForm.controls['subNatureOfPayment'].setValue('Yes');
    // expect(component.paymentForm.valid).toBeTruthy();
  });
});
