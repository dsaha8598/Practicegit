import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { StorageService } from '@app/shell/authentication/storageservice';
import { OnboardingService } from './onboarding.service';
import { NatureofpaymentService } from '@app/masters/natureofpayment/natureofpayment.service';
import { element } from 'protractor';
@Component({
  selector: 'ey-onboarding',
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.scss']
})
export class OnboardingComponent implements OnInit {
  items: MenuItem[];
  activeIndex: number = 0;
  isInvoice: boolean;
  isFlag: boolean;
  scopeForm: FormGroup;
  invoiceForm: FormGroup;
  isChallanProcessing: boolean;
  selectedPrioritiesListData: any = [];
  selectedScopeConfigValues: any = [];
  isInvalidForm1 = false;
  selectedInvoiceTypes: any = [];
  isGlAccount = false;
  invoicePriority: any;
  poPriority: any;
  glPriority: any;
  enteredAccountNumber: any = [];
  isOnboardingView: boolean;
  sacPriority: any;
  vendorPriority: any;
  accountNum: any = [];
  isInvalidForm2 = false;
  roleFormActionType: string;
  actionType: string;
  selectedInvoiceConfigCodes: any = [];
  selectedInvoiceConfigValues: any = [];
  selectedConsolidationConfigValue: any = [];
  invoiceFileForm: FormGroup;
  selectedProvisionPeriods: any = [];
  prioritiesObj: any = [];
  selectedProvisionProcessings: any = [];
  selectedChallans: any = [];
  custTransform: boolean;
  rolespopup: boolean;
  roleId: string;
  showRoleForm: boolean;
  moduleType: string;
  @Input() deductorPAN: string;
  showSections: any;
  sectionsList: any;
  selectedSections: any;
  @Output() closePopup: EventEmitter<boolean>;
  isDividendProcessing: boolean;
  is15CACBProcessing: boolean;
  isClientMatrix: boolean = false;
  isCompanyDeductor: boolean = false;
  isBusinessMutualDeductor: boolean = false;

  constructor(
    private readonly onboardingService: OnboardingService,
    private readonly storageService: StorageService,
    private readonly nopMasterService: NatureofpaymentService
  ) {
    this.closePopup = new EventEmitter<boolean>();
  }

  ngOnInit(): void {
    this.custTransform = true;
    this.rolespopup = true;
    this.getSections();
    this.stepper();
    this.buildFormGroup();
    this.getOnboardingParameters();
    this.showSteps();
    this.prioritiesObj = [
      {
        key: 'InvoiceDesc'
      },
      {
        key: 'GLDesc'
      },
      {
        key: 'SACDesc'
      },
      {
        key: 'VendorMaster'
      },
      {
        key: 'PODesc'
      }
    ];

    this.invoiceForm.get('pertransactionlimit').valueChanges.subscribe(val => {
      setTimeout(() => {
        if (val === 'No') {
          this.showSections = false;
          this.selectedSections = [];
        } else {
          this.showSections = true;
          this.sectionsList.forEach((element: any) => {
            this.selectedSections.push(element.section);
          });
        }
      }, 1000);
    });
  }

  transactionLimitSectionSelectHandler(event: any, section: string): void {
    console.log(event, section);
  }

  getSections() {
    this.selectedSections = [];
    this.nopMasterService.getNatureOfPaymentList().subscribe(val => {
      this.sectionsList = val;
      this.sectionsList.forEach((element: any) => {
        this.selectedSections.push(element.section);
      });

      console.log(this.selectedSections);
    });
  }

  selectPriorities(res: any) {
    const object = Object.keys(res);
    const values = Object.values(res);
    res = object;
    this.selectedPrioritiesListData = [];
    for (let i = 0; i < res.length; i++) {
      //  console.log('inside select priorities...', res);
      if (res[i] === 'InvoiceDesc') {
        this.invoicePriority = values[i];
      } else if (res[i] === 'PODesc') {
        this.poPriority = values[i];
      } else if (res[i] === 'GLDesc') {
        this.glPriority = values[i];
      } else if (res[i] === 'SACDesc') {
        this.sacPriority = values[i];
      } else {
        this.vendorPriority = values[i];
      }
      this.selectedPrioritiesListData.push({
        configCode: res[i],
        configValue: values[i]
      });
      console.log('printing selected invoice', this.selectedPrioritiesListData);
    }
  }

