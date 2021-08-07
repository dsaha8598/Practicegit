import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ArticleformComponent } from './articleform.component';
import { TestBedConfigure } from '@app/shared';

describe('ArticleformComponent', () => {
  let component: ArticleformComponent;
  let fixture: ComponentFixture<ArticleformComponent>;

  beforeEach(async(() => {
    TestBedConfigure.formConfig([ArticleformComponent], [], []);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
