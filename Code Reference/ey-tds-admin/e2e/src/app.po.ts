/*
 * Use the Page Object pattern to define the page under test.
 * See docs/coding-guide/e2e-tests.md for more info.
 */

import { browser, element, by } from 'protractor';
import { StorageService } from '@app/shell/authentication/storageservice';

export class AppPage {
  usernameField = element(by.css('input[formControlName="username"]'));
  passwordField = element(by.css('input[formControlName="password"]'));
  loginButton = element(by.css('button[type="submit"]'));

  constructor(private storageService: StorageService) {
    // Forces default language
    this.navigateTo();
    browser.executeScript(() =>
      this.storageService.setItem('language', 'en-US')
    );
  }

  navigateTo() {
    return browser.get('/');
  }

  login() {
    this.usernameField.sendKeys('test');
    this.passwordField.sendKeys('123');
    this.loginButton.click();
  }

  getParagraphText() {
    return element(by.css('app-root h1')).getText();
  }
}
