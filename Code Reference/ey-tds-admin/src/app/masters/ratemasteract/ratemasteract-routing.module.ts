import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RateMasterActComponent } from './ratemasteract.component';
import { RateMasterActformComponent } from './ratemasteractform/ratemasteractform.component';

const routes: Routes = [
  {
    path: '',
    component: RateMasterActComponent
  },
  {
    path: 'form',
    component: RateMasterActformComponent
  },
  {
    path: ':id',
    component: RateMasterActComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RateMasterActRoutingModule {}
