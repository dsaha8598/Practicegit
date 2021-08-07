import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadKeywordsComponent } from './download-keywords.component';

describe('DownloadKeywordsComponent', () => {
  let component: DownloadKeywordsComponent;
  let fixture: ComponentFixture<DownloadKeywordsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DownloadKeywordsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadKeywordsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
