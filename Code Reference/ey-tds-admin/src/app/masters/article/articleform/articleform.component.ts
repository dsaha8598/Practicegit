import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import {
  Validators,
  FormBuilder,
  FormGroup,
  FormArray,
  FormControl
} from '@angular/forms';
import { ArticleService } from '../article.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';

@Component({
  selector: 'ey-articleform',
  templateUrl: './articleform.component.html',
  styleUrls: ['./articleform.component.scss']
})
export class ArticleformComponent implements OnInit {
  articleMasterForm: FormGroup;
  submitted = false;
  actionState: string;
  controlskds: any;
  allcountries: any;
  applicableToDateError: boolean;
  minDate: any = undefined;
  headingMsg: string;
  data: any;
  natureOfRemittanceArray: any;
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly articleService: ArticleService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.createArticleForm();
    this.allcountries = [];
    this.natureOfRemittanceArray = [
      {
        label: 'Select nature of remittance',
        value: ''
      },
      {
        label: 'Royalty',
        value: 'ROYALTY'
      },
      {
        label: 'Interest',
        value: 'INTEREST'
      },
      {
        label: 'Fee for Technical Services',
        value: 'FTS'
      }
    ];
    this.allcountries.push({ label: 'Select country', value: '' });
    this.getCountries();
    this.headingMsg = 'Add';
    this.stateChecker();
    this.onChanges();
    this.disableFileds(2);
  }

  initArticleMaster(): FormGroup {
    return new FormGroup({
      id: new FormControl(undefined),
      condition: new FormControl(undefined, [
        Validators.required,
        Validators.maxLength(100)
      ]),
      isDetailedConditionApplicable: new FormControl(false),
      articleMasterDetailedConditions: new FormArray([this.initPayment()])
    });
  }
  get formArr() {
    return this.articleMasterForm.get('articleMasterConditions') as FormArray;
  }

  editDataBinder(data: any): void {
    if (
      data.articleMasterConditions[0].articleMasterDetailedConditions.length > 0
    ) {
      if (data.articleMasterConditions[0].isDetailedConditionApplicable) {
        for (
          let i = 0;
          i <
          data.articleMasterConditions[0].articleMasterDetailedConditions
            .length -
            1;
          i++
        ) {
          this.addArticlelist(0);
        }
      } else {
        // this.removeCondition(0);
      }
    }
  }

  fetchDatabyId(params: any): void {
    this.articleService.getArticlelistById(params.id).subscribe(
      (result: any) => {
        if (
          params.action.toUpperCase() === 'EDIT' ||
          params.action.toUpperCase() === 'VIEW'
        ) {
          this.data = result.data;
          this.stateChanger(params.action, this.data);
        } else {
          this.actionState = UtilModule.stateChanger(
            this.articleMasterForm,
            params.action,
            []
          );
        }
      },
      error => console.error(error)
    );
  }

  stateChanger(value: any, data: any): void {
    this.editDataBinder(data);
    data.applicableFrom = new Date(data.applicableFrom);
    if (data && data.applicableTo !== null) {
      data.applicableTo = new Date(data.applicableTo);
      this.articleMasterForm
        .get('applicableTo')
        .setValidators(Validators.required);
    }
    this.articleMasterForm.patchValue(data);
    if (data && data.natureOfRemittance) {
      this.articleMasterForm.controls['natureOfRemittance'].setValue(
        data.natureOfRemittance.toUpperCase()
      );
    }
    this.disablePreviousDate(data.applicableFrom);
    if (value.toUpperCase() === 'VIEW') {
      this.headingMsg = 'View';
      this.actionState = UtilModule.stateChanger(
        this.articleMasterForm,
        value.toUpperCase(),
        []
      );
    } else {
      this.headingMsg = 'Update';
      this.actionState = UtilModule.stateChanger(
        this.articleMasterForm,
        value.toUpperCase(),
        ['applicableTo']
      );
    }
  }

  getCountries(): void {
    this.articleService.getCountries().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let allcountries = {
          label: result[i].name,
          value: result[i].name
        };
        this.allcountries = [...this.allcountries, allcountries];
      }
    });
  }
  onChanges(): void {
    this.articleMasterForm.get('applicableTo').valueChanges.subscribe(val => {
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

  disableDetailsFields(value: boolean) {
    if (value == true) {
      this.disableFileds(1);
    } else if (value == false) {
      this.disableFileds(2);
    }
  }

  disableFileds(id: any) {
    this.controlskds = this.articleMasterForm.get(
      'articleMasterConditions'
    ) as FormArray;
    const control = this.controlskds.controls[0].controls
      .articleMasterDetailedConditions as FormArray;
    if (id == 1) {
      control.controls['0'].controls.condition.setValidators([
        Validators.required,
        Validators.maxLength(100)
      ]);
      control.controls['0'].controls.condition.updateValueAndValidity();
    } else if (id == 2) {
      control.controls['0'].controls.condition.setValidators(
        Validators.maxLength(100)
      );
      control.controls['0'].controls.condition.updateValueAndValidity();
    }
  }

  initPayment(): FormGroup {
    return this.formBuilder.group({
      id: [undefined],
      condition: ['', [Validators.required, Validators.maxLength(100)]]
    });
  }

  addArticlelist(i: number): void {
    const control = this.articleMasterForm.get(
      `articleMasterConditions.${i}.articleMasterDetailedConditions`
    ) as FormArray;
    control.push(this.initPayment());
  }

  get f(): any {
    return this.articleMasterForm.controls;
  }
  goBack(): void {
    this.router.navigate(['/dashboard/masters/article']);
  }

  saveArticlelist(type: any): void {
    this.submitted = true;
    if (this.articleMasterForm.invalid) {
      return;
    }
    if (this.articleMasterForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.articleMasterForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.articleMasterForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.articleService.addArticlelist(this.articleMasterForm.value).subscribe(
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
  updateArticlelist(): void {
    this.submitted = true;
    if (this.articleMasterForm.invalid) {
      return;
    }
    this.articleService
      .updateArticlelistById(this.articleMasterForm.getRawValue())
      .subscribe(
        (res: any) => {
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
    // this.articleMasterForm.reset();
    this.submitted = false;
    this.initialLoading();
  }

  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/article']);
  }

  getMasterConditions(form: any): any {
    return form.controls.articleMasterConditions.controls;
  }

  getArticleMasterDetailedConditions(form: any): any {
    return form.controls.articleMasterDetailedConditions.controls;
  }

  removeCondition(j: number): void {
    this.controlskds = this.articleMasterForm.get(
      'articleMasterConditions'
    ) as FormArray;
    const controls2 = this.controlskds.controls[0].controls
      .articleMasterDetailedConditions as FormArray;
    controls2.removeAt(j);
  }

  private createArticleForm(): void {
    this.articleMasterForm = this.formBuilder.group({
      id: [null],
      country: ['', [Validators.required]],
      articleNumber: ['', [Validators.required]],
      articleName: ['', Validators.required],
      isInclusionOrExclusion: [false, Validators.required],
      articleRate: ['', Validators.required],
      natureOfRemittance: ['', Validators.required],
      makeAvailableConditionSatisfied: new FormControl(false),
      mfnClauseExists: new FormControl(false),
      mfnApplicableTo: new FormControl(''),
      mliPrinciplePurpose: new FormControl(false),
      mliSimplifiedLimitation: new FormControl(false),
      mfnClauseIsAvailed: [undefined, Validators.required],
      mfnClauseIsNotAvailed: [undefined, Validators.required],
      exempt: new FormControl(false),
      remarks: new FormControl(''),
      applicableFrom: new FormControl('', [Validators.required]),
      applicableTo: [undefined],
      articleMasterConditions: new FormArray([this.initArticleMaster()])
    });
  }
  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      if (!params.action || params.action === '') {
        this.actionState = UtilModule.stateChanger(
          this.articleMasterForm,
          'New'
        );
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
