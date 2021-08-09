import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CessComponent } from './cess.component';
import { CessformComponent } from './cessform/cessform.component';

const routes: Routes = [
  {
    path: '',
    component: CessComponent
  },
  {
    path: 'form',
    component: CessformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CessRoutingModule {}
