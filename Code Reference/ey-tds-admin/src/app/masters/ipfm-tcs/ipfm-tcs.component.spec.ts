import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { IpfmTcsComponent } from './ipfm-tcs.component';
import { TestBedConfigure } from '@app/shared';

describe('IpfmTcsComponent', () => {
  let component: IpfmTcsComponent;
  let fixture: ComponentFixture<IpfmTcsComponent>;

  beforeEach(async(() => {
    TestBedConfigure.listConfig([IpfmTcsComponent], [], []);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IpfmTcsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
