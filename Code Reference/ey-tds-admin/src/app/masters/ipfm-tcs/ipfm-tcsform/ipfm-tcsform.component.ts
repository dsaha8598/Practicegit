import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { RadioButtonModule } from 'primeng/radiobutton';
import { IpfmTcsService } from '../ipfm-tcs.service';
import { Iipfm } from '@app/shared/model/ipfm.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-ipfmTcsform',
  templateUrl: './ipfm-tcsform.component.html',
  styleUrls: ['./ipfm-tcsform.component.scss']
})
export class IpfmTcsformComponent implements OnInit {
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  get f(): any {
    return this.intrestPenaltyFineForm.controls;
  }
  intrestPenaltyFineForm: FormGroup;
  submitted: boolean;
  lateFilling: boolean;
  finePerDay: boolean;
  actionState: string;
  headerMsg: string;
  minDate: any = undefined;
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
    private readonly actRoute: ActivatedRoute,
    private readonly ipfmTcsService: IpfmTcsService,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.createIpfmForm();
    this.stateChecker();
    this.headerMsg = 'Add';
    this.onChanges();
    this.handleChange('Short Deduction');
  }
  handleChange(value: string): void {
    this.intrestPenaltyFineForm.controls.rate.reset();
    this.intrestPenaltyFineForm.controls.finePerDay.reset();
    if (value === 'Late Filing') {
      this.lateFilling = true;
      this.finePerDay = false;
      // this.intrestPenaltyFineForm.controls.rate.patchValue(null);
      this.intrestPenaltyFineForm.controls.rate.disable();
    } else {
      this.finePerDay = true;
      this.lateFilling = false;
      this.intrestPenaltyFineForm.controls.rate.enable();
      this.intrestPenaltyFineForm.controls.rate.setValidators([
        Validators.required
      ]);
      this.intrestPenaltyFineForm.controls.rate.updateValueAndValidity();
    }
  }

  inputValidator(event: any): void {
    const pattern = /^[0-9.]*$/;
    if (!pattern.test(event.target.value)) {
      event.target.value = event.target.value.replace(/[^0-9.]/, '');
    }
  }

  editHandler(value: string): void {
    if (value === 'Late Filing') {
      this.lateFilling = true;
      this.finePerDay = false;
    } else {
      this.finePerDay = true;
      this.lateFilling = false;
    }
  }
  saveintrestPenaltyFine(type: any): void {
    this.submitted = true;
    // stop here if form is invalid
    if (this.intrestPenaltyFineForm.invalid) {
      return;
    }
    if (this.intrestPenaltyFineForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.intrestPenaltyFineForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.intrestPenaltyFineForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.ipfmTcsService
      .addIpfm(this.intrestPenaltyFineForm.value as Iipfm)
      .subscribe(
        (res: Iipfm) => {
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
  updateIntrestPenaltyFine(): void {
    this.submittedTo = true;
    if (this.intrestPenaltyFineForm.invalid) {
      return;
    }
    this.ipfmTcsService
      .updateIpfm(this.intrestPenaltyFineForm.getRawValue() as Iipfm)
      .subscribe(
        (res: Iipfm) => {
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
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/Interest&penaltyTCS']);
  }
  reset(): void {
    UtilModule.reset(this.intrestPenaltyFineForm);
    this.submitted = false;
    this.submittedTo = false;
  }
  fetchDatabyId(id: number): void {
    this.ipfmTcsService.getIpfmById(id).subscribe(result => {
      if (result && result.applicableTo !== null) {
        result.applicableTo = new Date(result.applicableTo);
        this.intrestPenaltyFineForm
          .get('applicableTo')
          .setValidators(Validators.required);
      }
      if (result.interestType == 'Late Filing') {
        this.intrestPenaltyFineForm.controls.rate.disable();
      } else {
        this.intrestPenaltyFineForm.controls.rate.enable();
      }
      this.headerMsg = 'View';
      this.disablePreviousDate(result.applicableFrom);
      this.editHandler(result.interestType);
      this.intrestPenaltyFineForm.setValue(result);
    });
  }
  onChanges(): void {
    this.intrestPenaltyFineForm
      .get('applicableTo')
      .valueChanges.subscribe(val => {
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

  stateChanger(value: string): void {
    console.log('inside statechanger');
    console.log(value);
    this.actionState = UtilModule.stateChanger(
      this.intrestPenaltyFineForm,
      value,
      ['applicableTo']
    );
    this.headerMsg = 'Update';
  }
  private createIpfmForm(): void {
    this.intrestPenaltyFineForm = this.formBuilder.group({
      active: [''],
      interestType: ['Short Deduction', Validators.required],
      typeOfIntrestCalculation: ['Calendar Month', Validators.required],
      applicableFrom: ['', Validators.required],
      applicableTo: [],
      rate: [0],
      finePerDay: [0],
      id: ['']
    });
  }
  private stateChecker(): void {
    this.actRoute.queryParams.subscribe(
      (params: any) => {
        if (!params.action || params.action === '') {
          this.actionState = UtilModule.stateChanger(
            this.intrestPenaltyFineForm,
            'New'
          );
        } else if (params.action.toUpperCase() === 'EDIT') {
          this.actionState = UtilModule.stateChanger(
            this.intrestPenaltyFineForm,
            params.action,
            ['applicableTo']
          );
          this.headerMsg = 'Update';
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
          }
        } else {
          this.headerMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.intrestPenaltyFineForm,
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
