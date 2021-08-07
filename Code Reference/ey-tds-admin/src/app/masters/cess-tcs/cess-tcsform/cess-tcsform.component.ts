import { Component, OnInit } from '@angular/core';
import { FormGroup, Validators, FormControl, FormArray } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CessTcsService } from '../cess-tcs.service';
import {
  NgbDateNativeAdapter,
  NgbDateAdapter
} from '@ng-bootstrap/ng-bootstrap';
import { UtilModule } from '@app/shared';
import { ICess } from '@app/shared/model/cess.model';
import { SelectItem } from 'primeng/api';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-cessTcsform',
  templateUrl: './cess-tcsform.component.html',
  styleUrls: ['./cess-tcsform.component.scss']
})
export class CessTcsformComponent implements OnInit {
  cessForm: FormGroup;
  isCessDetails: boolean;
  getCessTypeList: Array<SelectItem>;
  isCessApplicable: boolean;
  isInvalidApplicableData: boolean;
  isCessNotApplicable: boolean;
  showForm: boolean;
  getCollecteeStatusList: Array<SelectItem>;
  getCollecteeResidentialStatusList: Array<SelectItem>;
  //getNatureofIncomeList: Array<SelectItem>;
  isSaved: boolean;
  submitted: boolean;
  actionState: string;
  headingMsg: string;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  minDate: any = undefined;
  sampleTest: any;
  constructor(
    private readonly cessService: CessTcsService,
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
    this.getCollecteeResidentialStatusList = [];
    this.getCollecteeResidentialStatusList.push({
      label: 'Select residential status',
      value: ''
    });
    this.getCollecteeStatusList = [];
    this.getCollecteeStatusList.push({
      label: 'Select collectee status',
      value: ''
    });
    /* this.getNatureofIncomeList = [];
    this.getNatureofIncomeList.push({
      label: 'Select nature of income',
      value: ''
    });
     */ this.headingMsg =
      'Add';
    this.getCessType();
    this.getDeducteeStatus();
    //this.getNatureOfPayment();
    this.stateChecker();
    this.onChanges();
    this.showForm = true;
    this.cessForm.controls['basisOfCessDetails'].enable();
    this.requiredFieldHandler(0);
  }
  disableYesFields() {
    this.cessForm.controls.basisOfCessDetails.disable();
  }
  requiredFieldHandler(id: number): void {
    const control = this.cessForm.get('basisOfCessDetails') as FormArray;
    control
      .at(id)
      .get('collecteeStatusId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('collecteeStatusId')
      .updateValueAndValidity();
    /*    control
      .at(id)
      .get('natureOfIncomeMasterId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('natureOfIncomeMasterId')
      .updateValueAndValidity();
     control
      .at(id)
      .get('collecteeResidentialStatusId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('collecteeResidentialStatusId')
      .updateValueAndValidity();
 */ control
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
      collecteeStatusId: new FormControl(''),
      collecteeResidentialStatusId: new FormControl(2),
      collecteeResidentialStatus: new FormControl('Non-Resident'),
      invoiceSlabFrom: new FormControl(''),
      invoiceSlabTo: new FormControl(''),
      natureOfIncomeMasterId: new FormControl(''),
      nature: new FormControl(undefined),
      rate: new FormControl(''),
      cessTypeId: new FormControl(''),
      cessTypeName: new FormControl(undefined)
    });
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
        this.getCollecteeStatusList = [
          ...this.getCollecteeStatusList,
          getDeducteeStatusList
        ];
      }
    });
  }

  /*   getResidentialStatus(): void {
    this.cessService.getResidentialStatusList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let getDeducteeResidentialStatusList = {
          label: result[i].status,
          value: result[i].id
        };
        this.getCollecteeResidentialStatusList = [
          ...this.getCollecteeResidentialStatusList,
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
        this.getNatureofIncomeList = [
          ...this.getNatureofIncomeList,
          getNatureofPaymentList
        ];
      }
    });
  }*/
  getCessDetails(form: any): FormControl {
    return form.controls.basisOfCessDetails.controls;
  }

  saveCess(flow: any): void {
    this.submitted = true;
    if (this.cessForm.invalid) {
      console.log(
        'inside invalid',
        this.cessForm.invalid,
        this.cessForm.controls
      );

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
    console.log('inside save cess', this.cessForm.getRawValue() as ICess);
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
    this.route.navigate(['/dashboard/masters/cessTcs']);
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
        //  this.cessForm.controls.basisOfCessDetails.patchValue({collecteeResidentialStatus:'Non-Resident'});
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
      applicableFrom: new FormControl(undefined, [Validators.required]),
      applicableTo: new FormControl(undefined),
      basisOfCessDetails: new FormArray([this.initCessDetails()])
    });
  }
  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.cessForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
}
