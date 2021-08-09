import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { OnboardingServiceTcs } from './onboarding-tcs.service';
//import { AlertService } from '../alert/alert.service';
//import { type } from 'os';
@Component({
  selector: 'ey-onboarding-tcs',
  templateUrl: './onboarding-tcs.component.html',
  styleUrls: ['./onboarding-tcs.component.scss']
})
export class OnboardingComponentTcs implements OnInit {
  items: MenuItem[];
  activeIndex: number = 0;
  scopeForm: FormGroup;
  invoicePriority: any;
  roleFormActionType: string;
  actionType: string;
  custTransform: boolean;
  rolespopup: boolean;
  roleId: string;
  moduleType: string;
  showRoleForm: boolean;
  tenantConfigurations: any = [];
  selectedPriorityValues: any = [];
  selectedInvoiceScopes: any = [];
  selectedInvoiceProcessKeys: any = [];
  @Input() deductorPAN: string;
  @Output() closePopup: EventEmitter<boolean>;
  selectedConfigValues: any = {
    onboardingConfigValues: {
      buyerThresholdComputation: '',
      challanGeneration: '',
      creditNotes: '',
      gstImplication: '',
      documentOrPostingDate: '',
      collectionReferenceId: '',
      id: 1,
      invoiceProcessScope: [],
      scopeInvoiceProcess: [],
      lccTrackingNotification: '',
      priority: [],
      scopeProcess: [],
      sectionDetermination: '',
      tcsApplicability: '',
      tdsTransaction: ''
    },
    pan: ''
  };
  showOptions: boolean;
  priorityKeys: any = [];
  priorityValues: any = [];
  scopeProcessData: any = [];
  isEdit: boolean;
  invalidStep1: boolean;
  invalidStep2: boolean;
  invalidStep3: boolean;
  invoiceProcessName: any;
  constructor(
    private readonly onboardingService: OnboardingServiceTcs /* ,
    private alertService: AlertService */
  ) {
    this.closePopup = new EventEmitter<boolean>();
  }

  ngOnInit(): void {
    this.custTransform = true;
    this.rolespopup = true;
    this.showRoleForm = false;
    this.getInitalScopeProcess();
    this.buildconfigurationsForm();
    this.stepper();
  }

  buildconfigurationsForm() {
    this.scopeForm = new FormGroup({
      scopeProcess: new FormControl('', [Validators.required]),
      invoiceProcess: new FormControl('', [Validators.required]),
      invoicePriority: new FormControl('', [Validators.required])
    });
  }

  stepper(): void {
    this.items = [
      {
        label: 'Scope',
        command: (event: any) => {
          this.activeIndex = 0;
        }
      },
      {
        label: 'Invoice Process',
        command: (event: any) => {
          this.activeIndex = 1;
        }
      }
    ];
  }

  onNext() {
    if (
      this.selectedConfigValues.onboardingConfigValues.scopeProcess.length == 0
    ) {
      this.invalidStep1 = true;
      return;
    } else if (
      this.selectedConfigValues.onboardingConfigValues.invoiceProcessScope
        .length == 0 &&
      this.selectedConfigValues.onboardingConfigValues.priority.length == 0
    ) {
      this.invalidStep2 = true;
      return;
    } else if (this.invalidStep3) {
      return;
    }
    this.invalidStep1 = false;
    this.invalidStep2 = false;
    this.invalidStep3 = false;
    this.activeIndex = this.activeIndex + 1;
  }

  getInitalScopeProcess(): void {
    this.onboardingService.getCollectorOnboardingInfo().subscribe(
      (res: any) => {
        this.tenantConfigurations = res.data;
        // console.log('printing look up tables data', this.tenantConfigurations);
        this.getOnboardingParametersInfo();
      },
      error => {}
    );
  }
  showCollectionsOptions(id: any): void {
    console.log('on');
    if (id === 24) {
      this.showOptions = true;
    } else {
      this.showOptions = false;
    }
  }

