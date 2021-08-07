import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Shell } from '@app/shell/shell.service';
import { AuthGuard } from './shell/authentication/auth.guard';
import { NotfoundComponent } from './notfound/notfound.component';

const routes: Routes = [
  // { path: '', redirectTo: '/landing', pathMatch: 'full' },
  {
    path: '',
    loadChildren: () =>
      import('app/landing/landing.module').then(m => m.LandingModule)
  },
  {
    path: 'unauthorized',
    loadChildren: () =>
      import('app/unauthorized/unauthorized.module').then(
        m => m.UnauthorizedModule
      )
  },
  Shell.childRoutes([
    {
      path: 'masters',
      loadChildren: () =>
        import('app/masters/masters.module').then(m => m.MastersModule),
      canActivate: [AuthGuard],
      data: {
        breadcrumb: 'Masters',
        authorities: ['MASTERS']
      }
    },
    {
      path: 'admin',
      loadChildren: () =>
        import('app/admin/admin.module').then(m => m.AdminModule),
      canActivate: [AuthGuard],
      data: {
        breadcrumb: 'Admin',
        authorities: ['SUPER ADMIN']
      }
    },
    {
      path: '**',
      component: NotfoundComponent
    }
  ])
  // { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule]
  // providers: []
})
export class AppRoutingModule {}
