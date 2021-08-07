import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MastersComponent } from './masters.component';
import { AuthGuard } from '@app/shell/authentication/auth.guard';
import { SacDescriptionsComponent } from './sac-descriptions/sac-descriptions.component';
import { SacDescriptionsTcsComponent } from './sac-descriptions-tcs/sac-descriptions-tcs.component';
import { SectionRateMappingComponent } from './section-rate-mapping/section-rate-mapping.component';
import { HsnRateMappingComponent } from './hsn-rate-mapping/hsn-rate-mapping.component';
import { HSNApplicationMastersComponent } from './hsn-application-masters/hsn-application-masters.component';
import { SectionRateMappingTCSComponent } from './section-rate-mapping-tcs/section-rate-mapping-tcs.component';
import { ExemptionsListComponent } from './exemptions-list/exemptions-list.component';

const routes: Routes = [
  {
    path: '',
    component: MastersComponent
  },
  {
    path: 'cesstype',
    loadChildren: () =>
      import('app/masters/cesstype/cesstype.module').then(
        m => m.CesstypeModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Cess Type',
      authorities: ['CESS_TYPE_MASTER']
    }
  },
  {
    path: 'cesstypeTcs',
    loadChildren: () =>
      import('app/masters/cesstype-tcs/cesstype-tcs.module').then(
        m => m.CesstypeTcsModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Cess Type',
      authorities: ['TCS_CESS_TYPE_MASTER']
    }
  },
  {
    path: 'cess',
    loadChildren: () =>
      import('app/masters/cess/cess.module').then(m => m.CessModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Cess',
      authorities: ['CESS_MASTER']
    }
  },
  {
    path: 'cessTcs',
    loadChildren: () =>
      import('app/masters/cess-tcs/cess-tcs.module').then(m => m.CessTcsModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Cess',
      authorities: ['TCS_CESS_MASTER']
    }
  },

  {
    path: 'surcharge',
    loadChildren: () =>
      import('app/masters/surcharge/surcharge.module').then(
        m => m.SurchargeModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Surcharge',
      authorities: ['SURCHARGE_MASTER']
    }
  },

  {
    path: 'surchargeTcs',
    loadChildren: () =>
      import('app/masters/surcharge-tcs/surcharge-tcs.module').then(
        m => m.SurchargeTcsModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Surcharge',
      authorities: ['TCS_SURCHARGE_MASTER']
    }
  },

  {
    path: 'Interest&penalty',
    loadChildren: () =>
      import('app/masters/ipfm/ipfm.module').then(m => m.IpfmModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Interest & Penalty',
      authorities: ['IPFM_MASTER']
    }
  },
  {
    path: 'Interest&penaltyTCS',
    loadChildren: () =>
      import('app/masters/ipfm-tcs/ipfm-tcs.module').then(m => m.IpfmTcsModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Interest & Penalty',
      authorities: ['TCS_IPFM_MASTER']
    }
  },
  {
    path: 'tdsrate',
    loadChildren: () =>
      import('app/masters/tdsrate/tdsrate.module').then(m => m.TdsrateModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'TDS Rate',
      authorities: ['TDS_RATE_MASTER']
    }
  },
  {
    path: 'ratemaster',
    loadChildren: () =>
      import('app/masters/ratemaster/ratemaster.module').then(
        m => m.RatemasterModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Rate master',
      authorities: ['TCS_RATE_MASTER']
    }
  },
  {
    path: 'monthtracker',
    loadChildren: () =>
      import('app/masters/month-tracker/month-tracker.module').then(
        m => m.MonthTrackerModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Due date tracker',
      authorities: ['NOP_MASTER']
    }
  },
  {
    path: 'monthtrackerTcs',
    loadChildren: () =>
      import('app/masters/month-tracker-tcs/month-tracker-tcs.module').then(
        m => m.MonthTrackerTcsModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Due date tracker',
      authorities: ['NOI_MASTER']
    }
  },

  {
    path: 'article',
    loadChildren: () =>
      import('app/masters/article/article.module').then(m => m.ArticleModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Non-Resident master',
      authorities: ['ARTICLE_MASTER']
    }
  },
  {
    path: 'natureofpayment',
    loadChildren: () =>
      import('app/masters/natureofpayment/natureofpayment.module').then(
        m => m.NatureofpaymentModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Nature of expense',
      authorities: ['NOP_MASTER']
    }
  },
  {
    path: 'natureofincome',
    loadChildren: () =>
      import('app/masters/natureofincome/natureofincome.module').then(
        m => m.NatureofincomeModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Nature of income',
      authorities: ['NOI_MASTER']
    }
  },
  {
    path: 'thresholdgrpMaster',
    loadChildren: () =>
      import(
        'app/masters/threshold-group-master/threshold-group-master.module'
      ).then(m => m.ThresholdGroupMasterModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Threshold group master',
      authorities: ['THRESHOLD_GROUP_MASTER']
    }
  },
  {
    path: 'hsn-application',
    loadChildren: () =>
      import(
        'app/masters/hsn-application-masters/hsn-application-masters.module'
      ).then(m => m.HSNApplicationMastersModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'HSN',
      authorities: ['HSN_DESCRIPTION']
    }
  },
  {
    path: 'tcs-section-rate-mapping',
    component: SectionRateMappingTCSComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'TCS Section Rate Mapping',
      authorities: ['NOI_MASTER']
    }
  },
  {
    path: 'tds-section-rate-mapping',
    component: SectionRateMappingComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'TDS Section Rate Mapping',
      authorities: ['NOI_MASTER']
    }
  },
  {
    path: 'hsn-rate-mapping',
    component: HsnRateMappingComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'HSN Rate Mapping',
      authorities: ['NOI_MASTER']
    }
  },
  {
    path: 'sac-descriptions',
    component: SacDescriptionsComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'SAC decriptions',
      authorities: ['SAC_DESCRIPTION']
    }
  },
  {
    path: 'sac-descriptions-tcs',
    component: SacDescriptionsTcsComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'HSN/SAC-NOI mappings',
      authorities: ['TCS_SAC_DESCRIPTION']
    }
  },
  {
    path: 'deductor-group',
    loadChildren: () =>
      import('app/masters/deductor-group/deductor-group.module').then(
        m => m.DeductorGroupModule
      ),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Group and Tenant Config',
      authorities: ['ONBOARDING_MASTER']
    }
  },
  {
    path: 'currency-convertor-master',
    loadChildren: () =>
      import(
        'app/masters/currency-convertor-master/currency-convertor-master.module'
      ).then(m => m.CurrencyConvertorMasterModule),
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Currency Converter Master',
      authorities: ['CURRENCY_MASTER']
    }
  },
  {
    path: 'ratemasteract',
    loadChildren:
      'app/masters/ratemasteract/ratemasteract.module#RateMasterActModule',
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Dividend rate - Act',
      authorities: ['DIVIDEND_RATEACT_MASTER']
    }
  },
  {
    path: 'ratemastertreaty',
    loadChildren:
      'app/masters/ratemastertreaty/ratemastertreaty.module#RateMasterTreatyModule',
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Dividend rate - Treaty',
      authorities: ['DIVIDEND_RATETREATY_MASTER']
    }
  },
  {
    path: 'exemptionlist',
    component: ExemptionsListComponent,
    canActivate: [AuthGuard],
    data: {
      breadcrumb: 'Exemptions',
      authorities: ['EXEMPTION_MASTER']
    }
  },
  {
    path: '',
    redirectTo: '',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MastersRoutingModule {}
