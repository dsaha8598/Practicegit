import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { I18nService } from '@app/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  menuHidden = true;

  constructor(
    private readonly router: Router,
    private readonly i18nService: I18nService
  ) {}

  ngOnInit(): void {}

  toggleMenu(): void {
    this.menuHidden = !this.menuHidden;
  }

  setLanguage(language: string): void {
    this.i18nService.language = language;
  }

  get currentLanguage(): string {
    return this.i18nService.language;
  }

  // tslint:disable-next-line: prefer-array-literal
  get languages(): Array<string> {
    return this.i18nService.supportedLanguages;
  }
}
