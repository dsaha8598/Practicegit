import { HttpClient, HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, DebugElement } from '@angular/core';
import {
  async,
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { TestBedConfigure } from '@app/shared';
import { CesstypeTcsformComponent } from './cesstype-tcsform.component';
import { CesstypeTcsService } from '../cesstype-tcs.service';
import { By } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';

describe('CesstypeTcsformComponent', () => {
  let component: CesstypeTcsformComponent;
  let fixture: ComponentFixture<CesstypeTcsformComponent>;
  let de: DebugElement;
  let el: HTMLElement;
  let service: CesstypeService;
  let router: Router;
  let location: Location;
  beforeEach(async(() => {
    TestBedConfigure.formConfig(
      [CesstypeTcsformComponent],
      [BrowserDynamicTestingModule, HttpClientModule, HttpClientTestingModule],
      []
    );
  }));
  beforeEach(() => {
    fixture = TestBed.createComponent(CesstypeTcsformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    de = fixture.debugElement.query(By.css('form'));
    service = fixture.debugElement.injector.get(CesstypeTcsService);
  });
  var date = new Date();
  var obj = {
    cessType: '2019',
    applicableFrom: date,
    applicableTo: date
  };

  it('should be created', async(() => {
    expect(component).toBeTruthy();
  }));

  it('should summited true', async(() => {
    component.saveAndNewCessType();
    expect(component.submitted).toBeTruthy();
  }));

  it('form should invaild', () => {
    component.cessTypeForm.controls['cessType'].setValue('');
    component.cessTypeForm.controls['applicableFrom'].setValue('');
    component.cessTypeForm.controls['applicableTo'].setValue('');
    expect(component.cessTypeForm.valid).toBeFalsy();
  });

  it('cessType field validity', () => {
    let errors = {};
    let cessType = component.cessTypeForm.controls['cessType'];
    errors = cessType.errors || {};
    component.saveAndNewCessType();
    expect(errors['required']).toBeTruthy();
  });

  it('form should vaild', () => {
    component.cessTypeForm.controls['cessType'].setValue(
      returnValue('pass', obj.cessType)
    );
    component.cessTypeForm.controls['applicableFrom'].setValue(date);
    component.cessTypeForm.controls['applicableTo'].setValue(date);
    expect(component.cessTypeForm.valid).toBeTruthy();
    component.saveAndNewCessType();
    expect(component.cessTypeForm.reset).toBeTruthy();
  });

  it('form valid with save click and routes channge', () => {
    component.cessTypeForm.controls['cessType'].setValue(
      returnValue('pass', obj.cessType)
    );
    component.cessTypeForm.controls['applicableFrom'].setValue(date);
    component.cessTypeForm.controls['applicableTo'].setValue(date);
    expect(component.cessTypeForm.valid).toBeTruthy();
    component.saveCessType();
    expect(component.cessTypeForm.reset).toBeTruthy();
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
