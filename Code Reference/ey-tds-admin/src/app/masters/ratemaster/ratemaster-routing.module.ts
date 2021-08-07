import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RatemasterComponent } from './ratemaster.component';
import { RatemasterformComponent } from './ratemasterform/ratemasterform.component';

const routes: Routes = [
  {
    path: '',
    component: RatemasterComponent
  },
  {
    path: 'form',
    component: RatemasterformComponent
  },
  {
    path: ':id',
    component: RatemasterformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RatemasterRoutingModule {}
