import { Injectable } from '@angular/core';
import { StorageService } from './storageservice';
import { LZStringService } from 'ng-lz-string';

@Injectable({
  providedIn: 'root'
})
export class RouteaccessService {
  compressor: any;
  private authorities: Array<string>;
  constructor(
    private readonly storageService: StorageService,
    private readonly lzStringService: LZStringService
  ) {
    this.authorities = [];
    // this.database = indexedDB.open('window.origin', 1);
  }

  setAuthorities(authoritiesList: Array<string>, role: string): void {
    this.storageService.setItem(
      'authorities',
      window.btoa(authoritiesList.join('|'))
    );
    this.storageService.setItem('role', role);
  }

  getAuthorities(): Array<string> {
    const authorities = window
      .atob(this.storageService.getItem('authorities'))
      .split('|');

    return authorities ? authorities : [];
  }

  getRole(): string {
    const role = this.storageService.getItem('role');

    return role ? role : 'Not Assigned';
  }

  hasAccess(authorities: Array<string>): boolean {
    this.authorities = this.getAuthorities();
    if (!authorities) {
      return false;
    }

    for (const i of authorities) {
      if (this.authorities.includes(i)) {
        return true;
      }
    }

    return false;
  }
}
