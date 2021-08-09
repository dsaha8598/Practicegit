import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IpfmComponent } from './ipfm.component';
import { IpfmformComponent } from './ipfmform/ipfmform.component';

const routes: Routes = [
  {
    path: '',
    component: IpfmComponent
  },
  {
    path: 'form',
    component: IpfmformComponent
  },
  {
    path: ':id',
    component: IpfmformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IpfmRoutingModule {}
