import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  Validators,
  FormControl,
  FormArray
} from '@angular/forms';
import { NgbCalendar, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { SurchargeService } from '../surcharge.service';
import { UtilModule } from '@app/shared';
import { ISurcharge } from '@app/shared/model/surchage.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-surchargeform',
  templateUrl: './surchargeform.component.html',
  styleUrls: ['./surchargeform.component.scss']
})
export class SurchargeformComponent implements OnInit {
  surchargeForm: FormGroup;
  isSurchargeDetails: boolean;
  getSurchargeTypeList: Array<any>;
  isSurchargeApplicable: boolean;
  isSurchargeNotApplicable: boolean;
  showForm: boolean;
  pageParams: any;
  minDate: any = undefined;
  getDeducteeStatusList: Array<any>;
  getDeducteeResidentialStatusList: Array<any>;
  getNatureofPaymentList: Array<any>;
  getCategoryShareholderList: Array<any>;
  getTypeOfShareholderList: Array<any>;
  isSaved: boolean;
  submitted: boolean;
  submittedTo: boolean = false;
  actionState: string;
  headingMsg: string;
  model: NgbDateStruct;
  applicableToDateError: boolean;
  constructor(
    private readonly surchargeService: SurchargeService,
    private readonly actRoute: ActivatedRoute,
    private readonly route: Router,
    private calendar: NgbCalendar,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.createSurchargeForm();
    this.headingMsg = 'Add';
    this.isSaved = false;
    this.isSurchargeDetails = this.isSurchargeNotApplicable = this.showForm = false;
    this.isSurchargeApplicable = true;
    this.getDeducteeResidentialStatusList = [];
    this.getDeducteeResidentialStatusList.push({
      label: 'Select deductee residential status',
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
    this.getCategoryShareholderList = [];
    this.getCategoryShareholderList.push({
      label: 'Select category of shareholder',
      value: ''
    });
    this.getTypeOfShareholderList = [];
    this.getTypeOfShareholderList.push({
      label: 'Select beneficiary type',
      value: ''
    });
    this.getNatureOfPayment();
    this.getDeducteeStatus();
    this.getResidentialStatus();
    this.getShareholderCategory();
    this.getTypeOfShareholder();
    this.stateChecker();
    this.onChanges();
    this.changeHandler();
  }

  changeHandler(): void {
    const surchargeApplicable = this.surchargeForm.get('surchargeApplicable')
      .value;
    this.isSurchargeNotApplicable = this.isSurchargeApplicable = false;
    if (surchargeApplicable) {
      this.isSurchargeApplicable = true;
      let checkValues = [
        'bocNatureOfPayment',
        'bocDeducteeStatus',
        'bocDeducteeResidentialStatus',
        'bocInvoiceSlab'
      ];
      checkValues.forEach(key => {
        this.surchargeForm.controls[key].patchValue(null);
      });
      this.surchargeForm
        .get('surchargeRate')
        .setValidators([Validators.required, Validators.maxLength(2)]);
      this.surchargeForm.get('surchargeRate').updateValueAndValidity();
      this.surchargeForm.patchValue({ bocDeducteeStatus: false });
    } else {
      this.isSurchargeNotApplicable = true;
      this.surchargeForm.controls['bocNatureOfPayment'].patchValue(true);
    }
  }

  createSurchargeForm(): void {
    this.surchargeForm = new FormGroup({
      id: new FormControl(undefined),
      surchargeApplicable: new FormControl(true, [Validators.required]),
      surchargeRate: new FormControl(undefined),
      applicableFrom: new FormControl(undefined, [Validators.required]),
      applicableTo: new FormControl(undefined),
      bocNatureOfPayment: new FormControl(false),
      bocDeducteeStatus: new FormControl(false),
      bocDeducteeResidentialStatus: new FormControl(false),
      bocInvoiceSlab: new FormControl(false),
      bocShareholderCatagory: new FormControl(false),
      bocShareholderType: new FormControl(false),
      basisOfSurchargeDetails: new FormArray([this.initSurchargeDetails()])
    });
  }
  handleData(e: any, field: any): void {
    const checkValues = [
      'bocNatureOfPayment',
      'bocDeducteeStatus',
      'bocDeducteeResidentialStatus',
      'bocInvoiceSlab',
      'bocShareholderCatagory',
      'bocShareholderType'
    ];
    let changeValue = false;
    checkValues.forEach(key => {
      if (this.surchargeForm.controls[key].value === true) {
        changeValue = true;
      }
    });
    if (!changeValue) {
      this.surchargeForm.controls[field].patchValue(true);
    }
  }
  saveInitalSurchargeData(): void {
    this.isSurchargeNotApplicable = this.isSurchargeApplicable = this.showForm = false;
    this.surchargeForm.controls.surchargeApplicable.disable();
    this.surchargeForm.controls.surchargeRate.disable();
    const surchargeApplicable = this.surchargeForm.getRawValue()
      .surchargeApplicable;
    if (surchargeApplicable) {
      this.isSurchargeApplicable = true;
      this.surchargeForm.controls['bocDeducteeStatus'].reset();
      this.surchargeForm.controls['bocDeducteeResidentialStatus'].reset();
      this.surchargeForm.controls['bocInvoiceSlab'].reset();
      this.surchargeForm.controls['bocNatureOfPayment'].reset();
      this.surchargeForm.controls['bocShareholderCatagory'].reset();
      this.surchargeForm.controls['bocShareholderType'].reset();
      this.isSurchargeDetails = false;
    } else if (!surchargeApplicable) {
      this.isSurchargeNotApplicable = true;
      this.surchargeForm.controls['surchargeRate'].reset();
      this.surchargeForm.controls.bocDeducteeResidentialStatus.disable();
      this.surchargeForm.controls.bocDeducteeStatus.disable();
      this.surchargeForm.controls.bocNatureOfPayment.disable();
      this.surchargeForm.controls.bocInvoiceSlab.disable();
      this.surchargeForm.controls.bocShareholderCatagory.disable();
      this.surchargeForm.controls.bocShareholderType.disable();
      this.isSurchargeDetails = true;
      this.showForm = true;
      this.requiredFieldHandler(0);
    }
    this.isSaved = true;
  }
  requiredFieldHandler(id: number): void {
    const control = this.surchargeForm.get(
      'basisOfSurchargeDetails'
    ) as FormArray;
    if (this.surchargeForm.getRawValue().bocDeducteeStatus) {
      control
        .at(id)
        .get('deducteeStatusId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('deducteeStatusId')
        .updateValueAndValidity();
    }
    if (this.surchargeForm.getRawValue().bocNatureOfPayment) {
      control
        .at(id)
        .get('natureOfPaymentMasterId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('natureOfPaymentMasterId')
        .updateValueAndValidity();
    }
    if (this.surchargeForm.getRawValue().bocDeducteeResidentialStatus) {
      control
        .at(id)
        .get('deducteeResidentialStatusId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('deducteeResidentialStatusId')
        .updateValueAndValidity();
    }
    if (this.surchargeForm.getRawValue().bocInvoiceSlab) {
      control
        .at(id)
        .get('invoiceSlabFrom')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('invoiceSlabFrom')
        .updateValueAndValidity();
      // control
      //   .at(id)
      //   .get('invoiceSlabTo')
      //   .setValidators([Validators.required]);
      // control
      //   .at(id)
      //   .get('invoiceSlabTo')
      //   .updateValueAndValidity();
    }
    if (this.surchargeForm.getRawValue().bocShareholderCatagory) {
      control
        .at(id)
        .get('shareholderCatagoryId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('shareholderCatagoryId')
        .updateValueAndValidity();
    }
    if (this.surchargeForm.getRawValue().bocShareholderType) {
      control
        .at(id)
        .get('shareholderTypeId')
        .setValidators([Validators.required]);
      control
        .at(id)
        .get('shareholderTypeId')
        .updateValueAndValidity();
    }
    control
      .at(id)
      .get('rate')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('rate')
      .updateValueAndValidity();
  }

  initSurchargeDetails(): FormGroup {
    return new FormGroup({
      id: new FormControl(undefined),
      deducteeStatusId: new FormControl(''),
      deducteeResidentialStatusId: new FormControl(''),
      invoiceSlabFrom: new FormControl(''),
      invoiceSlabTo: new FormControl(''),
      natureOfPaymentMasterId: new FormControl(''),
      nature: new FormControl(undefined),
      status: new FormControl(undefined),
      residentStatus: new FormControl(undefined),
      rate: new FormControl(''),
      shareholderCatagoryId: new FormControl(''),
      shareholderTypeId: new FormControl('')
    });
  }
  getDeducteeStatus(): void {
    this.surchargeService.getDeducteeStatusList().subscribe(result => {
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
    this.surchargeService.getResidentialStatusList().subscribe(result => {
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
    this.surchargeService.getNOPList().subscribe(result => {
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

  getShareholderCategory(): void {
    this.surchargeService.getCategoryList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getCategoryList = {
          label: result[i].name,
          value: result[i].id
        };
        this.getCategoryShareholderList = [
          ...this.getCategoryShareholderList,
          getCategoryList
        ];
      }
    });
  }

  getTypeOfShareholder(): void {
    this.surchargeService.getTypeOfShareholderList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getTypeList = {
          label: result[i].name,
          value: result[i].id
        };
        this.getTypeOfShareholderList = [
          ...this.getTypeOfShareholderList,
          getTypeList
        ];
      }
    });
  }

  getSurchargeDetails(form: any): FormControl {
    return form.controls.basisOfSurchargeDetails.controls;
  }

  addSurchargeDetails(): void {
    const control = this.surchargeForm.get(
      'basisOfSurchargeDetails'
    ) as FormArray;
    control.push(this.initSurchargeDetails());
    this.requiredFieldHandler(control.length - 1);
  }

  removeSurchargeDetails(i: number): void {
    const control = this.surchargeForm.get(
      'basisOfSurchargeDetails'
    ) as FormArray;
    control.removeAt(i);
  }
  onChanges(): void {
    this.surchargeForm.get('applicableTo').valueChanges.subscribe(val => {
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

  saveSurcharge(type: any): void {
    this.submitted = true;
    // stop here if form is invalid
    if (this.surchargeForm.invalid) {
      return;
    }
    if (this.surchargeForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.surchargeForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.surchargeForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.surchargeService
      .addSurchargeMaster(this.surchargeForm.getRawValue() as ISurcharge)
      .subscribe(
        (res: any) => {
          this.reset();
          window.scrollTo(0, 0);
          if (type == 'back') {
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
  updateSurcharge(): void {
    this.submittedTo = true;
    if (this.surchargeForm.invalid) {
      return;
    }
    this.surchargeService
      .updateSurchargeMaster(this.surchargeForm.getRawValue() as ISurcharge)
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
          this.logger.debug('error', error);
        }
      );
  }

  back(): void {
    this.route.navigate(['/dashboard/masters/surcharge']);
  }
  reset(): void {
    UtilModule.reset(this.surchargeForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  fetchDatabyId(params: any): void {
    this.surchargeService.getSurchargeMasterById(params.id).subscribe(
      (result: any) => {
        this.editDataBinder(result);
        if (result && result.applicableTo !== null) {
          this.surchargeForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }
        this.headingMsg = 'View';
        this.surchargeForm.patchValue(result);
        this.disablePreviousDate(result.applicableFrom);
        if (params.action.toUpperCase() === 'EDIT') {
          this.stateChanger('edit');
        } else {
          this.headingMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.surchargeForm,
            params.action,
            []
          );
        }
      },
      error => console.error(error)
    );
  }
  get formArr() {
    return this.surchargeForm.get('basisOfSurchargeDetails') as FormArray;
  }

  editDataBinder(data: any): void {
    if (data.surchargeApplicable) {
      this.isSurchargeNotApplicable = false;
      this.isSurchargeApplicable = true;
      this.formArr.removeAt(0);
    } else {
      this.isSurchargeApplicable = false;
      this.isSurchargeNotApplicable = true;
      this.showForm = true;
      if (data.basisOfSurchargeDetails.length > 0) {
        for (let i = 0; i < data.basisOfSurchargeDetails.length - 1; i++) {
          this.addSurchargeDetails();
        }
      }
    }
  }

  get f(): any {
    return this.surchargeForm.controls;
  }

  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.actionState = UtilModule.stateChanger(this.surchargeForm, 'New');
      } else if (
        params.action.toUpperCase() === 'EDIT' ||
        params.action.toUpperCase() === 'VIEW'
      ) {
        this.fetchDatabyId(params);
      }
    });
  }

  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.surchargeForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
}
