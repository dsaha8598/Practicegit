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
import { SurchargeTcsService } from '../surcharge-tcs.service';
import { UtilModule } from '@app/shared';
import { ISurcharge } from '@app/shared/model/surchage.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-surchargeTcsform',
  templateUrl: './surcharge-tcsform.component.html',
  styleUrls: ['./surcharge-tcsform.component.scss']
})
export class SurchargeTcsformComponent implements OnInit {
  surchargeForm: FormGroup;
  isSurchargeDetails: boolean;
  getSurchargeTypeList: Array<any>;
  isSurchargeApplicable: boolean;
  isSurchargeNotApplicable: boolean;
  showForm: boolean;
  pageParams: any;
  minDate: any = undefined;
  getCollecteeStatusList: Array<any>;
  getCollecteeResidentialStatusList: Array<any>;
  getNatureofIncomeList: Array<any>;
  isSaved: boolean;
  submitted: boolean;
  submittedTo: boolean = false;
  actionState: string;
  headingMsg: string;
  model: NgbDateStruct;
  applicableToDateError: boolean;
  constructor(
    private readonly surchargeTcsService: SurchargeTcsService,
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
    this.getCollecteeStatusList = [];
    this.getCollecteeStatusList.push({
      label: 'Select collectee status',
      value: ''
    });
    /* this.getNatureofIncomeList = [];
    this.getNatureofIncomeList.push({
      label: 'Select nature of income',
      value: ''
    }); */
    //this.getNatureOfIncome();
    this.getDeducteeStatus();
    this.stateChecker();
    this.onChanges();
    this.showForm = true;
    this.requiredFieldHandler(0);
    // this.changeHandler();
  }

  /*   changeHandler(): void {
    const surchargeApplicable = this.surchargeForm.get('surchargeApplicable')
      .value;
    this.isSurchargeNotApplicable = this.isSurchargeApplicable = false;
    if (surchargeApplicable) {
      this.isSurchargeApplicable = true;
      this.surchargeForm
        .get('surchargeRate')
        .setValidators([Validators.required, Validators.maxLength(2)]);
      this.surchargeForm.get('surchargeRate').updateValueAndValidity();
     } else {
      this.isSurchargeNotApplicable = true;
      this.showForm=true;
      this.requiredFieldHandler(0);
    }
  }
 */
  createSurchargeForm(): void {
    this.surchargeForm = new FormGroup({
      id: new FormControl(undefined),
      applicableFrom: new FormControl(undefined, [Validators.required]),
      applicableTo: new FormControl(undefined),
      basisOfSurchargeDetails: new FormArray([this.initSurchargeDetails()])
    });
  }

  requiredFieldHandler(id: number): void {
    const control = this.surchargeForm.get(
      'basisOfSurchargeDetails'
    ) as FormArray;
    control
      .at(id)
      .get('collecteeStatusId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('collecteeStatusId')
      .updateValueAndValidity();
    /*  control
      .at(id)
      .get('natureOfIncomeMasterId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('natureOfIncomeMasterId')
      .updateValueAndValidity();
    */ control
      .at(id)
      .get('collecteeResidentialStatusId')
      .setValidators([Validators.required]);
    control
      .at(id)
      .get('collecteeResidentialStatusId')
      .updateValueAndValidity();
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
      collecteeStatusId: new FormControl(''),
      collecteeResidentialStatusId: new FormControl(2),
      collecteeResidentialStatus: new FormControl({
        value: 'Non-Resident',
        disabled: true
      }),
      invoiceSlabFrom: new FormControl(''),
      invoiceSlabTo: new FormControl(''),
      natureOfIncomeMasterId: new FormControl(''),
      nature: new FormControl(undefined),
      status: new FormControl(undefined),
      residentStatus: new FormControl(undefined),
      rate: new FormControl('')
    });
  }
  getDeducteeStatus(): void {
    this.surchargeTcsService.getDeducteeStatusList().subscribe(result => {
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

  /* getResidentialStatus(): void {
    this.surchargeTcsService.getResidentialStatusList().subscribe(result => {
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
 */
  /*   getNatureOfIncome(): void {
    this.surchargeTcsService.getNOPList().subscribe(result => {
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
  }
 */
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
      console.log(
        'inside invalid',
        this.surchargeForm.invalid,
        this.surchargeForm.controls
      );
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
    this.surchargeTcsService
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
    this.surchargeTcsService
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
    this.route.navigate(['/dashboard/masters/surchargeTcs']);
  }
  reset(): void {
    UtilModule.reset(this.surchargeForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  fetchDatabyId(params: any): void {
    this.surchargeTcsService.getSurchargeMasterById(params.id).subscribe(
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
