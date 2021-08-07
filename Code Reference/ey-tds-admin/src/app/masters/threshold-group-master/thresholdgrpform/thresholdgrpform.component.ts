import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { ThresholdGroupMasterService } from '../threshold-group-master.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { ITHRESHOLD } from '@app/shared/model/thresholdgrp.model';
@Component({
  selector: 'ey-thresholdgrpform',
  templateUrl: './thresholdgrpform.component.html',
  styleUrls: ['./thresholdgrpform.component.scss']
})
export class ThresholdgrpformComponent implements OnInit {
  thresholdGrpForm: FormGroup;
  submitted = false;
  actionState: string;
  getnatureofpaymentlist: any;
  isNatureOfPayment: string;
  minDate: any = undefined;
  residentialStatus: Array<any>;
  valueChanges: boolean = false;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  headingMsg: string;
  isDeducteeStatus: boolean;
  natureOfpaymentList: any = [];
  dropdownSettings = {
    singleSelection: false,
    idField: 'natureId',
    textField: 'nature',
    selectAllText: 'Select all',
    unSelectAllText: 'Unselect all',
    itemsShowLimit: 3,
    allowSearchFilter: true
  };

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly thresholdgrpmasterService: ThresholdGroupMasterService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.createthresholdGrpForm();
    this.getnatureofpaymentlist = [];
    this.getnatureofpaymentlist.push({
      label: 'Select nature of payment',
      value: ''
    });
    this.getNatureofpayment();
    this.headingMsg = 'Add';
    this.statechecker();
  }
  onChanges(): void {
    this.thresholdGrpForm
      .get('applicableTo')
      .valueChanges.subscribe((val: any) => {
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
    this.thresholdgrpmasterService
      .getNatureofPaymentList()
      .subscribe((result: any) => {
        for (let i = 0; i < result.length; i++) {
          let nature = {
            nature: result[i].nature, //+ ' ' + '-' + ' ' + result[i].section,
            natureId: result[i].id
          };
          this.getnatureofpaymentlist = [
            ...this.getnatureofpaymentlist,
            nature
          ];
        }
        this.getnatureofpaymentlist.shift();
        /*  console.log(
          'printing nature of paymentlist',
          this.getnatureofpaymentlist
        );
      */
      });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/masters/thresholdgrpMaster']);
  }

  get f(): any {
    return this.thresholdGrpForm.controls;
  }

  addThreshold(type: any): void {
    this.natureOfpaymentList;
    this.submitted = true;
    //console.log('values', this.thresholdGrpForm.value);
    if (this.thresholdGrpForm.invalid) {
      return;
    }
    if (this.thresholdGrpForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.thresholdGrpForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.thresholdGrpForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    // console.log(this.thresholdGrpForm.getRawValue());
    this.thresholdgrpmasterService
      .addThreshold(this.thresholdGrpForm.value as ITHRESHOLD)
      .subscribe(
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

  updateThreashold(): void {
    this.submittedTo = true;
    if (this.thresholdGrpForm.invalid) {
      return;
    }
    this.thresholdgrpmasterService
      .updateThresholdById(this.thresholdGrpForm.getRawValue() as ITHRESHOLD)
      .subscribe(
        (res: ITHRESHOLD) => {
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
    UtilModule.reset(this.thresholdGrpForm);
    // this.thresholdGrpForm.reset();
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  fetchDatabyId(id: any): void {
    this.thresholdgrpmasterService.getthresholdById(id).subscribe(
      (result: any) => {
        if (result && result.applicableTo !== null) {
          this.thresholdGrpForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }
        this.disablePreviousDate(result.applicableFrom);
        this.natureOfpaymentList = this.getnatureofpaymentlist.filter(
          (each: any) => {
            /*   debugger;
            console.log(result);
           */ if (
              each.value === result.nature
            ) {
              const obj = {
                natureId: each.id,
                nature: each.nature
              };
              return obj;
            }
          }
        );
        this.natureOfpaymentList.isDisabled = true; //   result.nature = this.natureOfpaymentList;
        /*     console.log(this.natureOfpaymentList);
        debugger;
     */ this.isDeducteeStatus = true;
        // this.getResidentialStatus();
        this.thresholdGrpForm.patchValue(result);
      },
      (error: any) => {}
    );
  }
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/thresholdgrpMaster']);
  }

  stateChanger(value: any): any {
    this.actionState = UtilModule.stateChanger(this.thresholdGrpForm, value, [
      'applicableTo'
    ]);
    this.headingMsg = 'Update';
  }

  handleNatureChange(): void {
    this.thresholdGrpForm.value.natureOfPaymentId = 0;
    if (!this.thresholdGrpForm.value.rate) {
      this.thresholdGrpForm.controls.rate.setValidators([
        Validators.required,
        Validators.maxLength(2)
      ]);
    }
    this.thresholdGrpForm.value.subNatureOfPaymentId = 0;
    this.thresholdGrpForm.value.saccode = '';
    if (!this.thresholdGrpForm.value.isSubNaturePaymentMaster) {
      this.thresholdGrpForm.controls.saccode.setValidators([
        Validators.required,
        Validators.maxLength(4),
        Validators.minLength(4),
        Validators.pattern('^[0-9]*$')
      ]);
      this.thresholdGrpForm.controls.subNatureOfPaymentId.disable();
      this.thresholdGrpForm.controls.natureOfPaymentId.enable();
      this.thresholdGrpForm.controls.saccode.enable();
      this.thresholdGrpForm
        .get('natureOfPaymentId')
        .setValidators(Validators.required);
      this.thresholdGrpForm.get('natureOfPaymentId').updateValueAndValidity();
      this.thresholdGrpForm.get('saccode').setValidators(Validators.required);
      this.thresholdGrpForm.get('saccode').updateValueAndValidity();
    } else {
      this.thresholdGrpForm.controls.natureOfPaymentId.disable();
      this.thresholdGrpForm.controls.saccode.disable();
      this.thresholdGrpForm.controls.subNatureOfPaymentId.enable();
      this.thresholdGrpForm
        .get('subNatureOfPaymentId')
        .setValidators(Validators.required);
      this.thresholdGrpForm
        .get('subNatureOfPaymentId')
        .updateValueAndValidity();
    }
  }
  changeHandler(): void {
    if (!this.thresholdGrpForm.value.isAnnualTransactionLimitApplicable) {
      this.thresholdGrpForm.controls.annualTransactionLimit.disable();
    } else {
      this.thresholdGrpForm.controls.annualTransactionLimit.enable();
      this.thresholdGrpForm
        .get('annualTransactionLimit')
        .setValidators(Validators.required);
      this.thresholdGrpForm
        .get('annualTransactionLimit')
        .updateValueAndValidity();
    }
    if (!this.thresholdGrpForm.value.isPerTransactionLimitApplicable) {
      this.thresholdGrpForm.controls.perTransactionLimit.disable();
    } else {
      this.thresholdGrpForm.controls.perTransactionLimit.enable();
      this.thresholdGrpForm
        .get('perTransactionLimit')
        .setValidators(Validators.required);
      this.thresholdGrpForm.get('perTransactionLimit').updateValueAndValidity();
    }
  }

  private createthresholdGrpForm(): void {
    this.thresholdGrpForm = this.formBuilder.group({
      id: [''],
      taxApplicability: [true],
      groupName: ['', Validators.required],
      thresholdAmount: [, Validators.required],
      //  natureId: [''],
      nature: [[], Validators.required],
      applicableFrom: ['', Validators.required],
      applicableTo: [undefined]
    });
    this.onChanges();
  }
  private statechecker(): void {
    this.actRoute.queryParams.subscribe(
      (params: any) => {
        if (!params.action || params.action === '') {
          this.actionState = UtilModule.stateChanger(
            this.thresholdGrpForm,
            'New'
          );
        } else if (params.action.toUpperCase() === 'EDIT') {
          this.headingMsg = 'Update';
          this.actionState = UtilModule.stateChanger(
            this.thresholdGrpForm,
            params.action,
            ['applicableTo']
          );
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
          }
        } else {
          this.headingMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.thresholdGrpForm,
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