  getOnboardingParametersInfo(): void {
    this.onboardingService.getConfigurations(this.deductorPAN).subscribe(
      (res: any) => {
        if (res.data != null) {
          this.isEdit = true;
          this.selectedConfigValues = res.data;
          this.bindOnboardingParameters(res.data.onboardingConfigValues);
        } else {
          this.isEdit = false;
        }
      },
      error => {}
    );
  }

  bindOnboardingParameters(data: any) {
    if (data.tcsApplicability == '24') {
      this.showOptions = true;
    }
    this.selectedConfigValues.onboardingConfigValues = data;
    this.priorityKeys = data.priority.map((x: any) => x.configCode);
    this.priorityValues = data.priority.map((x: any) => x.configValue);
    this.scopeProcessData = data.scopeProcess.toString();
    this.bindInvoiceProcessPriorities();
    this.bindInvoiceScopeProcessData(data.scopeProcess);
    this.bindInvoiceProcessKeys(this.priorityKeys);
  }

  bindInvoiceProcessKeys(processKeys: any) {
    for (let i = 0; i < processKeys.length; i++) {
      this.selectedInvoiceProcessKeys[processKeys[i]] = true;
    }
  }

  selectAlltheScopes(event: any) {
    this.selectedConfigValues.onboardingConfigValues.scopeProcess = [];
    this.selectedInvoiceScopes = [];
    if (event) {
      let allScopes = this.tenantConfigurations.filter(
        (x: any) => x.module == 'SCOPE_PROCESS'
      );

      for (let i = 0; i < allScopes.length; i++) {
        const scopeId = this.tenantConfigurations[i].id;
        this.selectedConfigValues.onboardingConfigValues.scopeProcess.push(
          scopeId.toString()
        );

        this.selectedInvoiceScopes[scopeId] = true;
      }
    }
  }

  bindInvoiceScopeProcessData(scopes: any) {
    for (let i = 0; i < scopes.length; i++) {
      this.selectedInvoiceScopes[scopes[i]] = true;
    }
  }

  bindInvoiceProcessPriorities() {
    for (let i = 0; i < this.priorityValues.length; i++) {
      this.selectedPriorityValues[this.priorityKeys[i]] = this.priorityValues[
        i
      ];
    }
  }

  onScopeConfigurationsSelection(event: any, value: any, selectedValue: any) {
    if (selectedValue === 'All of the Above') {
      this.selectAlltheScopes(event);
    } else {
      if (event) {
        const index = this.selectedConfigValues.onboardingConfigValues.scopeProcess.findIndex(
          (x: any) => x == value
        );
        if (index == -1) {
          this.selectedConfigValues.onboardingConfigValues.scopeProcess.push(
            value.toString()
          );
        }
      } else {
        const index = this.selectedConfigValues.onboardingConfigValues.scopeProcess.findIndex(
          (x: any) => x == value
        );
        this.selectedConfigValues.onboardingConfigValues.scopeProcess.splice(
          index,
          1
        );
        const allModulesIndex = this.tenantConfigurations.findIndex(
          (x: any) => x.selectedValue == 'All of the Above'
        );

        const allModulesId = this.tenantConfigurations[allModulesIndex].id;
        const isAllModuleIndex = this.selectedConfigValues.onboardingConfigValues.scopeProcess.findIndex(
          (x: any) => x == allModulesId
        );
        if (isAllModuleIndex != -1) {
          this.selectedInvoiceScopes[allModulesId] = false;
          this.selectedConfigValues.onboardingConfigValues.scopeProcess.splice(
            isAllModuleIndex,
            1
          );
        }
      }
    }
  }

