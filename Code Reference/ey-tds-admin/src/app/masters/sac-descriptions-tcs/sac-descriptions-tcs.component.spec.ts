import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SacDescriptionsTcsComponent } from './sac-descriptions-tcs.component';

describe('SacDescriptionsComponent', () => {
  let component: SacDescriptionsComponent;
  let fixture: ComponentFixture<SacDescriptionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SacDescriptionsComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SacDescriptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
