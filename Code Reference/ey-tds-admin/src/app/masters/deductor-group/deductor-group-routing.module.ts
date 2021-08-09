import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DeductorGroupComponent } from './deductor-group.component';
import { DeductorGroupFormComponent } from './deductor-group-form/deductor-group-form.component';
import { RolesPermissionFormComponent } from '@app/shared/components/roles-permissions/roles-permission-form/roles-permission-form.component';
import { RolesPermissionTcsFormComponent } from '@app/shared/components/roles-permissions-tcs/roles-permission-tcs-form/roles-permission-tcs-form.component';

const routes: Routes = [
  {
    path: '',
    component: DeductorGroupComponent
  },
  {
    path: 'form',
    component: DeductorGroupFormComponent
  },
  {
    path: 'roleform',
    component: RolesPermissionFormComponent,
    data: {
      breadcrumb: 'Roles and Permissions'
    }
  },
  {
    path: 'roleform',
    component: RolesPermissionTcsFormComponent,
    data: {
      breadcrumb: 'Roles and Permissions'
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DeductorGroupRoutingModule {}
