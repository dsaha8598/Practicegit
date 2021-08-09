import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UnauthorizedComponent } from './unauthorized.component';
import { UnauthorizedRoutingModule } from './unauthorized-routing.module';

@NgModule({
  declarations: [UnauthorizedComponent],
  imports: [CommonModule, UnauthorizedRoutingModule]
})
export class UnauthorizedModule {}
