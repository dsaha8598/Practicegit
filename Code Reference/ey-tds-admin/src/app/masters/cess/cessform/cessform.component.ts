import { Component, OnInit } from '@angular/core';
import { FormGroup, Validators, FormControl, FormArray } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CessService } from '../cess.service';
import {
  NgbDateNativeAdapter,
  NgbDateAdapter
} from '@ng-bootstrap/ng-bootstrap';
import { UtilModule } from '@app/shared';
import { ICess } from '@app/shared/model/cess.model';
import { SelectItem } from 'primeng/api';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-cessform',
  templateUrl: './cessform.component.html',
  styleUrls: ['./cessform.component.scss']
})
export class CessformComponent implements OnInit {
  cessForm: FormGroup;
  isCessDetails: boolean;
  getCessTypeList: Array<SelectItem>;
  isCessApplicable: boolean;
  isInvalidApplicableData: boolean;
  isCessNotApplicable: boolean;
  showForm: boolean;
  getDeducteeStatusList: Array<SelectItem>;
  getDeducteeResidentialStatusList: Array<SelectItem>;
  getNatureofPaymentList: Array<SelectItem>;
  isSaved: boolean;
  submitted: boolean;
  actionState: string;
  headingMsg: string;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  minDate: any = undefined;
  constructor(
    private readonly cessService: CessService,
    private readonly actRoute: ActivatedRoute,
    private readonly route: Router,
    private logger: CustomLoggerService
  ) {}
  ngOnInit(): void {
    this.initialLoading();
  }
  initialLoading(): void {
    this.createCessForm();
    this.isCessDetails = this.isCessNotApplicable = this.showForm = false;
    this.isCessApplicable = true;
    this.getCessTypeList = [];
    this.getCessTypeList.push({ label: 'Select Cess', value: '' });
    this.getDeducteeResidentialStatusList = [];
    this.getDeducteeResidentialStatusList.push({
      label: 'Select residential status',
      value: ''
    });
    this.getDeducteeStatusList = [];
    this.getDeducteeStatusList.push({
      label: 'Select deductee status',
      value: ''
    });
    this.getNatureofPaymentList = [];
    this.getNatureofPaymentList.push({
      label: 'Select nature of payment',
      value: ''
    });
    this.headingMsg = 'Add';
    this.getCessType();
    this.getDeducteeStatus();
    this.getNatureOfPayment();
    this.getResidentialStatus();
    this.stateChecker();
    this.onChanges();
  }
  changeHandling() {
    if (this.cessForm.value.isCessApplicable) {
      this.cessForm.controls.cessTypeId.enable();
      this.cessForm.get('cessTypeId').setValidators(Validators.required);
      this.cessForm.get('cessTypeId').updateValueAndValidity();
      this.cessForm.controls.rate.enable();
      this.cessForm.get('rate').setValidators(Validators.required);
      this.cessForm.get('rate').updateValueAndValidity();
      this.cessForm.controls.basisOfCessDetails.disable();
      this.initCessDetails();
    } else {
      this.cessForm.controls.cessTypeId.disable();
      this.cessForm.controls.rate.disable();
      this.formArr.removeAt(0);
    }
  }
  disableYesFields() {
    this.cessForm.controls.basisOfCessDetails.disable();
  }
  changeHandler(): void {
    const cessApplicable = this.cessForm.value.isCessApplicable;
    this.isCessNotApplicable = this.isCessApplicable = false;
    if (cessApplicable) {
      this.isCessApplicable = true;
      this.cessForm.controls.basisOfCessDetails.disable();
      this.cessForm.controls.cessTypeId.enable();
      this.cessForm.get('cessTypeId').setValidators(Validators.required);
      this.cessForm.get('cessTypeId').updateValueAndValidity();
      this.cessForm.controls.rate.enable();
      this.cessForm.get('rate').setValidators(Validators.required);
      this.cessForm.get('rate').updateValueAndValidity();
      let checkValues = [
        'bocNatureOfPayment',
        'bocDeducteeStatus',
        'bocDeducteeResidentialStatus',
        'bocInvoiceSlab'
      ];
      checkValues.forEach(key => {
        this.cessForm.controls[key].patchValue(null);
      });
    } else {
      this.isCessNotApplicable = true;
      this.cessForm.controls['bocNatureOfPayment'].patchValue(true);
      this.cessForm.controls.cessTypeId.disable();
      this.cessForm.controls.rate.disable();
      this.cessForm.controls['basisOfCessDetails'].enable();
    }
  }
  handleData(e: any, field: any) {
    let checkValues = [
      'bocNatureOfPayment',
      'bocDeducteeStatus',
      'bocDeducteeResidentialStatus',
      'bocInvoiceSlab'
    ];
    let changeValue = 'false';
    checkValues.forEach(key => {
      if (this.cessForm.controls[key].value == true) {
        changeValue = 'true';
      }
    });
    if (changeValue == 'false') {
      this.cessForm.controls[field].patchValue(true);
    }
  }
  saveInitalCessData(): void {
    this.isCessNotApplicable = this.isCessApplicable = this.showForm = false;
    this.cessForm.controls.isCessApplicable.disable();
    const cessApplicable = this.cessForm.getRawValue().isCessApplicable;
    if (this.cessForm.getRawValue().isCessApplicable) {
      this.isCessApplicable = true;
      this.cessForm.controls['bocDeducteeStatus'].reset();
      this.cessForm.controls['bocDeducteeResidentialStatus'].reset();
      this.cessForm.controls['bocInvoiceSlab'].reset();
      this.cessForm.controls['bocNatureOfPayment'].reset();
      this.isCessDetails = false;
    } else if (!this.cessForm.getRawValue().isCessApplicable) {
      this.isCessNotApplicable = true;
      this.cessForm.controls['cessTypeId'].reset();
      this.cessForm.controls['rate'].reset();
      this.cessForm.controls.bocDeducteeResidentialStatus.disable();
      this.cessForm.controls.bocDeducteeStatus.disable();
      this.cessForm.controls.bocNatureOfPayment.disable();
      this.cessForm.controls.bocInvoiceSlab.disable();
      this.isCessDetails = true;
      this.showForm = true;
      this.requiredFieldHandler(0);
    }
    this.isSaved = true;
  }
  requiredFieldHandler(id: number): void {
    const control = this.cessForm.get('basisOfCessDetails') as FormArray;
    if (this.cessForm.get('bocDeducteeStatus').value) {
      control
        .at(id)
        .get('deducteeStatusId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('deducteeStatusId')
        .updateValueAndValidity();
    }
    if (this.cessForm.get('bocNatureOfPayment').value) {
      control
        .at(id)
        .get('natureOfPaymentMasterId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('natureOfPaymentMasterId')
        .updateValueAndValidity();
    }
    if (this.cessForm.get('bocDeducteeResidentialStatus').value) {
      control
        .at(id)
        .get('deducteeResidentialStatusId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('deducteeResidentialStatusId')
        .updateValueAndValidity();
    }
    if (this.cessForm.get('bocInvoiceSlab').value) {
      control
        .at(id)
        .get('invoiceSlabFrom')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('invoiceSlabFrom')
        .updateValueAndValidity();
      control
        .at(id)
        .get('invoiceSlabTo')
        .setValidators([]);
      control
        .at(id)
        .get('invoiceSlabTo')
        .updateValueAndValidity();
    }
    control
      .at(id)
      .get('cessTypeId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('cessTypeId')
      .updateValueAndValidity();
    control
      .at(id)
      .get('rate')
      .setValidators([Validators.required, Validators.maxLength(2)]);
    control
      .at(id)
      .get('rate')
      .updateValueAndValidity();
  }
  initCessDetails(): FormGroup {
    return new FormGroup({
      id: new FormControl(undefined),
      deducteeStatusId: new FormControl(''),
      deducteeResidentialStatusId: new FormControl(''),
      deducteeResidentialStatus: new FormControl(undefined),
      // residentialStatus: new FormControl(undefined),
      invoiceSlabFrom: new FormControl(''),
      invoiceSlabTo: new FormControl(''),
      natureOfPaymentMasterId: new FormControl(''),
      // natureOfPayment: new FormControl(undefined),
      nature: new FormControl(undefined),
      deducteeStatus: new FormControl(undefined),
      // status: new FormControl(undefined),
      rate: new FormControl(''),
      cessTypeId: new FormControl(''),
      cessTypeName: new FormControl(undefined)
    });
  }

  getCessType(): void {
    this.cessService.getCessTypeList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getCessTypeList = {
          label: result[i].cessType,
          value: result[i].id
        };
        this.getCessTypeList = [...this.getCessTypeList, getCessTypeList];
      }
    });
  }
  getDeducteeStatus(): void {
    this.cessService.getDeducteeStatusList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getDeducteeStatusList = {
          label: result[i].status,
          value: result[i].id
        };
        this.getDeducteeStatusList = [
          ...this.getDeducteeStatusList,
          getDeducteeStatusList
        ];
      }
    });
  }

  getResidentialStatus(): void {
    this.cessService.getResidentialStatusList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getDeducteeResidentialStatusList = {
          label: result[i].status,
          value: result[i].id
        };
        this.getDeducteeResidentialStatusList = [
          ...this.getDeducteeResidentialStatusList,
          getDeducteeResidentialStatusList
        ];
      }
    });
  }

  getNatureOfPayment(): void {
    this.cessService.getNOPList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getNatureofPaymentList = {
          label: result[i].nature,
          value: result[i].id
        };
        this.getNatureofPaymentList = [
          ...this.getNatureofPaymentList,
          getNatureofPaymentList
        ];
      }
    });
  }
  getCessDetails(form: any): FormControl {
    return form.controls.basisOfCessDetails.controls;
  }
  addCessDetails(): void {
    const control = this.cessForm.get('basisOfCessDetails') as FormArray;
    control.push(this.initCessDetails());
    this.requiredFieldHandler(control.length - 1);
  }
  removeCessDetails(i: number): void {
    const control = this.cessForm.get('basisOfCessDetails') as FormArray;
    control.removeAt(i);
  }
  onChanges(): void {
    this.cessForm.get('applicableTo').valueChanges.subscribe(val => {
      this.applicableToDateError = false;
    });
  }
  disablePreviousDate(date: any) {
    let nextDay = new Date(date);
    nextDay.setDate(nextDay.getDate() + 1);
    this.minDate = {
      year: nextDay.getFullYear(),
      month: nextDay.getMonth() + 1,
      day: nextDay.getDate()
    };
  }

  getFormattedDate(date1: any) {
    let date = new Date(date1);
    let year = date.getFullYear();
    let month = (1 + date.getMonth()).toString().padStart(2, '0');
    let day = date
      .getDate()
      .toString()
      .padStart(2, '0');
    return year + '-' + month + '-' + day;
  }
  saveCess(flow: any): void {
    this.submitted = true;
    if (this.cessForm.invalid) {
      return;
    }
    if (this.cessForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.cessForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.cessForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    // if (this.cessForm.controls.isCessApplicable.value === false) {
    //   let checkValues = [
    //     'bocNatureOfPayment',
    //     'bocDeducteeStatus',
    //     'bocDeducteeResidentialStatus',
    //     'bocInvoiceSlab'
    //   ];
    //   checkValues.forEach((val: any) => {
    //     if (this.cessForm.controls.val.value === null) {
    //       this.cessForm.controls['val'].patchValue(false);
    //     }
    //   })
    // }

    // if (this.cessForm.controls.bocNatureOfPayment.value === null) {
    //   this.cessForm.controls['bocNatureOfPayment'].patchValue(false);
    // }
    // if (this.cessForm.controls.bocDeducteeStatus.value === null) {
    //   this.cessForm.controls['bocDeducteeStatus'].patchValue(false);
    // }
    // if (this.cessForm.controls.bocDeducteeResidentialStatus.value === null) {
    //   this.cessForm.controls['bocDeducteeResidentialStatus'].patchValue(false);
    // }
    // if (this.cessForm.controls.bocInvoiceSlab.value === null) {
    //   this.cessForm.controls['bocInvoiceSlab'].patchValue(false);
    // }
    this.cessService
      .addCessMaster(this.cessForm.getRawValue() as ICess)
      .subscribe(
        (res: any) => {
          this.reset();
          window.scrollTo(0, 0);
          if (flow == 'back') {
            setTimeout(() => {
              this.back();
            }, 500);
          }
        },
        (error: any) => {
          window.scrollTo(0, 0);
        }
      );
  }
  updateCess(): void {
    // this.changeApplicableToStatus(1);
    this.submittedTo = true;
    if (this.cessForm.invalid) {
      return;
    }
    this.cessService
      .updateCessMaster(this.cessForm.getRawValue() as ICess)
      .subscribe(
        (res: any) => {
          this.reset();
          window.scrollTo(0, 0);
          setTimeout(() => {
            this.back();
          }, 500);
        },
        (error: any) => {
          window.scrollTo(0, 0);
        }
      );
  }
  back(): void {
    this.route.navigate(['/dashboard/masters/cess']);
  }
  reset(): void {
    UtilModule.reset(this.cessForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }
  fetchDatabyId(params: any): void {
    this.cessService.getCessMasterById(params.id).subscribe(
      (result: any) => {
        this.editDataBinder(result);
        if (result && result.isCessApplicable == true) {
          result.basisOfCessDetails = [];
        }
        this.cessForm.patchValue(result);
        this.disablePreviousDate(result.applicableFrom);
        if (result && result.applicableTo !== null) {
          this.cessForm.get('applicableTo').setValidators(Validators.required);
        }
        if (params.action.toUpperCase() === 'EDIT') {
          this.stateChanger('edit');
        } else {
          this.headingMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.cessForm,
            params.action,
            []
          );
        }
      },
      error => this.logger.error(error)
    );
  }
  get formArr() {
    return this.cessForm.get('basisOfCessDetails') as FormArray;
  }
  editDataBinder(data: any): void {
    if (data.isCessApplicable) {
      this.showForm = false;
      this.isCessApplicable = true;
      this.isCessNotApplicable = false;
      this.formArr.removeAt(0);
    } else {
      this.isCessNotApplicable = true;
      this.isCessApplicable = false;
      this.showForm = true;
      if (data.basisOfCessDetails.length > 0) {
        for (let i = 0; i < data.basisOfCessDetails.length - 1; i++) {
          this.addCessDetails();
        }
      }
    }
  }
  get f(): any {
    return this.cessForm.controls;
  }
  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.actionState = UtilModule.stateChanger(this.cessForm, 'New');
      } else if (
        params.action.toUpperCase() === 'EDIT' ||
        params.action.toUpperCase() === 'VIEW'
      ) {
        this.fetchDatabyId(params);
      }
    });
  }
  private createCessForm(): void {
    this.cessForm = new FormGroup({
      id: new FormControl(''),
      isCessApplicable: new FormControl(true, [Validators.required]),
      cessTypeId: new FormControl(undefined, Validators.required),
      rate: new FormControl(undefined, Validators.required),
      applicableFrom: new FormControl(undefined, [Validators.required]),
      applicableTo: new FormControl(undefined),
      bocNatureOfPayment: new FormControl(false),
      bocDeducteeStatus: new FormControl(false),
      bocDeducteeResidentialStatus: new FormControl(false),
      bocInvoiceSlab: new FormControl(false),
      cessTypeName: new FormControl(),
      basisOfCessDetails: new FormArray([this.initCessDetails()])
    });
  }
  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.cessForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
}
