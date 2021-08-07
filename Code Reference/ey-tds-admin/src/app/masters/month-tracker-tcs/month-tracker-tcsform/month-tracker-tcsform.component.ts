import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { MonthTrackerTcsService } from '../month-tracker-tcs.service';
import { SelectItem } from 'primeng/api';

@Component({
  selector: 'ey-month-tracker-Tcsform',
  templateUrl: './month-tracker-tcsform.component.html',
  styleUrls: ['./month-tracker-tcsform.component.scss']
})
export class MonthTrackerTcsFormComponent implements OnInit {
  monthlyTrackerForm: FormGroup;
  submitted: boolean;
  actionState: string;
  minDate: any = undefined;
  monthlyTrackerFormData: any;
  headingMsg: string;
  defaultDate: Date;
  submittedTo: boolean;
  monthList: Array<SelectItem>;
  yearList: Array<SelectItem>;
  applicableToDateError: boolean;
  constructor(
    private readonly monthTrackerService: MonthTrackerTcsService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.defaultDate = new Date();
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
    this.monthList = [
      { label: 'January', value: 1 },
      { label: 'Febuary', value: 2 },
      { label: 'March', value: 3 },
      { label: 'April', value: 4 },
      { label: 'May', value: 5 },
      { label: 'June', value: 6 },
      { label: 'July', value: 7 },
      { label: 'August', value: 8 },
      { label: 'September', value: 9 },
      { label: 'October', value: 10 },
      { label: 'November', value: 11 },
      { label: 'December', value: 12 }
    ];
    this.yearList = [
      { label: '2011', value: 2011 },
      { label: '2012', value: 2012 },
      { label: '2013', value: 2013 },
      { label: '2014', value: 2014 },
      { label: '2015', value: 2015 },
      { label: '2016', value: 2016 },
      { label: '2017', value: 2017 },
      { label: '2018', value: 2018 },
      { label: '2019', value: 2019 },
      { label: '2020', value: 2020 },
      { label: '2021', value: 2021 },
      { label: '2022', value: 2022 },
      { label: '2023', value: 2023 },
      { label: '2024', value: 2024 },
      { label: '2025', value: 2025 },
      { label: '2026', value: 2026 },
      { label: '2027', value: 2027 },
      { label: '2028', value: 2028 },
      { label: '2029', value: 2029 },
      { label: '2030', value: 2030 }
    ];
  }

  initialLoading(): void {
    this.headingMsg = 'Add';
    this.createForm();
    this.stateChecker();
    this.onChanges();
  }

  fetchDatabyId(id: number): void {
    this.monthTrackerService.getMonthlyTrackerById(id).subscribe(
      (result: any) => {
        this.monthlyTrackerForm.setValue(result);
        this.disablePreviousDate(result.applicableFrom);
        if (result && result.applicableTo !== null) {
          this.monthlyTrackerForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }
      },
      error => {
        console.error(error);
      }
    );
  }
  onChanges(): void {
    this.monthlyTrackerForm.get('applicableTo').valueChanges.subscribe(val => {
      this.applicableToDateError = false;
    });
  }

  disablePreviousDate(date: any): void {
    const nextDay = new Date(date);
    nextDay.setDate(nextDay.getDate() + 1);
    this.minDate = {
      year: nextDay.getFullYear(),
      month: nextDay.getMonth() + 1,
      day: nextDay.getDate()
    };
  }

  getFormattedDate(date1: any): string {
    const date = new Date(date1);
    const year = UtilModule.getCurrentFinancialYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date
      .getDate()
      .toString()
      .padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  savemonthlyTracker(type: any): void {
    this.submitted = true;
    if (this.monthlyTrackerForm.invalid) {
      return;
    }
    if (this.monthlyTrackerForm.controls.applicableTo.value !== null) {
      const applicableFromDate = this.getFormattedDate(
        this.monthlyTrackerForm.controls.applicableFrom.value
      );
      const applicableToDate = this.getFormattedDate(
        this.monthlyTrackerForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;

        return;
      }
    }

    this.monthTrackerService
      .addMonthlyTracker(this.monthlyTrackerForm.value)
      .subscribe(
        result => {
          this.reset();
          window.scrollTo(0, 0);
          if (type === 'back') {
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
    UtilModule.reset(this.monthlyTrackerForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  checkApplicable(): boolean {
    if (!this.monthlyTrackerForm.controls.applicableTo.value) {
      return false;
    }

    return true;
  }

  updatemonthlyTracker(): void {
    this.submittedTo = true;
    if (this.monthlyTrackerForm.invalid) {
      return;
    }
    this.monthTrackerService
      .updateMonthlyTrackerById(this.monthlyTrackerForm.getRawValue())
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
    this.router
      .navigate(['/dashboard/masters/monthtrackerTcs'])
      .then()
      .catch();
  }
  get f(): any {
    return this.monthlyTrackerForm.controls;
  }

  createForm(): void {
    this.monthlyTrackerForm = new FormGroup({
      id: new FormControl(),
      month: new FormControl(new Date().getMonth() + 1, Validators.required),
      year: new FormControl(
        UtilModule.getCurrentFinancialYear(),
        Validators.required
      ),
      dueDateForChallanPayment: new FormControl(undefined, Validators.required),
      monthClosureForProcessing: new FormControl(
        undefined,
        Validators.required
      ),
      dueDateForFiling: new FormControl(undefined, Validators.required),
      applicableFrom: new FormControl(undefined, Validators.required),
      monthName: new FormControl(),
      applicableTo: new FormControl()
    });
  }

  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.headingMsg = 'Add';
        this.actionState = UtilModule.stateChanger(
          this.monthlyTrackerForm,
          'New'
        );
      } else if (params.action.toUpperCase() === 'EDIT') {
        this.headingMsg = 'Update';
        this.actionState = UtilModule.stateChanger(
          this.monthlyTrackerForm,
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
          this.monthlyTrackerForm,
          params.action
        );
      }
    });
  }

  yearTrigger(event: any): void {
    // this.selectedYearResident = event.selectedValue;
    // this.getResidentInvoiceData(
    //   this.selectedYearResident,
    //   this.selectedMonthResident
    // );
  }

  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.monthlyTrackerForm, type, ['applicableTo']);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }
}
