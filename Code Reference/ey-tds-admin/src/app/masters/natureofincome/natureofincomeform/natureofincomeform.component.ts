import { Component, OnInit } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { INatureOfPayment } from '@app/shared/model/natureOfPayment.model';
import { NatureofincomeService } from '../natureofincome.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-natureofincomeform',
  templateUrl: './natureofincomeform.component.html',
  styleUrls: ['./natureofincomeform.component.scss']
})
export class NatureofincomeformComponent implements OnInit {
  submittedTo: boolean;
  dateError: boolean;
  applicableToDateError: boolean;
  minDate: any = undefined;
  get f(): any {
    return this.paymentForm.controls;
  }
  paymentForm: FormGroup;
  submitted = false;
  natureOfPaymentOption = false;
  actionState: string;
  headingMsg: string;
  msgs: Array<any>;
  constructor(
    private readonly natureofincomeService: NatureofincomeService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}
  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.paymentForm = new FormGroup({
      id: new FormControl(''),
      section: new FormControl('', [
        Validators.required,
        Validators.maxLength(10),
        Validators.pattern(/^[a-zA-Z0-9()]+$/)
      ]),
      nature: new FormControl('', [
        Validators.required,
        Validators.maxLength(2048)
      ]),
      isSubNaturePaymentApplies: new FormControl(false, Validators.required),
      displayValue: new FormControl('', [
        Validators.required,
        Validators.maxLength(10),
        Validators.pattern(/^[a-zA-Z0-9)(]+$/)
      ]),
      applicableFrom: new FormControl('', [Validators.required]),
      applicableTo: new FormControl(undefined),
      subNaturePaymentMasters: new FormArray([this.initPayment()])
    });
    this.stateChecker();
    this.headingMsg = 'Add';
    this.msgs = [];
    this.changeHandler();
    this.onChanges();
  }

  getFormattedDate(date1: any) {
    const date = new Date(date1);
    let year = date.getFullYear();
    const month = (1 + date.getMonth()).toString().padStart(2, '0');
    const day = date
      .getDate()
      .toString()
      .padStart(2, '0');

    return year + '-' + month + '-' + day;
  }
  onChanges(): void {
    this.paymentForm.get('applicableTo').valueChanges.subscribe(val => {
      this.applicableToDateError = false;
    });
  }
  changeHandler(): void {
    if (!this.paymentForm.value.isSubNaturePaymentApplies) {
      this.paymentForm.controls.subNaturePaymentMasters.disable();
    } else {
      this.paymentForm.controls.subNaturePaymentMasters.enable();
      this.paymentForm
        .get('subNaturePaymentMasters')
        .setValidators(Validators.required);
      this.paymentForm.get('subNaturePaymentMasters').updateValueAndValidity();
    }
  }
  saveNatureOfCollection(type: any): void {
    this.submitted = true;
    // stop here if form is invalid
    if (this.paymentForm.invalid) {
      return;
    }
    if (this.paymentForm.controls.applicableTo.value != null) {
      const applicableFromDate = this.getFormattedDate(
        this.paymentForm.controls.applicableFrom.value
      );
      const applicableToDate = this.getFormattedDate(
        this.paymentForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;

        return;
      }
    }

    this.natureofincomeService
      .addNatureOfCollection(this.paymentForm.getRawValue() as INatureOfPayment)
      .subscribe(
        (res: INatureOfPayment) => {
          this.logger.debug('in');
          this.reset();
          window.scrollTo(0, 0);
          if (type === 'back') {
            setTimeout(() => {
              this.backFormClick();
            }, 500);
          }
        },
        (error: any) => {
          console.log(error);
        }
      );
  }
  updateNatureOfPayment(): void {
    this.submittedTo = true;
    if (this.paymentForm.invalid) {
      return;
    }
    this.natureofincomeService
      .updateNatureOfCollectionById(
        this.paymentForm.getRawValue() as INatureOfPayment
      )
      .subscribe(
        (result: INatureOfPayment) => {
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
  fetchDatabyId(params: any): void {
    this.natureofincomeService.getNatureOfCollectionById(params.id).subscribe(
      (result: any) => {
        this.editDataBinder(result);
        this.paymentForm.patchValue(result);
        this.disablePreviousDate(result.applicableFrom);
        if (result && result.applicableTo !== null) {
          this.paymentForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }
        if (params.action.toUpperCase() === 'EDIT') {
          this.stateChanger('edit');
        } else {
          this.headingMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.paymentForm,
            params.action,
            []
          );
        }
      },
      error => console.error(error)
    );
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
  get formArr() {
    return this.paymentForm.get('subNaturePaymentMasters') as FormArray;
  }
  editDataBinder(data: any): void {
    if (!data.isSubNaturePaymentApplies) {
      this.formArr.removeAt(0);
    } else {
      if (data.subNaturePaymentMasters.length > 0) {
        for (let i = 0; i < data.subNaturePaymentMasters.length - 1; i++) {
          this.addNatureOfCollection();
        }
      }
    }
  }
  reset(): void {
    this.paymentForm.reset();
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }
  backFormClick(): void {
    this.router
      .navigate(['/dashboard/masters/natureofincome'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
  initPayment(): FormGroup {
    return new FormGroup({
      id: new FormControl(''),
      nature: new FormControl('', Validators.required)
    });
  }
  addNatureOfCollection(): void {
    const control = this.paymentForm.controls
      .subNaturePaymentMasters as FormArray;
    control.push(this.initPayment());
  }
  removeNatureOfPayment(i: number): void {
    const control = this.paymentForm.controls
      .subNaturePaymentMasters as FormArray;
    control.removeAt(i);
  }
  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.paymentForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.headingMsg = 'Create';
        this.actionState = UtilModule.stateChanger(this.paymentForm, 'New');
      } else if (
        params.action.toUpperCase() === 'EDIT' ||
        params.action.toUpperCase() === 'VIEW'
      ) {
        if (params.id && params.id !== 0) {
          this.fetchDatabyId(params);
        }
      }
    });
  }
}
