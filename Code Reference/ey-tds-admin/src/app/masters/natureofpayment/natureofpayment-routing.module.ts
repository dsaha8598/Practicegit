import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NatureofpaymentComponent } from './natureofpayment.component';
import { NatureofpaymentformComponent } from './natureofpaymentform/natureofpaymentform.component';

const routes: Routes = [
  {
    path: '',
    component: NatureofpaymentComponent
  },
  {
    path: 'form',
    component: NatureofpaymentformComponent,
    children: [
      {
        path: ':action',
        component: NatureofpaymentformComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NatureofpaymentRoutingModule {}
