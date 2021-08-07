import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly cookieStorageKeys: string[];
  constructor(private readonly cookieService: CookieService) {
    this.cookieStorageKeys = ['TOKEN'];
  }

  setItem(key: any, value: any): void {
    if (this.cookieStorageKeys.includes(key.toUpperCase())) {
      this.setCookie(key.toUpperCase(), value, 2);
    } else {
      sessionStorage.setItem(key, value);
    }
  }

  getItem(key: any): any {
    if (this.cookieStorageKeys.includes(key.toUpperCase())) {
      return this.getCookie(key);
    }

    return sessionStorage.getItem(key);
  }

  clearAll(): void {
    localStorage.clear();
    sessionStorage.clear();
    this.deleteCookies();
  }

  private getCookie(name: string): any {
    const ca: Array<string> = document.cookie.split(';');
    const caLen: number = ca.length;
    const cookieName = `${name.toUpperCase()}=`;
    let c: string;

    for (let i = 0; i < caLen; i += 1) {
      c = ca[i].replace(/^\s+/g, '');
      if (c.indexOf(cookieName) === 0) {
        return c.substring(cookieName.length, c.length);
      }
    }

    return '';
  }

  private deleteCookies(): void {
    this.cookieStorageKeys.forEach(element => {
      this.setCookie(element, '', -1);
    });
  }

  private setCookie(name: string, value: string, expireDays: number): void {
    const d: Date = new Date();
    d.setTime(d.getTime() + expireDays * 24 * 60 * 60 * 1000);
    const expires = `expires=${d.toUTCString()}`;
    const cpath: string = `; path=${window.location.href}`;
    document.cookie = `${name}=${value}; ${expires}${cpath}`;
  }
}
