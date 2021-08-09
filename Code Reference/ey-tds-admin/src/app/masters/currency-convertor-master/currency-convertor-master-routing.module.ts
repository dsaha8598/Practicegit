import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CurrencyConvertorMasterComponent } from './currency-convertor-master.component';

const routes: Routes = [
  {
    path: '',
    component: CurrencyConvertorMasterComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CurrencyConvertorMasterRoutingModule {}
