import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavpillsComponent } from './navpills.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BrowserModule } from '@angular/platform-browser';

describe('NavpillsComponent', () => {
  let component: NavpillsComponent;
  let fixture: ComponentFixture<NavpillsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NavpillsComponent],
      imports: [BrowserModule, RouterModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavpillsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
