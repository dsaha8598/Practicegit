import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { OnboardingService26AS } from './onboarding26-as.service';

@Component({
  selector: 'ey-onboarding26-as',
  templateUrl: './onboarding26-as.component.html',
  styleUrls: ['./onboarding26-as.component.scss']
})
export class Onboarding26ASComponent implements OnInit {
  /*  selectedLevel1: boolean = false;
   selectedLevel2: boolean = false; */
  selectedReconProcessKeys: any = [];
  selectedPriorityValues: any = [];
  levelAggregateList: any = [];
  levelPerTransactionList: any = [];
  selectedInvoiceScopes: any = [];
  tenantConfigurations: any;
  priorityKeys: any = [];
  priorityValues: any = [];
  scopeForm: FormGroup;
  @Input() deductorPAN: string;
  @Output() closePopup: EventEmitter<boolean>;
  selectedConfigValues: any = {
    onboardingConfigValues: {
      priority: [],
      recon: [],
      tolerance: {
        levelAggregate: [],
        levelPerTransaction: []
      }
    },
    pan: ''
  };
  isEdit: boolean;
  invalidStep1: boolean;
  invalidStep2: boolean;
  invalidStep3: boolean;
  reconProcessName: any;
  constructor(private readonly onboardingService: OnboardingService26AS) {
    this.closePopup = new EventEmitter<boolean>();
  }

  ngOnInit() {
    this.getInitalScopeProcess();
    this.buildconfigurationsForm();
    /* Step 1:- get the inital configurations  */
  }

  getInitalScopeProcess(): void {
    /* this.onboardingService.get26ASOnboardingInfo().subscribe(
      (res: any) => { */
    this.tenantConfigurations = /* {
          "status": "OK",
          "requestStatus": "To get a Collector Look Up values",
          "statusMessage": "NO ALERT",
          "data": */ [
      {
        active: true,
        id: 1,
        selectedValue: 'TDS amount',
        module: 'PROCESS_DETERMINATION',
        isMultiSelection: 1
      },
      {
        active: true,
        id: 2,
        selectedValue: 'Transaction amount (without GST)',
        module: 'PROCESS_DETERMINATION',
        isMultiSelection: 1
      },
      {
        active: true,
        id: 3,
        selectedValue: 'Transaction amount (with GST)',
        module: 'PROCESS_DETERMINATION',
        isMultiSelection: 1
      }
    ];
    /*  } */

    /* res.data;
  console.log('printing look up tables data', this.tenantConfigurations);
  this.getOnboardingParametersInfo(); */
    /* },
    error => { }
  ); */
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

  buildconfigurationsForm() {
    this.scopeForm = new FormGroup({
      reconProcess: new FormControl('', [Validators.required]),
      priority: new FormControl('', [Validators.required]),
      levelAggregate: new FormControl('', [Validators.required]),
      levelPerTransaction: new FormControl('', [Validators.required])
    });
  }

  bindOnboardingParameters(data: any) {
    this.selectedConfigValues.onboardingConfigValues = data;
    this.priorityKeys = data.priority.map((x: any) => x.configCode);
    this.priorityValues = data.priority.map((x: any) => x.configValue);
    this.bindInvoiceProcessPriorities();
    this.bindInvoiceScopeProcessData(data.scopeProcess);
    this.bindInvoiceProcessKeys(this.priorityKeys);
  }

  bindInvoiceProcessKeys(processKeys: any) {
    for (let i = 0; i < processKeys.length; i++) {
      this.selectedReconProcessKeys[processKeys[i]] = true;
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

  onSectionConfigCodeSelection(
    event: any,
    value: any,
    selectedKey: any,
    position: any
  ) {
    const index = this.selectedConfigValues.onboardingConfigValues.recon.findIndex(
      (x: any) => x.configCode == selectedKey
    );
    if (event) {
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.recon.push(
          value.toString()
        );
        this.checkForprirotySelect(selectedKey, position);
      }
    } else {
      this.selectedConfigValues.onboardingConfigValues.recon.splice(index, 1);
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
      this.reconProcessName = selectedValue;
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
          this.reconProcessName = selectedValue;
        }
      }
    }
  }
  onSectionConfigPrioritySelection(event: any, value: any) {
    this.invalidStep3 = false;
    this.invalidStep2 = false;
    this.invalidStep1 = false;
    const index = this.selectedConfigValues.onboardingConfigValues.priority.findIndex(
      (x: any) => x.configCode == value
    );

    if (event.target.value != '') {
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.priority.push({
          configCode: value,
          configValue: parseInt(event.target.value)
        });
        this.selectedReconProcessKeys[value] = true;
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
        this.selectedReconProcessKeys[value] = false;
      }
    }
  }

  onlevel1change(event: any, value: any) {
    const index = this.selectedConfigValues.onboardingConfigValues.tolerance.levelAggregate.findIndex(
      (x: any) => x.configCode == value
    );

    if (event.target.value != '') {
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelAggregate.push(
          {
            configCode: value,
            configValue: parseInt(event.target.value)
          }
        );
        this.selectedReconProcessKeys[value] = true;
      } else {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelAggregate[
          index
        ]['configValue'] = parseInt(event.target.value);
      }
    } else {
      if (index != -1) {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelAggregate.splice(
          index,
          1
        );
        this.selectedReconProcessKeys[value] = false;
      }
    }
  }

  onlevel2change(event: any, value: any): void {
    const index = this.selectedConfigValues.onboardingConfigValues.tolerance.levelPerTransaction.findIndex(
      (x: any) => x.configCode == value
    );

    if (event.target.value != '') {
      if (index == -1) {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelPerTransaction.push(
          {
            configCode: value,
            configValue: parseInt(event.target.value)
          }
        );
        this.selectedReconProcessKeys[value] = true;
      } else {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelPerTransaction[
          index
        ]['configValue'] = parseInt(event.target.value);
      }
    } else {
      if (index != -1) {
        this.selectedConfigValues.onboardingConfigValues.tolerance.levelPerTransaction.splice(
          index,
          1
        );
        this.selectedReconProcessKeys[value] = false;
      }
    }
  }

  submitConfiguration(): void {
    if (this.selectedConfigValues.onboardingConfigValues.recon.length == 0) {
      this.invalidStep1 = true;
      return;
    } else if (
      this.selectedConfigValues.onboardingConfigValues.recon.length == 0 &&
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
    this.selectedConfigValues.pan = this.deductorPAN;
    console.log('submit onboarding', this.selectedConfigValues);
    console.log(this.levelAggregateList, this.levelPerTransactionList);
    /*  this.onboardingService
       .postConfigurations(this.selectedConfigValues)
       .subscribe(
         (res: any) => {
           this.closePopup.emit(false);
         },
         (error: any) => {
           this.closePopup.emit(false);
         }
       ); */
  }
}
