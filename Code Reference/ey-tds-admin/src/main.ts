/*
 * Entry point of the application.
 * Only platform bootstrapping code should be here.
 * For app-specific initialization, use `app/app.component.ts`.
 */

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from '@app/app.module';
import { environment } from '@env/environment';

if (environment.production) {
  enableProdMode();
  const isIEOrEdge = /msie\s|trident\/|edge\//i.test(
    window.navigator.userAgent
  );
  if (!isIEOrEdge) {
    if (window.navigator) {
      navigator.serviceWorker.getRegistrations().then(function(registrations) {
        for (const registration of registrations) {
          registration.unregister();
        }
      });
    }
  }
  if (window.caches) {
    caches.keys().then(function(names) {
      for (const name of names) {
        caches.delete(name);
      }
    });
  }
}

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch((err: any) => console.log(err));