  buildFormGroup(): void {
    this.scopeForm = new FormGroup({
      panValidation: new FormControl(false, [Validators.required]),
      InvoiceExcelUploading: new FormControl(false, [Validators.required]),
      ratePrediction: new FormControl(false, [Validators.required]),
      monthlyCompliance: new FormControl(false),
      quertarlyReturns: new FormControl(false),
      clause34a: new FormControl(false),
      traceDefaults: new FormControl(false),
      allAbove: new FormControl(false),
      dividend: new FormControl(false),
      generate15CACB: new FormControl(false),
      dvndFileForm15gh: new FormControl(false)
    });

    this.invoiceForm = new FormGroup({
      invoiceDescription: new FormControl(false, [Validators.required]),
      poDescription: new FormControl(false, [Validators.required]),
      glDescription: new FormControl(false, [Validators.required]),
      sac: new FormControl(false, [Validators.required]),
      vendorMaster: new FormControl(false, [Validators.required]),
      provisionProcessing: new FormControl('VENDORSECTION', [
        Validators.required
      ]),
      advanceProcessing: new FormControl('FIFO', [Validators.required]),
      invoicePriority: new FormControl(''),
      poPriority: new FormControl(''),
      glPriority: new FormControl(''),
      sacPriority: new FormControl(''),
      vendorPriority: new FormControl(''),
      provisionPeriod: new FormControl('Monthly', [Validators.required]),
      interestCalculationType: new FormControl('30days', [Validators.required]),
      roundoff: new FormControl('Decimal', [Validators.required]),
      pertransactionlimit: new FormControl('Yes', [Validators.required]),
      creditProcessing: new FormControl('A', [Validators.required]),
      invoiceProcessPriority: new FormControl(false, [Validators.required]),
      challanProcessing: new FormControl(false, [Validators.required]),
      dvndDdtPaidBeforeEOY: new FormControl(false, [Validators.required]),
      dvndPrepForm15CaCb: new FormControl('ONLY_PART_C_OF_FORM_15_CA', [
        Validators.required
      ]),
      clientRules_1_KEY: new FormControl(false, [Validators.required]),
      clientRules_1_All: new FormControl(false, [Validators.required]),
      clientRules_2_KEY: new FormControl(false, [Validators.required]),
      clientRules_2_All: new FormControl(false, [Validators.required]),
      clientRules_3_KEY: new FormControl(false, [Validators.required]),
      clientRules_3_All: new FormControl(false, [Validators.required]),
      clientRules_4_KEY: new FormControl(false, [Validators.required]),
      clientRules_4_All: new FormControl(false, [Validators.required]),
      clientRules_5_KEY: new FormControl(false, [Validators.required]),
      clientRules_5_All: new FormControl(false, [Validators.required]),
      clientRules_6_KEY: new FormControl(false, [Validators.required]),
      clientRules_6_All: new FormControl(false, [Validators.required])

      // accountNumber: new FormControl()
    });

    this.invoiceFileForm = new FormGroup({
      sap: new FormControl(false, [Validators.required]),
      pdf: new FormControl(false, [Validators.required]),
      excel: new FormControl(false, [Validators.required]),
      all: new FormControl(false, [Validators.required])
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

  changeValidations(event: any, id: any): void {
    if (event) {
      this.isInvoice = true;
      if (id === 2) {
        this.scopeForm.controls.InvoiceExcelUploading.setValidators(
          Validators.required
        );
        this.scopeForm.controls.InvoiceExcelUploading.updateValueAndValidity();
        this.scopeForm.controls.ratePrediction.clearValidators();
        this.scopeForm.controls.ratePrediction.updateValueAndValidity();
      } else {
        this.scopeForm.controls.ratePrediction.setValidators(
          Validators.required
        );
        this.scopeForm.controls.ratePrediction.updateValueAndValidity();
        this.scopeForm.controls.InvoiceExcelUploading.clearValidators();
        this.scopeForm.controls.InvoiceExcelUploading.updateValueAndValidity();
      }
      this.pushConfigValues(true, id, 1, '');
    } else {
      if (id === 3) {
        this.scopeForm.controls.clause34a.setValue(false);
        this.scopeForm.controls.traceDefaults.setValue(false);
        this.removeConfigValues(3, 1, '');
        this.removeConfigValues(6, 1, '');
        this.removeConfigValues(7, 1, '');
        this.removeConfigValues(4, 1, '');
      } else if (id === 2) {
        this.removeConfigValues(2, 1, '');
      } else if (id === 8) {
        this.removeConfigValues(8, 1, '');
      }
    }
    if (this.selectedScopeConfigValues.length === 10) {
      this.scopeForm.controls.allAbove.setValue(true);
    } else {
      this.scopeForm.controls.allAbove.setValue(false);
    }
    this.showSteps();
  }

  pushConfigValues(event: any, value: any, id: any, code: any): void {
    console.log('inside push...', event, value, id, code);
    console.log(this.selectedPrioritiesListData.length);
    console.log('after checking invoice', this.selectedPrioritiesListData);
    console.log(this.selectedInvoiceConfigValues.length);
    console.log('before', this.selectedInvoiceConfigValues);
    if (event) {
      if (id === 1) {
        const index = this.selectedScopeConfigValues.findIndex(
          (x: any) => x === value
        );
        if (index === -1) {
          this.selectedScopeConfigValues.push(value);
        }
        if (this.selectedScopeConfigValues.length >= 1) {
          this.isInvalidForm2 = false;
        }
        this.isGlAccount = this.selectedScopeConfigValues.includes(6);
        this.isChallanProcessing = this.selectedScopeConfigValues.includes(4);
        this.isDividendProcessing = this.selectedScopeConfigValues.includes(8); //Dividend
        this.is15CACBProcessing = this.selectedScopeConfigValues.includes(9); //15CACB processing
      } else {
        const index = this.selectedInvoiceConfigValues.findIndex(
          (x: any) => x === value
        );
        if (index === -1) {
          this.selectedInvoiceConfigValues.push(value);
          this.selectedInvoiceConfigCodes.push(code);
        }
      }
    } else {
      console.log('inside else going to remove');
      this.removeConfigValues(value, id, code);
      this.isDividendProcessing = this.selectedScopeConfigValues.includes(8); //Dividend
      this.is15CACBProcessing = this.selectedScopeConfigValues.includes(9); //15CACB processing
    }
    if (this.selectedScopeConfigValues.length == 10) {
      this.scopeForm.controls.allAbove.setValue(true);
    } else {
      this.scopeForm.controls.allAbove.setValue(false);
    }
  }

  selectedConsolidationValues(event: any, value: any): void {
    if (event) {
      if (value == 1) {
        const index = this.selectedConsolidationConfigValue.findIndex(
          (x: any) => x == value
        );
        if (index == -1) {
          this.selectedConsolidationConfigValue.push(value);
        }
      } else {
        const index = this.selectedChallans.findIndex((x: any) => x == value);
        if (index == -1) {
          this.selectedChallans.push(value);
        }
      }
    } else {
      if (value == 1) {
        const index = this.selectedConsolidationConfigValue.findIndex(
          (x: any) => x == value
        );
        if (index == -1) {
          this.selectedConsolidationConfigValue.splice(index, 1);
        }
      } else {
        const index = this.selectedChallans.findIndex((x: any) => x == value);
        if (index == -1) {
          this.selectedChallans.push(value);
        }
      }
    }
  }

  selectDeselectAllAbove(event: any): void {
    if (event) {
      this.isInvoice = true;
      this.isGlAccount = true;
      this.isDividendProcessing = true;
      this.is15CACBProcessing = true;
      this.isChallanProcessing = true;
      for (let i = 1; i <= 10; i++) {
        const index = this.selectedScopeConfigValues.findIndex(
          (x: any) => x == i
        );
        if (index == -1) {
          this.selectedScopeConfigValues.push(i);
        }
      }
      this.selectAllScopeControls();
    } else {
      this.isInvoice = false;
      this.isGlAccount = false;
      this.isChallanProcessing = false;
      this.isDividendProcessing = false;
      this.is15CACBProcessing = false;
      this.selectedScopeConfigValues = [];
      this.scopeForm.reset();
    }
  }

  selectAllScopeControls(): void {
    this.scopeForm.controls.clause34a.setValue(true);
    this.scopeForm.controls.panValidation.setValue(true);
    this.scopeForm.controls.InvoiceExcelUploading.setValue(true);
    this.scopeForm.controls.ratePrediction.setValue(true);
    this.scopeForm.controls.monthlyCompliance.setValue(true);
    this.scopeForm.controls.quertarlyReturns.setValue(true);
    this.scopeForm.controls.traceDefaults.setValue(true);
    this.scopeForm.controls.allAbove.setValue(true);
    this.scopeForm.controls.dividend.setValue(true);
    this.scopeForm.controls.generate15CACB.setValue(true);
    this.scopeForm.controls.dvndFileForm15gh.setValue(true);
  }

  pushInvoiceTypes(event: any, value: any): void {
    const index = this.selectedInvoiceTypes.findIndex((x: any) => x === value);
    if (event) {
      if (index === -1) {
        this.selectedInvoiceTypes.push(value);
      }
    } else {
      this.selectedInvoiceTypes.splice(index, 1);
    }
    if (this.selectedInvoiceTypes.length === 3) {
      this.invoiceFileForm.controls.all.setValue(true);
    } else {
      this.invoiceFileForm.controls.all.setValue(false);
    }
  }

  selectedPriorities(event: any, key: any): void {
    console.log('inside selected priorites', event.target.value, key);
    const index = this.selectedPrioritiesListData.findIndex(
      (x: any) => x.key == key
    );
    if (index === -1) {
      this.selectedPrioritiesListData.push({
        configCode: key,
        configValue: parseInt(event.target.value)
      });
      console.log('inside selected if', this.selectedPrioritiesListData);
    } else {
      this.selectedPrioritiesListData[index].priority = event.target.value;
      console.log('else', this.selectedPrioritiesListData[index].priority);
    }
  }

  selectallInvoiceTypes(event: any): void {
    if (event) {
      for (let i = 1; i <= 3; i++) {
        const index = this.selectedInvoiceTypes.findIndex((x: any) => x == i);
        if (index == -1) {
          this.selectedInvoiceTypes.push(i);
        }
      }
      this.selectInvoiceTypesAll();
    } else {
      this.selectedInvoiceTypes = [];
      this.invoiceFileForm.reset();
    }
  }

  selectInvoiceTypesAll(): void {
    this.invoiceFileForm.controls.sap.setValue(true);
    this.invoiceFileForm.controls.excel.setValue(true);
    this.invoiceFileForm.controls.pdf.setValue(true);
    this.invoiceFileForm.controls.all.setValue(true);
  }

  removeConfigValues(value: any, id: any, code: any): void {
    console.log('inside remove config values', value, id);
    console.log(this.selectedPrioritiesListData, 'before splash');
    console.log(this.selectedInvoiceConfigValues, 'before');
    if (id === 1) {
      const index = this.selectedScopeConfigValues.findIndex(
        (x: any) => x === value
      );
      if (index !== -1) {
        this.selectedScopeConfigValues.splice(index, 1);
      }
      if (value === 1 || value === 3) {
        this.scopeForm.controls.monthlyCompliance.setValue(false);
        const monthlyComplianceIndex = this.selectedScopeConfigValues.findIndex(
          (x: any) => x === 4
        );
        if (monthlyComplianceIndex !== -1) {
          this.selectedScopeConfigValues.splice(monthlyComplianceIndex, 1);
        }
      }
    } else {
      const index = this.selectedInvoiceConfigValues.findIndex(
        (x: any) => x === value
      );
      console.log(index);
      if (index !== -1) {
        this.selectedInvoiceConfigValues.splice(index, 1);
        for (let i = 0; i < this.selectedPrioritiesListData.length; i++) {
          if (code === this.selectedPrioritiesListData[i].configCode) {
            this.selectedPrioritiesListData.splice(i, 1);
          }
        }
        if (code === 'InvoiceDesc') {
          this.invoiceForm.controls['invoicePriority'].setValue('');
        } else if (code === 'PODesc') {
          this.invoiceForm.controls['poPriority'].setValue('');
        } else if (code === 'GLDesc') {
          this.invoiceForm.controls['glPriority'].setValue('');
        } else if (code === 'SACDesc') {
          this.invoiceForm.controls['sacPriority'].setValue('');
        } else if (code === 'VendorMaster') {
          this.invoiceForm.controls['vendorPriority'].setValue('');
        }
      }
      console.log(this.selectedPrioritiesListData, 'after splice');
      console.log(this.selectedInvoiceConfigValues, 'after');
    }
  }

  onSubmit(id: any): void {
    const value1 = this.selectedScopeConfigValues.includes(2);
    const value2 = this.selectedScopeConfigValues.includes(3);
    const value3 = this.selectedScopeConfigValues.includes(8); //Dividend
    if (value1 || value2 || value3) {
      if (id === 0) {
        this.activeIndex = this.activeIndex - 1;
      } else if (id === 1) {
        if (
          this.selectedScopeConfigValues.length === 0 ||
          (this.selectedInvoiceTypes.length === 0 && this.isFlag)
        ) {
          this.isInvalidForm1 = true;
        } else {
          this.isInvalidForm1 = false;
          this.activeIndex = id;
        }
      } else {
        if (
          this.selectedInvoiceConfigValues.length < 3 ||
          (this.selectedPrioritiesListData.length < 2 && this.isFlag)
        ) {
          this.isInvalidForm2 = true;
        } else {
          this.isInvalidForm2 = false;
          this.submitConfiguration();
        }
      }
    } else {
      if (this.selectedScopeConfigValues.length === 0) {
        this.isInvalidForm1 = true;
      } else {
        this.submitConfiguration();
      }
    }
  }

  filteringAccountNumber(): void {
    let accountKeys = [];
    for (let i = 0; i < this.accountNum.length; i++) {
      accountKeys.push(this.accountNum[i].value);
    }
    this.enteredAccountNumber = accountKeys;
    for (let i = 0; i < this.selectedInvoiceConfigValues.length; i++) {
      const priorityIndex = this.selectedInvoiceConfigValues.findIndex(
        (x: any) => x.configValue === i
      );
      if (priorityIndex === -1) {
        const codeIndex = this.selectedPrioritiesListData.findIndex(
          (x: any) => x.configCode === this.selectedInvoiceConfigCodes[i]
        );
        if (codeIndex === -1) {
          this.selectedPrioritiesListData.push({
            configCode: this.selectedInvoiceConfigCodes[i],
            configValue: this.selectedInvoiceConfigValues[i]
          });
        }
      }
    }
  }

  submitConfiguration(): void {
    if (!this.isOnboardingView) {
      this.filteringAccountNumber();
    }
    const payLoad = {
      pan: this.deductorPAN,
      onboardingConfigValues: {
        ipp: this.selectedScopeConfigValues,
        ppa: this.selectedInvoiceConfigValues,
        cnp: this.invoiceForm.controls.creditProcessing.value,
        tif: this.selectedInvoiceTypes,
        cp: this.invoiceForm.controls.challanProcessing.value,
        roundoff: this.invoiceForm.controls.roundoff.value,
        pertransactionlimit: this.invoiceForm.controls.pertransactionlimit
          .value,
        selectedSectionsForTransactionLimit: this.selectedSections,
        interestCalculationType: this.invoiceForm.controls
          .interestCalculationType.value,
        provisionTracking: this.invoiceForm.controls.provisionPeriod.value,
        provisionProcessing: this.invoiceForm.controls.provisionProcessing
          .value,
        advanceProcessing: this.invoiceForm.controls.advanceProcessing.value,
        priority: this.selectedPrioritiesListData,
        generate15CACB: this.scopeForm.controls.generate15CACB.value,
        dvndFileForm15gh: this.scopeForm.controls.dvndFileForm15gh.value,
        dvndEnabled: this.scopeForm.controls.dividend.value,
        dvndClientSpecificRules: this.seClientRulesMatrix(),
        dvndPrepForm15CaCb: this.invoiceForm.controls.dvndPrepForm15CaCb.value,
        dvndDdtPaidBeforeEOY: this.invoiceForm.controls.dvndDdtPaidBeforeEOY
          .value
      }
    };
    console.log(JSON.stringify(payLoad));
    this.onboardingService.postConfigurations(payLoad).subscribe(
      (res: any) => {
        this.selectedScopeConfigValues = [];
        this.selectedInvoiceConfigValues = [];
        this.selectedConsolidationConfigValue = [];
        this.selectedInvoiceTypes = [];
        this.invoiceForm.controls.provisionPeriod.setValue(false);
        this.invoiceForm.controls.provisionProcessing.setValue(false);
        this.invoiceForm.controls.advanceProcessing.setValue(false);
        this.closePopup.emit(false);
      },
      (error: any) => {
        this.closePopup.emit(false);
      }
    );
  }
  seClientRulesMatrix() {
    if (this.isCompanyDeductor) {
      let payLoad = {
        TREATY_BENEFITS_IN_ABSENCE_OF_DOCS: this.setRules1(),
        TREATY_BENEFITS_BY_INDEMNITY: this.setRules2(),
        TREATY_BENEFITS_AS_PER_MFN_CLAUSE: this.setRules3(),
        TREATY_BENEFITS_TO_FII_FPI_GDR: this.setRules4(),
        APPLY_SURCHARGE_AND_CESS_AS_PER_LDC: this.setRules6()
      };
      return payLoad;
    } else if (this.isBusinessMutualDeductor) {
      let payLoad = {
        TREATY_BENEFITS_IN_ABSENCE_OF_DOCS: this.setRules1(),
        TREATY_BENEFITS_BY_INDEMNITY: this.setRules2(),
        TREATY_BENEFITS_AS_PER_MFN_CLAUSE: this.setRules3(),
        TREATY_BENEFITS_TO_MUTUAL_FUND_BUSINESS_TRUST_DEDUCTOR_TYPE: this.setRules5(),
        APPLY_SURCHARGE_AND_CESS_AS_PER_LDC: this.setRules6()
      };
      return payLoad;
    } else {
      let payLoad = {
        TREATY_BENEFITS_IN_ABSENCE_OF_DOCS: this.setRules1(),
        TREATY_BENEFITS_BY_INDEMNITY: this.setRules2(),
        TREATY_BENEFITS_AS_PER_MFN_CLAUSE: this.setRules3(),
        APPLY_SURCHARGE_AND_CESS_AS_PER_LDC: this.setRules6()
      };
      return payLoad;
    }
  }

  setRules1() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_1_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_1_All.value
    };
    return payLoad;
  }
  setRules2() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_2_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_2_All.value
    };
    return payLoad;
  }
  setRules3() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_3_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_3_All.value
    };
    return payLoad;
  }
  setRules4() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_4_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_4_All.value
    };
    return payLoad;
  }
  setRules5() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_5_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_5_All.value
    };
    return payLoad;
  }
  setRules6() {
    let payLoad = {
      keyStrategicShareholders: this.invoiceForm.controls.clientRules_6_KEY
        .value,
      allShareholders: this.invoiceForm.controls.clientRules_6_All.value
    };
    return payLoad;
  }

  roleFormBehaviorHandler(event: any): void {
    this.roleFormActionType = event.actionType;
    this.roleId = event.id ? event.id : undefined;
    this.moduleType = event.scopeType;
    this.showRoleForm = event.toggler;
  }
  closeRoles(event: any): void {
    this.closePopup.emit(false);
  }

  roleListBehaviorHandler(event: any): void {
    this.showRoleForm = event;
  }

  getOnboardingParameters() {
    this.onboardingService.getConfigurations(this.deductorPAN).subscribe(
      (res: any) => {
        if (res.data !== null) {
          this.isOnboardingView = true;
          this.selectDividendProperties(res);
          this.set15CACBProperties(res);
          this.selectScopeConfigutions(res.data.ipp, res);
          this.selectInvoiceTypes(res.data.tif);
          this.selectInvoiceProcesses(res.data.ppa);
          this.selectPriorities(res.data.priority);
          this.selectProvisionTrackingAccountNumber(
            res.data.provisionTracking,
            res.data.provisionProcessing,
            res.data.advanceProcessing,
            res.data.cnp,
            res.data.cp,
            res.data.roundoff,
            res.data.pertransactionlimit,
            res.data.interestCalculationType
          );
          if (res.data.pertransactionlimit === 'Yes') {
            this.selectedSections =
              res.data.selectedSectionsForTransactionLimit;
          } else {
            this.selectedSections = [];
          }
          //    console.log(res.data);
        } else {
          this.isOnboardingView = false;
        }
      },
      error => {}
    );
  }

  selectScopeConfigutions(res: any, forForm15GH: any) {
    this.selectedScopeConfigValues = res;
    this.showSteps();
    for (let i = 0; i < this.selectedScopeConfigValues.length; i++) {
      if (this.selectedScopeConfigValues[i] == 1) {
        this.scopeForm.controls.panValidation.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 2) {
        this.scopeForm.controls.InvoiceExcelUploading.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 3) {
        this.scopeForm.controls.ratePrediction.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 4) {
        this.scopeForm.controls.monthlyCompliance.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 5) {
        this.scopeForm.controls.quertarlyReturns.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 6) {
        this.scopeForm.controls.clause34a.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 8) {
        this.scopeForm.controls.dividend.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 9) {
        this.scopeForm.controls.generate15CACB.setValue(true);
      } else if (this.selectedScopeConfigValues[i] == 10) {
        if (
          forForm15GH.data.dvndFileForm15gh != null &&
          forForm15GH.data.dvndFileForm15gh != undefined
        ) {
          this.scopeForm.controls.dvndFileForm15gh.setValue(
            forForm15GH.data.dvndFileForm15gh
          );
        }
      } else {
        this.scopeForm.controls.traceDefaults.setValue(true);
      }
    }
    if (this.selectedScopeConfigValues.length == 10) {
      this.scopeForm.controls.allAbove.setValue(true);
    } else {
      this.scopeForm.controls.allAbove.setValue(false);
    }
  }

  showSteps() {
    if (
      this.selectedScopeConfigValues.length != 0 &&
      this.selectedScopeConfigValues != null
    ) {
      const value1 = this.selectedScopeConfigValues.includes(2);
      const value2 = this.selectedScopeConfigValues.includes(3);
      this.isGlAccount = this.selectedScopeConfigValues.includes(6);
      this.isChallanProcessing = this.selectedScopeConfigValues.includes(4);
      this.isDividendProcessing = this.selectedScopeConfigValues.includes(8); //Dividend
      this.is15CACBProcessing = this.selectedScopeConfigValues.includes(9); //Dividend
      if (!value1 && !value2) {
        this.isFlag = false;
      } else {
        this.isFlag = true;
      }

      if (!value1 && !value2 && !this.isDividendProcessing) {
        this.isInvoice = false;
        this.items.splice(1, 1);
      } else {
        this.isInvoice = true;
        const stepIndex = this.items.findIndex(
          (x: any) => x.label == 'Processing'
        );
        if (stepIndex == -1) {
          this.items.push({
            label: 'Processing',
            command: (event: any) => {
              this.activeIndex = 1;
            }
          });
        }
      }
    } else {
      this.items.splice(1, 1);
      this.isInvoice = false;
      this.isInvoice = false;
      this.isGlAccount = false;
      this.isChallanProcessing = false;
      this.isDividendProcessing = false;
      this.is15CACBProcessing = false;
      this.selectedScopeConfigValues = [];
      this.scopeForm.reset();
    }
  }

  selectInvoiceTypes(res: any) {
    this.selectedInvoiceTypes = res;
    for (let i = 0; i < this.selectedInvoiceTypes.length; i++) {
      if (this.selectedInvoiceTypes[i] == 1) {
        this.invoiceFileForm.controls.sap.setValue(true);
      } else if (this.selectedInvoiceTypes[i] == 2) {
        this.invoiceFileForm.controls.excel.setValue(true);
      } else {
        this.invoiceFileForm.controls.pdf.setValue(true);
      }
    }
    if (this.selectedInvoiceTypes.length == 3) {
      this.invoiceFileForm.controls.all.setValue(true);
    }
  }

  selectInvoiceProcesses(res: any) {
    this.selectedInvoiceConfigValues = res;
    for (let i = 0; i < this.selectedInvoiceConfigValues.length; i++) {
      if (this.selectedInvoiceConfigValues[i] === 1) {
        this.invoiceForm.controls.invoiceDescription.setValue(true);
      } else if (this.selectedInvoiceConfigValues[i] === 2) {
        this.invoiceForm.controls.poDescription.setValue(true);
      } else if (this.selectedInvoiceConfigValues[i] === 3) {
        this.invoiceForm.controls.glDescription.setValue(true);
      } else if (this.selectedInvoiceConfigValues[i] === 4) {
        this.invoiceForm.controls.sac.setValue(true);
      } else if (this.selectedInvoiceConfigValues[i] === 5) {
        this.invoiceForm.controls.vendorMaster.setValue(true);
      }
    }
  }

  selectProvisionTrackingAccountNumber(
    provisionPeriod: any,
    provisionProcessing: any,
    advanceProcessing: any,
    creditNoteProcessing: any,
    challanProcessing: any,
    roundoff: any,
    pertransactionlimit: boolean,
    interestCalculationType: any
  ) {
    this.invoiceForm.controls.provisionPeriod.setValue(provisionPeriod);
    this.invoiceForm.controls.challanProcessing.setValue(challanProcessing);
    this.invoiceForm.controls.provisionProcessing.setValue(provisionProcessing);
    this.invoiceForm.controls.advanceProcessing.setValue(advanceProcessing);
    this.invoiceForm.controls.creditProcessing.setValue(creditNoteProcessing);
    this.invoiceForm.controls.roundoff.setValue(roundoff);
    this.invoiceForm.controls.pertransactionlimit.setValue(pertransactionlimit);

    this.invoiceForm.controls.interestCalculationType.setValue(
      interestCalculationType
    );
  }
  selectDividendProperties(res: any) {
    let dvndOnboardingMetaData = res.data;
    if (dvndOnboardingMetaData.dvndDeductorTypeName === 'Company') {
      this.isCompanyDeductor = true;
    } else if (
      dvndOnboardingMetaData.dvndDeductorTypeName === 'Business Trust' ||
      dvndOnboardingMetaData.dvndDeductorTypeName === 'Mutual Fund'
    ) {
      this.isBusinessMutualDeductor = true;
    } else {
      this.isCompanyDeductor = false;
      this.isBusinessMutualDeductor = false;
    }

    if (res.data.dvndEnabled) {
      this.isDividendProcessing = true;
      this.scopeForm.controls.dividend.setValue(true);
      this.invoiceForm.controls.clientRules_1_KEY.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_IN_ABSENCE_OF_DOCS.keyStrategicShareholders
      );
      this.invoiceForm.controls.clientRules_1_All.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_IN_ABSENCE_OF_DOCS.allShareholders
      );
      this.invoiceForm.controls.clientRules_2_KEY.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_BY_INDEMNITY.keyStrategicShareholders
      );
      this.invoiceForm.controls.clientRules_2_All.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_BY_INDEMNITY.allShareholders
      );
      this.invoiceForm.controls.clientRules_3_KEY.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_AS_PER_MFN_CLAUSE.keyStrategicShareholders
      );
      this.invoiceForm.controls.clientRules_3_All.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .TREATY_BENEFITS_AS_PER_MFN_CLAUSE.allShareholders
      );
      if (dvndOnboardingMetaData.dvndDeductorTypeName === 'Company') {
        if (
          dvndOnboardingMetaData.dvndClientSpecificRules
            .TREATY_BENEFITS_TO_FII_FPI_GDR != null
        ) {
          this.invoiceForm.controls.clientRules_4_KEY.setValue(
            dvndOnboardingMetaData.dvndClientSpecificRules
              .TREATY_BENEFITS_TO_FII_FPI_GDR.keyStrategicShareholders
          );
          this.invoiceForm.controls.clientRules_4_All.setValue(
            dvndOnboardingMetaData.dvndClientSpecificRules
              .TREATY_BENEFITS_TO_FII_FPI_GDR.allShareholders
          );
        }
      } else if (
        dvndOnboardingMetaData.dvndDeductorTypeName === 'Business Trust' ||
        dvndOnboardingMetaData.dvndDeductorTypeName === 'Mutual Fund'
      ) {
        if (
          dvndOnboardingMetaData.dvndClientSpecificRules
            .TREATY_BENEFITS_TO_MUTUAL_FUND_BUSINESS_TRUST_DEDUCTOR_TYPE != null
        ) {
          this.invoiceForm.controls.clientRules_5_KEY.setValue(
            dvndOnboardingMetaData.dvndClientSpecificRules
              .TREATY_BENEFITS_TO_MUTUAL_FUND_BUSINESS_TRUST_DEDUCTOR_TYPE
              .keyStrategicShareholders
          );
          this.invoiceForm.controls.clientRules_5_All.setValue(
            dvndOnboardingMetaData.dvndClientSpecificRules
              .TREATY_BENEFITS_TO_MUTUAL_FUND_BUSINESS_TRUST_DEDUCTOR_TYPE
              .allShareholders
          );
        }
      }
      this.invoiceForm.controls.clientRules_6_KEY.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .APPLY_SURCHARGE_AND_CESS_AS_PER_LDC.keyStrategicShareholders
      );
      this.invoiceForm.controls.clientRules_6_All.setValue(
        dvndOnboardingMetaData.dvndClientSpecificRules
          .APPLY_SURCHARGE_AND_CESS_AS_PER_LDC.allShareholders
      );
      this.invoiceForm.controls.dvndDdtPaidBeforeEOY.setValue(
        dvndOnboardingMetaData.dvndDdtPaidBeforeEOY
      );
    }
  }
  set15CACBProperties(res: any) {
    if (res.data.ipp.includes(9)) {
      this.is15CACBProcessing = true;
      this.scopeForm.controls.generate15CACB.setValue(true);
      this.invoiceForm.controls.dvndPrepForm15CaCb.setValue(
        res.data.dvndPrepForm15CaCb
      );
    }
  }
}
