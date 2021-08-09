import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { IpfmComponent } from './ipfm.component';
import { TestBedConfigure } from '@app/shared';

describe('IpfmComponent', () => {
  let component: IpfmComponent;
  let fixture: ComponentFixture<IpfmComponent>;

  beforeEach(async(() => {
    TestBedConfigure.listConfig([IpfmComponent], [], []);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IpfmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
