import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SurchargeComponent } from './surcharge.component';
import { SurchargeformComponent } from './surchargeform/surchargeform.component';

const routes: Routes = [
  {
    path: '',
    component: SurchargeComponent
  },
  {
    path: 'form',
    component: SurchargeformComponent
  },
  {
    path: ':id',
    component: SurchargeformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SurchargeRoutingModule {}
