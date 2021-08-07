import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { RatemasterService } from '../ratemaster.service';
import { ITDSRate } from '@app/shared/model/tds.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-ratemasterform',
  templateUrl: './ratemasterform.component.html',
  styleUrls: ['./ratemasterform.component.scss']
})
export class RatemasterformComponent implements OnInit {
  TDSForm: FormGroup;
  submitted = false;
  actionState: string;
  getnatureofincomelist: any;
  //getsubnatureofincomelist: any;
  isNatureOfIncome: string;
  minDate: any = undefined;
  /* getCollecteeResidentialStatusList: Array<any>;
  getCollecteeStatusList: Array<any>;
   */ residentialStatus: Array<
    any
  >;
  valueChanges: boolean = false;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  headingMsg: string;
  isCollecteeStatus: boolean;
  collecteeSingleStatus: any = [];
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
    private readonly ratemasterService: RatemasterService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.tdsForm();
    this.getnatureofincomelist = [];
    this.getnatureofincomelist.push({
      label: 'Select nature of income',
      value: ''
    });
    /*  this.getsubnatureofincomelist = [];
    this.getsubnatureofincomelist.push({
      label: 'Select sub-nature of income',
      value: ''
    });
     this.getCollecteeResidentialStatusList = [];
    this.getCollecteeResidentialStatusList.push({
      label: 'Select collectee residential status',
      value: ''
    });
     this.getCollecteeStatusList = [];
    this.getCollecteeStatusList.push({
      label: 'Select collectee status',
      value: ''
    });
    */ this.getNatureofpayment();
    //  this.getSubnatureofpayment();
    /*  this.getCollecteeStatus();
    this.getResidentialStatus();
    */ this.headingMsg =
      'Add';
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
    this.ratemasterService.getNatureofPaymentList().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let nature = {
          label: result[i].nature + ' ' + '-' + ' ' + result[i].section,
          value: result[i].id
        };
        this.getnatureofincomelist = [...this.getnatureofincomelist, nature];
      }
    });
  }

  get f(): any {
    return this.TDSForm.controls;
  }

  /* getSubnatureofpayment(): void {
    this.ratemasterService
      .getSubNatureofPaymentList()
      .subscribe((result: any) => {
        for (let i = 0; i < result.length; i++) {
          let subNature = {
            label: result[i].nature,
            value: result[i].id
          };
          this.getsubnatureofincomelist = [
            ...this.getsubnatureofincomelist,
            subNature
          ];
        }
      });
  }
   getCollecteeStatus(): void {
    this.ratemasterService.getCollecteeStatusList().subscribe((result: any) => {
      this.getCollecteeStatusList = result;
      console.log(JSON.stringify(result));
    });
  }
  getResidentialStatus(): void {
    this.ratemasterService.getResidentialStatusList().subscribe(result => {
      for (let i = 0; i < result.length; i++) {
        let reDeductee = {
          label: result[i].status,
          value: result[i].id
        };
        this.getCollecteeResidentialStatusList = [
          ...this.getCollecteeResidentialStatusList,
          reDeductee
        ];
      }
    });
  }
 */
  saveTdsRate(type: any): void {
    this.collecteeSingleStatus;
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
    this.ratemasterService.addTds(this.TDSForm.value as ITDSRate).subscribe(
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
    this.ratemasterService
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
    this.ratemasterService.getTdsById(id).subscribe(
      (result: any) => {
        if (result && result.applicableTo !== null) {
          this.TDSForm.get('applicableTo').setValidators(Validators.required);
        }
        this.headingMsg = 'View';
        this.disablePreviousDate(result.applicableFrom);
        /*   this.collecteeSingleStatus = this.getCollecteeStatusList.filter(
          each => {
            if (each.id === result.deducteeStatusId) {
              const obj = {
                id: each.id,
                status: each.status
              };
              return obj;
            }
          }
        );
         delete this.collecteeSingleStatus[0].active;
        delete this.collecteeSingleStatus[0].panCode;
        this.collecteeSingleStatus[0].isDisabled = true;
        result.deducteeStatus = this.collecteeSingleStatus;
        this.isCollecteeStatus = true;*/
        this.TDSForm.patchValue(result);
      },
      (error: any) => {}
    );
  }
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/ratemaster']);
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
    //  this.TDSForm.value.saccode = '';
    /* if (!this.TDSForm.value.isSubNaturePaymentMaster) {
      this.TDSForm.controls.saccode.setValidators([
        Validators.required,
        Validators.maxLength(4),
        Validators.minLength(4),
        Validators.pattern('^[0-9]*$')
      ]);
      this.TDSForm.controls.subNatureOfPaymentId.disable();
      this.TDSForm.controls.natureOfPaymentId.enable();
    this.TDSForm.controls.saccode.enable();
      this.TDSForm.get('natureOfIncomeId').setValidators(Validators.required);
      this.TDSForm.get('natureOfIncomeId').updateValueAndValidity();
       this.TDSForm.get('saccode').setValidators(Validators.required);
      this.TDSForm.get('saccode').updateValueAndValidity();
     } else {
      this.TDSForm.controls.natureOfIncomeId.disable();
      this.TDSForm.controls.saccode.disable();
      this.TDSForm.controls.subNatureOfIncomeId.enable();
      this.TDSForm.get('subNatureOfIncomeId').setValidators(
        Validators.required
      );
      this.TDSForm.get('subNatureOfIncomeId').updateValueAndValidity();
    } */
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
      // isSubNaturePaymentMaster: [false, Validators.required],
      id: [undefined],
      /*   subNatureOfIncomeId: [''],
      subNatureIncomeMaster: [''],
     */ rate: [
        null,
        Validators.required
      ],
      rateForNoPan: [null, Validators.required],
      noItrRate: [null, Validators.required],
      noPanRateAndNoItrRate: [null, Validators.required],
      natureOfIncomeId: ['', Validators.required],
      natureOfIncomeMaster: [''],
      /*  saccode: [
        null,
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(4),
          Validators.pattern('^[0-9]*$')
        ]
      ],
      */ isPerTransactionLimitApplicable: [
        false,
        Validators.required
      ],
      isAnnualTransactionLimitApplicable: [false, Validators.required],
      annualTransactionLimit: [0, Validators.required],
      perTransactionLimit: [0, Validators.required],
      collecteeStatusId: [''],
      collecteeStatus: [undefined],
      statusName: [undefined],
      collecteeResidentialStatusId: [undefined],
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
