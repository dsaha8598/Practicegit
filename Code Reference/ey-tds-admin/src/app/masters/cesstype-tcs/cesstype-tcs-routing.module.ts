import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CesstypeTcsComponent } from './cesstype-tcs.component';
import { CesstypeTcsformComponent } from './cesstype-tcsform/cesstype-tcsform.component';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

const routes: Routes = [
  {
    path: '',
    component: CesstypeTcsComponent
  },
  {
    path: 'form',
    component: CesstypeTcsformComponent
  },
  {
    path: ':id',
    component: CesstypeTcsformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CesstypeTcsRoutingModule {
  constructor(private logger: CustomLoggerService) {
    this.logger.debug(routes);
  }
}
