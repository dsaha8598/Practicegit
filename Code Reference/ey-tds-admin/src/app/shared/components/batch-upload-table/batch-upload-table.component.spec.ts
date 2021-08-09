import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BatchUploadTableComponent } from './batch-upload-table.component';

describe('BatchUploadTableComponent', () => {
  let component: BatchUploadTableComponent;
  let fixture: ComponentFixture<BatchUploadTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BatchUploadTableComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BatchUploadTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
