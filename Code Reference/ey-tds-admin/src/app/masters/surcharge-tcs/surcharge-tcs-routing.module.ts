import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SurchargeTcsComponent } from './surcharge-tcs.component';
import { SurchargeTcsformComponent } from './surcharge-tcsform/surcharge-tcsform.component';

const routes: Routes = [
  {
    path: '',
    component: SurchargeTcsComponent
  },
  {
    path: 'tcsform',
    component: SurchargeTcsformComponent
  },
  {
    path: ':id',
    component: SurchargeTcsformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SurchargeTcsRoutingModule {}
