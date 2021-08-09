import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';
import { HSNApplicationMastersComponent } from './hsn-application-masters.component';
import { HSNFormComponent } from './hsn-form/hsn-form.component';

const routes: Routes = [
  {
    path: '',
    component: HSNApplicationMastersComponent
  },
  {
    path: 'form',
    component: HSNFormComponent
  }
];

@NgModule({
  declarations: [],
  imports: [CommonModule, RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HSNClientRoutingModule {}
