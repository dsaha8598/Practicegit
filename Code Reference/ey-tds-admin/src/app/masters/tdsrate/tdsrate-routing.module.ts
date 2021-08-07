import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TdsrateComponent } from './tdsrate.component';
import { TdsrateformComponent } from './tdsrateform/tdsrateform.component';

const routes: Routes = [
  {
    path: '',
    component: TdsrateComponent
  },
  {
    path: 'form',
    component: TdsrateformComponent
  },
  {
    path: ':id',
    component: TdsrateformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TdsrateRoutingModule {}
