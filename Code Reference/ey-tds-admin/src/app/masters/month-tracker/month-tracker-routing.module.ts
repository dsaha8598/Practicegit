import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MonthTrackerComponent } from './month-tracker.component';
import { MonthTrackerFormComponent } from './month-tracker-form/month-tracker-form.component';

const routes: Routes = [
  {
    path: '',
    component: MonthTrackerComponent
  },
  {
    path: 'form',
    component: MonthTrackerFormComponent
  },
  {
    path: ':id',
    component: MonthTrackerFormComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonthTrackerRoutingModule {}
