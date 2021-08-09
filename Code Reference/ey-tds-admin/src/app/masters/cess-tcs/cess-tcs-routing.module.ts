import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CessTcsComponent } from './cess-tcs.component';
import { CessTcsformComponent } from './cess-tcsform/cess-tcsform.component';

const routes: Routes = [
  {
    path: '',
    component: CessTcsComponent
  },
  {
    path: 'form',
    component: CessTcsformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CessTcsRoutingModule {}