  onSectionConfigCodeSelection(
    event: any,
    value: any,
    selectedKey: any,
    position: any
  ) {
    this.invalidStep2 = false;
    const index = this.selectedConfigValues.onboardingConfigValues.invoiceProcessScope.findIndex(
      (x: any) => x.configCode == selectedKey
    );
    if (event) {
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.invoiceProcessScope.push(
          value.toString()
        );
        this.checkForprirotySelect(selectedKey, position);
      }
    } else {
      this.selectedConfigValues.onboardingConfigValues.invoiceProcessScope.splice(
        index,
        1
      );
      this.selectedPriorityValues[selectedKey] = '';
      delete this.selectedPriorityValues[selectedKey];
      for (
        var i = 0;
        i < this.selectedConfigValues.onboardingConfigValues.priority.length;
        i++
      ) {
        if (
          selectedKey ==
          this.selectedConfigValues.onboardingConfigValues.priority[i]
            .configCode
        ) {
          this.selectedConfigValues.onboardingConfigValues.priority.splice(
            i,
            1
          );
        }
      }
    }
  }
  checkForprirotySelect(selectedValue: any, position: any) {
    if (this.selectedConfigValues.onboardingConfigValues.priority.length == 0) {
      this.invalidStep3 = true;
      this.invoiceProcessName = selectedValue;
    } else {
      for (
        var i = 0;
        i < this.selectedConfigValues.onboardingConfigValues.priority.length;
        i++
      ) {
        if (
          selectedValue !=
          this.selectedConfigValues.onboardingConfigValues.priority[i]
        ) {
          this.invalidStep3 = true;
          this.invoiceProcessName = selectedValue;
        }
      }
    }
  }
  onSectionConfigPrioritySelection(event: any, value: any) {
    this.invalidStep3 = false;
    this.invalidStep2 = false;
    const index = this.selectedConfigValues.onboardingConfigValues.priority.findIndex(
      (x: any) => x.configCode == value
    );
    //    this.alertService.clear();
    if (event.target.value != '') {
      // if (
      //   this.selectedConfigValues.onboardingConfigValues.priority.length == 0
      // ) {
      //   if (event.target.value != 1) {
      //     this.alertService.error(
      //       'Value should be less than the selected value'
      //     );
      //     setTimeout(() => {
      //       this.alertService.clear();
      //     }, 2000);
      //     event.target.value = '';
      //     return;
      //   }
      // } else {
      //   const prevValueLength =
      //     this.selectedConfigValues.onboardingConfigValues.priority.length - 1;
      //   const proposedValue =
      //     this.selectedConfigValues.onboardingConfigValues.priority[
      //       prevValueLength
      //     ].configValue + 1;
      //   if (proposedValue != parseInt(event.target.value)) {
      //     this.alertService.error('Values should be in sequence');
      //     setTimeout(() => {
      //       this.alertService.clear();
      //     }, 2000);

      //     event.target.value = '';
      //     return;
      //   }
      // }
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.priority.push({
          configCode: value,
          configValue: parseInt(event.target.value)
        });
        this.selectedInvoiceProcessKeys[value] = true;
      } else {
        this.selectedConfigValues.onboardingConfigValues.priority[index][
          'configValue'
        ] = parseInt(event.target.value);
      }
    } else {
      if (index != -1) {
        this.selectedConfigValues.onboardingConfigValues.priority.splice(
          index,
          1
        );
        this.selectedInvoiceProcessKeys[value] = false;
      }
    }
  }

  submitConfiguration(): void {
    this.selectedConfigValues.pan = this.deductorPAN;
    //  console.log("submit onboarding", this.selectedConfigValues);
    this.onboardingService
      .postConfigurations(this.selectedConfigValues)
      .subscribe(
        (res: any) => {
          this.closePopup.emit(false);
        },
        (error: any) => {
          this.closePopup.emit(false);
        }
      );
  }

  roleFormBehaviorHandler(event: any): void {
    this.roleFormActionType = event.actionType;
    this.roleId = event.id ? event.id : undefined;
    this.moduleType = event.scopeType;
    this.showRoleForm = event.toggler;
    //  this.moduleType = event.scopeType;
  }

  closeRoles(event: any): void {
    this.closePopup.emit(false);
  }
  roleListBehaviorHandler(event: any): void {
    this.showRoleForm = event;
  }
}
