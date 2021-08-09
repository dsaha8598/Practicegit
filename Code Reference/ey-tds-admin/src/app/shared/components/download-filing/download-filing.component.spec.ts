import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadFilingComponent } from './download-filing.component';

describe('DownloadFilingComponent', () => {
  let component: DownloadFilingComponent;
  let fixture: ComponentFixture<DownloadFilingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DownloadFilingComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadFilingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
