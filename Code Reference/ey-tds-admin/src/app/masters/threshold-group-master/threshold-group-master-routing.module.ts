import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ThresholdGroupMasterComponent } from './threshold-group-master.component';
import { ThresholdgrpformComponent } from './thresholdgrpform/thresholdgrpform.component';

const routes: Routes = [
  {
    path: '',
    component: ThresholdGroupMasterComponent
  },
  {
    path: 'form',
    component: ThresholdgrpformComponent
  },
  {
    path: ':id',
    component: ThresholdgrpformComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ThresholdGroupMasterRoutingModule {}
