import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IpfmTcsComponent } from './ipfm-tcs.component';
import { IpfmTcsformComponent } from './ipfm-tcsform/ipfm-tcsform.component';

const routes: Routes = [
  {
    path: '',
    component: IpfmTcsComponent
  },
  {
    path: 'form',
    component: IpfmTcsformComponent
  },
  {
    path: ':id',
    component: IpfmTcsformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class IpfmTcsRoutingModule {}
