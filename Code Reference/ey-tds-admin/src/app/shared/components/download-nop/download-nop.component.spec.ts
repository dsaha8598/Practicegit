import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadNOPComponent } from './download-nop.component';

describe('DownloadNOPComponent', () => {
  let component: DownloadNOPComponent;
  let fixture: ComponentFixture<DownloadNOPComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DownloadNOPComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadNOPComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
