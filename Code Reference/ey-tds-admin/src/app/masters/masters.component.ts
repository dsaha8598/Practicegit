import { Component, OnInit } from '@angular/core';
import { StorageService } from '@app/shell/authentication/storageservice';
import { MasterService } from './master.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'ey-masters',
  templateUrl: './masters.component.html',
  styleUrls: ['./masters.component.scss']
})
export class MastersComponent implements OnInit {
  masterNavigation: any;
  masterNavigationTCS: any;
  clientNavigation: any;
  onboardingNavigation: any;
  items: Array<any>;
  home: any;
  str: String;
  selectedModule: String;
  backModule: string;
  displayConsent: boolean = false;
  // extra
  message: string;
  constructor(
    private readonly masterService: MasterService,
    private readonly storageService: StorageService,
    private readonly activateRouter: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activateRouter.queryParams.subscribe(params => {
      this.backModule = params['selectedModule'];
    });

    if (this.backModule) {
      this.selectedModule = this.backModule;
    } else {
      this.selectedModule = 'TCS';
    }
    this.masterNavigation = [
      {
        label: 'Nature of expense',
        href: 'natureofpayment',
        addNewAuthority: 'NOP_CREATE',
        listViewAuthority: 'NOP_LIST',
        isVisibleAuthority: 'NOP_MASTER'
      },
      {
        label: 'TDS rate',
        href: 'tdsrate',
        addNewAuthority: 'TDS_RATE_CREATE',
        listViewAuthority: 'TDS_RATE_LIST',
        isVisibleAuthority: 'TDS_RATE_MASTER'
      },
      {
        label: 'Interest & Penalty',
        href: 'Interest&penalty',
        addNewAuthority: 'IPFM_CREATE',
        listViewAuthority: 'IPFM_LIST',
        isVisibleAuthority: 'IPFM_MASTER'
      },
      {
        label: 'Non-resident master',
        href: 'article',
        addNewAuthority: 'ARTICLE_CREATE',
        listViewAuthority: 'ARTICLE_LIST',
        isVisibleAuthority: 'ARTICLE_MASTER'
      },
      {
        label: 'Surcharge',
        href: 'surcharge',
        addNewAuthority: 'SURCHARGE_CREATE',
        listViewAuthority: 'SURCHARGE_LIST',
        isVisibleAuthority: 'SURCHARGE_MASTER'
      },
      {
        label: 'Cess type',
        href: 'cesstype',
        addNewAuthority: 'CESS_TYPE_CREATE',
        listViewAuthority: 'CESS_TYPE_LIST',
        isVisibleAuthority: 'CESS_TYPE_MASTER'
      },
      {
        label: 'Cess',
        href: 'cess',
        addNewAuthority: 'CESS_CREATE',
        listViewAuthority: 'CESS_LIST',
        isVisibleAuthority: 'CESS_MASTER'
      },
      {
        label: 'Due date tracker',
        href: 'monthtracker',
        addNewAuthority: 'NOP_CREATE',
        listViewAuthority: 'NOP_LIST',
        isVisibleAuthority: 'NOP_MASTER'
      },
      {
        label: 'SAC descriptions',
        href: 'sac-descriptions',
        uploadAuthority: 'SAC_DESCRIPTION_UPLOAD',
        listViewAuthority: 'SAC_DESCRIPTION',
        isVisibleAuthority: 'SAC_DESCRIPTION'
      },
      {
        label: 'Currency converter master',
        href: 'currency-convertor-master',
        uploadAuthority: 'CURRENCY_MASTER_UPLOAD',
        listViewAuthority: 'CURRENCY_MASTER',
        isVisibleAuthority: 'CURRENCY_MASTER'
      },
      {
        label: 'Dividend rate - Act',
        href: 'ratemasteract',
        addNewAuthority: 'DIVIDEND_RATEACT_CREATE',
        listViewAuthority: 'DIVIDEND_RATEACT_LIST',
        isVisibleAuthority: 'DIVIDEND_RATEACT_MASTER'
      },
      {
        label: 'Dividend rate - Treaty',
        href: 'ratemastertreaty',
        addNewAuthority: 'DIVIDEND_RATETREATY_CREATE',
        listViewAuthority: 'DIVIDEND_RATETREATY_LIST',
        isVisibleAuthority: 'DIVIDEND_RATETREATY_MASTER'
      },
      {
        label: 'Exemptions List',
        href: 'exemptionlist',
        uploadAuthority: 'EXEMPTION_UPLOAD',
        listViewAuthority: 'EXEMPTION_LIST',
        isVisibleAuthority: 'EXEMPTION_MASTER'
      },
      {
        label: 'Threshold group master',
        href: 'thresholdgrpMaster',
        addNewAuthority: 'THRESHOLD_GROUP_CREATE',
        listViewAuthority: 'THRESHOLD_GROUP_LIST',
        isVisibleAuthority: 'THRESHOLD_GROUP_MASTER'
      },
      {
        label: 'HSN',
        href: 'hsn-application',
        addNewAuthority: 'HSN_DESCRIPTION_CREATE',
        uploadAuthority: 'HSN_DESCRIPTION_UPLOAD',
        listViewAuthority: 'HSN_DESCRIPTION',
        isVisibleAuthority: 'HSN_DESCRIPTION'
      }
    ];

    this.masterNavigationTCS = [
      {
        label: 'Nature of income',
        href: 'natureofincome',
        addNewAuthority: 'NOI_CREATE',
        listViewAuthority: 'NOI_LIST',
        isVisibleAuthority: 'NOI_MASTER'
      },
      {
        label: 'Rate master',
        href: 'ratemaster',
        addNewAuthority: 'TCS_RATE_CREATE',
        listViewAuthority: 'TCS_RATE_LIST',
        isVisibleAuthority: 'TCS_RATE_MASTER'
      },
      {
        label: 'Interest & Penalty',
        href: 'Interest&penaltyTCS',
        addNewAuthority: 'TCS_IPFM_CREATE',
        listViewAuthority: 'TCS_IPFM_LIST',
        isVisibleAuthority: 'TCS_IPFM_MASTER'
      },
      {
        label: 'Surcharge',
        href: 'surchargeTcs',
        addNewAuthority: 'TCS_SURCHARGE_CREATE',
        listViewAuthority: 'TCS_SURCHARGE_LIST',
        isVisibleAuthority: 'TCS_SURCHARGE_MASTER'
      },
      {
        label: 'Cess type',
        href: 'cesstypeTcs',
        addNewAuthority: 'TCS_CESS_TYPE_CREATE',
        listViewAuthority: 'TCS_CESS_TYPE_LIST',
        isVisibleAuthority: 'TCS_CESS_TYPE_MASTER'
      },
      {
        label: 'Cess',
        href: 'cessTcs',
        addNewAuthority: 'TCS_CESS_CREATE',
        listViewAuthority: 'TCS_CESS_LIST',
        isVisibleAuthority: 'TCS_CESS_MASTER'
      },
      {
        label: 'Due date/Month tracker',
        href: 'monthtrackerTcs',
        addNewAuthority: 'NOI_CREATE',
        listViewAuthority: 'NOI_LIST',
        isVisibleAuthority: 'NOI_MASTER'
      },
      {
        label: 'HSN/SAC-NOI mappings',
        href: 'sac-descriptions-tcs',
        uploadAuthority: 'TCS_SAC_DESCRIPTION_UPLOAD',
        listViewAuthority: 'TCS_SAC_DESCRIPTION',
        isVisibleAuthority: 'TCS_SAC_DESCRIPTION'
      }
    ];

    this.onboardingNavigation = [
      {
        label: 'Group & Tenant config',
        href: 'deductor-group',
        addNewAuthority: 'ONBOARDING_MASTER',
        listViewAuthority: 'ONBOARDING_MASTER',
        isVisibleAuthority: 'ONBOARDING_MASTER'
      },
      {
        label: 'TCS Section Rate Mapping',
        href: 'tcs-section-rate-mapping',
        listViewAuthority: 'ONBOARDING_MASTER',
        isVisibleAuthority: 'ONBOARDING_MASTER'
      },
      {
        label: 'TDS Section Rate Mapping',
        href: 'tds-section-rate-mapping',
        listViewAuthority: 'ONBOARDING_MASTER',
        isVisibleAuthority: 'ONBOARDING_MASTER'
      },
      {
        label: 'HSN Rate Mapping',
        href: 'hsn-rate-mapping',
        listViewAuthority: 'ONBOARDING_MASTER',
        isVisibleAuthority: 'ONBOARDING_MASTER'
      }
    ];

    this.clientNavigation = [
      {
        label: 'Deductee/Vendor',
        href: 'deductee',
        addNewAuthority: 'DEDUCTEE_CREATE',
        listViewAuthority: 'DEDUCTEE_LIST',
        isVisibleAuthority: 'DEDUCTEE_MASTER'
      },
      {
        // TODO: Revisit this and work on the permissions
        label: 'General ledger accounts',
        href: 'coa-codes',
        listViewAuthority: 'COA_MASTER',
        isVisibleAuthority: 'COA_MASTER'
      },
      {
        label: 'Lower deduction certificate: resident',
        href: 'ldc',
        addNewAuthority: 'LDC_CREATE',
        listViewAuthority: 'LDC_LIST',
        isVisibleAuthority: 'LDC_MASTER'
      },
      {
        label: 'Lower deduction certificate: Non-resident',
        href: 'ao',
        addNewAuthority: 'AO_CREATE',
        listViewAuthority: 'AO_LIST',
        isVisibleAuthority: 'AO_MASTER'
      },
      {
        label: 'Keyword repository',
        href: 'keywords',
        addNewAuthority: 'KEYWORD_MASTER_CREATE',
        listViewAuthority: 'KEYWORD_MASTER_VIEW',
        isVisibleAuthority: 'KEYWORD_MASTER'
      },
      {
        label: 'Manage Traces Credentials',
        href: 'manage-traces-credentials',
        listViewAuthority: 'MANAGE_TRACES_CREDENTIALS',
        isVisibleAuthority: 'MANAGE_TRACES_CREDENTIALS'
      }
    ];

    this.showConsent();
  }

  handleSelectChange(): void {}
  syncRoleDefaults(): void {
    this.masterService.syncRoles().subscribe(
      (result: any) => {
        console.log(result);
      },
      (error: any) => {
        console.log(error);
      }
    );
  }

  showConsent(): void {
    let show = this.storageService.getItem('consentRequired');
    if (show === 'yes') {
      this.displayConsent = true;
    } else {
      this.displayConsent = false;
    }
  }
  callAPI(): void {
    this.masterService.getCallAPI().subscribe(
      (result: any) => {
        console.log(result);
      },
      (error: any) => {
        console.log(error);
      }
    );
  }
}
