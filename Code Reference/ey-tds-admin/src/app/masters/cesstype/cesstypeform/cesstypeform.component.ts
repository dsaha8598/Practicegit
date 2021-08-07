import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { CesstypeService } from '../cesstype.service';
import { DatePipe } from '@angular/common';
import {
  NgbDateAdapter,
  NgbDateNativeAdapter,
  NgbDatepickerConfig
} from '@ng-bootstrap/ng-bootstrap';
import { ICessType } from '@app/shared/model/cesstype.model';
import { AlertService } from '@app/shared/components/alert/alert.service';

@Component({
  selector: 'ey-cesstypeform',
  templateUrl: './cesstypeform.component.html',
  styleUrls: ['./cesstypeform.component.scss']
})
export class CesstypeformComponent implements OnInit {
  cessTypeForm: FormGroup;
  submitted: boolean = false;
  actionState: string;
  minDate: any = undefined;
  cessTypeFormData: any;
  headingMsg: string;
  defaultDate: Date = new Date();
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly cesstypeService: CesstypeService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private readonly alertService: AlertService,
    private config: NgbDatepickerConfig
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.headingMsg = 'Add';
    this.createForm();
    this.stateChecker();
    this.onChanges();
  }

  fetchDatabyId(id: number): void {
    this.cesstypeService.getCessDatabyId(id).subscribe(
      (result: ICessType) => {
        this.cessTypeForm.setValue(result);
        this.disablePreviousDate(result.applicableFrom);
        if (result && result.applicableTo !== null) {
          this.cessTypeForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }
      },
      error => console.error(error)
    );
  }
  onChanges(): void {
    this.cessTypeForm.get('applicableTo').valueChanges.subscribe(val => {
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

  saveCessType(type: any): void {
    this.submitted = true;
    if (this.cessTypeForm.invalid) {
      return;
    }
    if (this.cessTypeForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.cessTypeForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.cessTypeForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.cesstypeService
      .saveCessTypeList(this.cessTypeForm.value as ICessType)
      .subscribe(
        result => {
          this.reset();
          window.scrollTo(0, 0);
          if (type == 'back') {
            setTimeout(() => {
              this.goBack();
            }, 500);
          }
        },
        error => {
          window.scrollTo(0, 0);
        }
      );
  }
  reset(): void {
    UtilModule.reset(this.cessTypeForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  checkApplicable(): boolean {
    if (!this.cessTypeForm.controls.applicableTo.value) {
      return false;
    }

    return true;
  }

  updateCessType(): void {
    this.submittedTo = true;
    if (this.cessTypeForm.invalid) {
      return;
    }
    this.cesstypeService
      .updateCessTypeList(this.cessTypeForm.getRawValue() as ICessType)
      .subscribe(
        result => {
          this.reset();
          window.scrollTo(0, 0);
          setTimeout(() => {
            this.goBack();
          }, 500);
        },
        error => {
          window.scrollTo(0, 0);
        }
      );
  }

  goBack(): void {
    this.router.navigate(['/dashboard/masters/cesstype']);
  }
  get f(): any {
    return this.cessTypeForm.controls;
  }

  createForm() {
    this.cessTypeForm = new FormGroup({
      active: new FormControl(),
      id: new FormControl(),
      cessType: new FormControl('', Validators.required),
      applicableFrom: new FormControl(undefined, Validators.required),
      applicableTo: new FormControl()
    });
  }

  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.headingMsg = 'Add';
        this.actionState = UtilModule.stateChanger(this.cessTypeForm, 'New');
      } else if (params.action.toUpperCase() === 'EDIT') {
        this.headingMsg = 'Update';
        this.actionState = UtilModule.stateChanger(
          this.cessTypeForm,
          params.action,
          ['applicableTo']
        );
        if (params.id && params.id !== 0) {
          this.fetchDatabyId(params.id);
        }
      } else {
        this.headingMsg = 'View';
        if (params.id && params.id !== 0) {
          this.fetchDatabyId(params.id);
        }
        this.actionState = UtilModule.stateChanger(
          this.cessTypeForm,
          params.action
        );
      }
    });
  }

  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.cessTypeForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
}
