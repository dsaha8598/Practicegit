import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { TdsrateService } from '../tdsrate.service';
import { ITDSRate } from '@app/shared/model/tds.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-tdsrateform',
  templateUrl: './tdsrateform.component.html',
  styleUrls: ['./tdsrateform.component.scss']
})
export class TdsrateformComponent implements OnInit {
  TDSForm: FormGroup;
  submitted = false;
  actionState: string;
  getnatureofpaymentlist: any;
  getsubnatureofpaymentlist: any;
  isNatureOfPayment: string;
  minDate: any = undefined;
  getDeducteeResidentialStatusList: Array<any>;
  getDeducteeStatusList: Array<any>;
  residentialStatus: Array<any>;
  valueChanges: boolean = false;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  headingMsg: string;
  isDeducteeStatus: boolean;
  deducteeSingleStatus: any = [];
  dropdownSettings = {
    singleSelection: false,
    idField: 'id',
    textField: 'status',
    selectAllText: 'Select all',
    unSelectAllText: 'Unselect all',
    itemsShowLimit: 3,
    allowSearchFilter: true
  };

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly tdsrateService: TdsrateService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.tdsForm();
    this.getnatureofpaymentlist = [];
    this.getnatureofpaymentlist.push({
      label: 'Select nature of payment',
      value: ''
    });
    this.getsubnatureofpaymentlist = [];
    this.getsubnatureofpaymentlist.push({
      label: 'Select sub-nature of payment',
      value: ''
    });
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
    this.getNatureofpayment();
    this.getSubnatureofpayment();
    this.getDeducteeStatus();
    this.getResidentialStatus();
    this.headingMsg = 'Add';
    this.statechecker();
  }
  onChanges(): void {
    this.TDSForm.get('applicableTo').valueChanges.subscribe(val => {
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

  getNatureofpayment(): void {
    this.tdsrateService.getNatureofPaymentList().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let nature = {
          label: result[i].nature + ' ' + '-' + ' ' + result[i].section,
          value: result[i].id
        };
        this.getnatureofpaymentlist = [...this.getnatureofpaymentlist, nature];
      }
    });
  }

  get f(): any {
    return this.TDSForm.controls;
  }

  getSubnatureofpayment(): void {
    this.tdsrateService.getSubNatureofPaymentList().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let subNature = {
          label: result[i].nature,
          value: result[i].id
        };
        this.getsubnatureofpaymentlist = [
          ...this.getsubnatureofpaymentlist,
          subNature
        ];
      }
    });
  }
  getDeducteeStatus(): void {
    this.tdsrateService.getDeducteeStatusList().subscribe(result => {
      this.getDeducteeStatusList = result;
      console.log(JSON.stringify(result));
    });
  }
  getResidentialStatus(): void {
    this.tdsrateService.getResidentialStatusList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let reDeductee = {
          label: result[i].status,
          value: result[i].id
        };
        this.getDeducteeResidentialStatusList = [
          ...this.getDeducteeResidentialStatusList,
          reDeductee
        ];
      }
    });
  }

  saveTdsRate(type: any): void {
    this.deducteeSingleStatus;
    this.submitted = true;
    console.log('values', this.TDSForm.value);
    if (this.TDSForm.invalid) {
      return;
    }
    if (this.TDSForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.TDSForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.TDSForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.tdsrateService.addTds(this.TDSForm.value as ITDSRate).subscribe(
      (res: any) => {
        this.reset();
        window.scrollTo(0, 0);
        if (type == 'back') {
          setTimeout(() => {
            this.backFormClick();
          }, 500);
        }
      },
      (error: any) => {
        window.scrollTo(0, 0);
      }
    );
  }

  updateTdsRate(): void {
    this.submittedTo = true;
    if (this.TDSForm.invalid) {
      return;
    }
    this.tdsrateService
      .updateTdsById(this.TDSForm.getRawValue() as ITDSRate)
      .subscribe(
        (res: ITDSRate) => {
          this.reset();
          window.scrollTo(0, 0);
          setTimeout(() => {
            this.backFormClick();
          }, 500);
        },
        (error: any) => {
          window.scrollTo(0, 0);
        }
      );
  }

  reset(): void {
    UtilModule.reset(this.TDSForm);
    // this.TDSForm.reset();
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  fetchDatabyId(id: any): void {
    this.tdsrateService.getTdsById(id).subscribe(
      (result: any) => {
        if (result && result.applicableTo !== null) {
          this.TDSForm.get('applicableTo').setValidators(Validators.required);
        }
        this.headingMsg = 'View';
        this.disablePreviousDate(result.applicableFrom);
        this.deducteeSingleStatus = this.getDeducteeStatusList.filter(each => {
          if (each.id === result.deducteeStatusId) {
            const obj = {
              id: each.id,
              status: each.status
            };
            return obj;
          }
        });
        delete this.deducteeSingleStatus[0].active;
        delete this.deducteeSingleStatus[0].panCode;
        this.deducteeSingleStatus[0].isDisabled = true;
        result.deducteeStatus = this.deducteeSingleStatus;
        this.isDeducteeStatus = true;
        // this.getResidentialStatus();
        this.TDSForm.patchValue(result);
      },
      (error: any) => {}
    );
  }
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/tdsrate']);
  }

  stateChanger(value: any): any {
    this.actionState = UtilModule.stateChanger(this.TDSForm, value, [
      'applicableTo'
    ]);
    this.headingMsg = 'Update';
  }

  handleNatureChange(): void {
    this.TDSForm.value.natureOfPaymentId = 0;
    if (!this.TDSForm.value.rate) {
      this.TDSForm.controls.rate.setValidators([
        Validators.required,
        Validators.maxLength(2)
      ]);
    }
    this.TDSForm.value.subNatureOfPaymentId = 0;
    this.TDSForm.value.saccode = '';
    if (!this.TDSForm.value.isSubNaturePaymentMaster) {
      this.TDSForm.controls.saccode.setValidators([
        Validators.required,
        Validators.maxLength(4),
        Validators.minLength(4),
        Validators.pattern('^[0-9]*$')
      ]);
      this.TDSForm.controls.subNatureOfPaymentId.disable();
      this.TDSForm.controls.natureOfPaymentId.enable();
      this.TDSForm.controls.saccode.enable();
      this.TDSForm.get('natureOfPaymentId').setValidators(Validators.required);
      this.TDSForm.get('natureOfPaymentId').updateValueAndValidity();
      this.TDSForm.get('saccode').setValidators(Validators.required);
      this.TDSForm.get('saccode').updateValueAndValidity();
    } else {
      this.TDSForm.controls.natureOfPaymentId.disable();
      this.TDSForm.controls.saccode.disable();
      this.TDSForm.controls.subNatureOfPaymentId.enable();
      this.TDSForm.get('subNatureOfPaymentId').setValidators(
        Validators.required
      );
      this.TDSForm.get('subNatureOfPaymentId').updateValueAndValidity();
    }
  }
  changeHandler(): void {
    if (!this.TDSForm.value.isAnnualTransactionLimitApplicable) {
      this.TDSForm.controls.annualTransactionLimit.disable();
    } else {
      this.TDSForm.controls.annualTransactionLimit.enable();
      this.TDSForm.get('annualTransactionLimit').setValidators(
        Validators.required
      );
      this.TDSForm.get('annualTransactionLimit').updateValueAndValidity();
    }
    if (!this.TDSForm.value.isPerTransactionLimitApplicable) {
      this.TDSForm.controls.perTransactionLimit.disable();
    } else {
      this.TDSForm.controls.perTransactionLimit.enable();
      this.TDSForm.get('perTransactionLimit').setValidators(
        Validators.required
      );
      this.TDSForm.get('perTransactionLimit').updateValueAndValidity();
    }
  }

  private tdsForm(): void {
    this.TDSForm = this.formBuilder.group({
      isSubNaturePaymentMaster: [false, Validators.required],
      id: [undefined],
      subNatureOfPaymentId: [''],
      subNaturePaymentMaster: [''],
      rate: [null, Validators.required],
      rateForNoPan: [null, Validators.required],
      noItrRate: [null, Validators.required],
      noPanRateAndNoItrRate: [null, Validators.required],
      natureOfPaymentId: ['', Validators.required],
      natureOfPaymentMaster: [''],
      saccode: [
        null,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(4),
          Validators.pattern('^[0-9]*$')
        ]
      ],
      isPerTransactionLimitApplicable: [false, Validators.required],
      // isAnnualTransactionLimitApplicable: [false, Validators.required],
      // annualTransactionLimit: [0, Validators.required],
      perTransactionLimit: [0, Validators.required],
      deducteeStatusId: [''],
      deducteeStatus: [[], Validators.required],
      statusName: [undefined],
      deducteeResidentialStatusId: [undefined],
      residentialStatusName: [undefined],
      applicableFrom: ['', Validators.required],
      applicableTo: [undefined]
    });
    this.onChanges();
  }
  private statechecker(): void {
    this.actRoute.queryParams.subscribe(
      (params: any) => {
        if (!params.action || params.action === '') {
          this.actionState = UtilModule.stateChanger(this.TDSForm, 'New');
        } else if (params.action.toUpperCase() === 'EDIT') {
          this.actionState = UtilModule.stateChanger(
            this.TDSForm,
            params.action,
            ['applicableTo']
          );
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
          }
        } else {
          this.actionState = UtilModule.stateChanger(
            this.TDSForm,
            params.action
          );
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
          }
        }
      },
      (error: any) => {
        this.logger.error(error);
      }
    );
  }
}
