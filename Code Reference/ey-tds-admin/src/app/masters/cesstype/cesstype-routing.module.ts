import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CesstypeComponent } from './cesstype.component';
import { CesstypeformComponent } from './cesstypeform/cesstypeform.component';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

const routes: Routes = [
  {
    path: '',
    component: CesstypeComponent
  },
  {
    path: 'form',
    component: CesstypeformComponent
  },
  {
    path: ':id',
    component: CesstypeformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CesstypeRoutingModule {
  constructor(private logger: CustomLoggerService) {
    this.logger.debug(routes);
  }
}
