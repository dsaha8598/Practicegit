import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RateMasterTreatyComponent } from './ratemastertreaty.component';
import { RateMasterTreatyformComponent } from './ratemastertreatyform/ratemastertreatyform.component';

const routes: Routes = [
  {
    path: '',
    component: RateMasterTreatyComponent
  },
  {
    path: 'form',
    component: RateMasterTreatyformComponent
  },
  {
    path: ':id',
    component: RateMasterTreatyComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RateMasterTreatyRoutingModule {}
