import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NatureofincomeComponent } from './natureofincome.component';
import { NatureofincomeformComponent } from './natureofincomeform/natureofincomeform.component';

const routes: Routes = [
  {
    path: '',
    component: NatureofincomeComponent
  },
  {
    path: 'form',
    component: NatureofincomeformComponent,
    children: [
      {
        path: ':action',
        component: NatureofincomeformComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NatureofincomeRoutingModule {}
