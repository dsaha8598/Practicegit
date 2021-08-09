import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MonthTrackerTcsComponent } from './month-tracker-tcs.component';
import { MonthTrackerTcsFormComponent } from './month-tracker-tcsform/month-tracker-tcsform.component';

const routes: Routes = [
  {
    path: '',
    component: MonthTrackerTcsComponent
  },
  {
    path: 'form',
    component: MonthTrackerTcsFormComponent,
    data: {
      breadcrumb: 'Month Tracker'
    }
  },
  {
    path: ':id',
    component: MonthTrackerTcsFormComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonthTrackerTcsRoutingModule {}
