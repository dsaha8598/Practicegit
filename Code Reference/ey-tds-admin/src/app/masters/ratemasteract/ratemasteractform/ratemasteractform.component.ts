import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  FormControl
} from '@angular/forms';
import { ActivatedRoute, Router, NavigationExtras } from '@angular/router';
import { UtilModule } from '@app/shared';
import { SelectItem } from 'primeng/api';
import { RateMasterActService } from '../ratemasteract.service';
import {
  NgbDateAdapter,
  NgbDateNativeAdapter
} from '@ng-bootstrap/ng-bootstrap';
import { IRateMasterAct } from '@app/shared/model/rateMaster.act.model';
import { thisExpression } from 'babel-types';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-dividendrateform',
  templateUrl: './ratemasteractform.component.html',
  styleUrls: ['./ratemasteractform.component.scss']
})
export class RateMasterActformComponent implements OnInit {
  DividendRateForm: FormGroup;
  submitted = false;
  actionState: string;
  isNatureOfPayment: string;
  minDate: any = undefined;
  residentialStatus: Array<any>;
  valueChanges: boolean = false;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  headingMsg: string;
  isDeducteeStatus: boolean;
  deducteeSingleStatus: any = [];
  getDeductorlist: Array<object>;
  deductortyprlisterror: boolean;
  getshareholderlist: Array<any>;
  ratemasterResidentialStatus: Array<object>;
  sectionerror: boolean;
  updateID: any;
  dropdownSettings = {
    singleSelection: false,
    idField: 'id',
    textField: 'status',
    selectAllText: 'Select All',
    unSelectAllText: 'UnSelect All',
    itemsShowLimit: 3,
    allowSearchFilter: true
  };

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly dividendrateService: RateMasterActService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.dividendRateForm();

    this.getDeductorlist = [];
    this.getDeductorlist.push({ label: 'Select deductor', value: '' });

    this.getshareholderlist = [];
    this.getshareholderlist.push({
      label: 'Please Select Shareholder Category ',
      value: ''
    });
    this.ratemasterResidentialStatus = [];
    this.ratemasterResidentialStatus.push({
      label: 'Select Residential status',
      value: ''
    });

    this.headingMsg = 'Add';
    this.getShareholder();
    this.getResidentialStatus();
    this.getDeductorType();
    this.stateChecker();
  }

  getResidentialStatus() {
    this.dividendrateService
      .getResidentialStatusList()
      .subscribe((data: any) => {
        this.logger.debug('Resident Types' + data);
        for (let i = 0; i < data.length; i++) {
          if (data[i].status != 'RNOR') {
            let residentTypelist = {
              label: this.abbrivations(data[i].status.toUpperCase()),
              value: data[i].status
            };
            this.ratemasterResidentialStatus = [
              ...this.ratemasterResidentialStatus,
              residentTypelist
            ];
          }
        }
      });
    return this.ratemasterResidentialStatus;
  }

  abbrivations(key: string): string {
    const abbrivations = {
      RES: 'Resident',
      NR: 'Non-Resident'
    };
    return abbrivations[key];
  }

  getDeductorType() {
    this.dividendrateService.getDeductorType().subscribe(
      (result: any) => {
        this.logger.debug('Dividend Decutor Types' + result);
        for (let i = 0; i < result.length; i++) {
          let resideductortyprlist = {
            label: result[i].name,
            value: result[i].id
          };
          this.getDeductorlist = [
            ...this.getDeductorlist,
            resideductortyprlist
          ];
        }
      },
      (err: any) => {
        this.deductortyprlisterror = true;
      }
    );

    return this.getDeductorlist;
  }

  getShareholder() {
    this.dividendrateService.getShareholderCategoryALLlist().subscribe(
      (result: any) => {
        this.logger.debug('Shareholder category List' + result);
        for (let i = 0; i < result.length; i++) {
          let shareholderCategorylist = {
            label: result[i].name,
            value: result[i].id
          };
          this.getshareholderlist = [
            ...this.getshareholderlist,
            shareholderCategorylist
          ];
        }
      },
      (err: any) => {
        this.deductortyprlisterror = true;
      }
    );

    return this.getshareholderlist;
  }

  get f(): any {
    return this.DividendRateForm.controls;
  }

  saveDividendRate(type: any): void {
    this.deducteeSingleStatus;
    this.submitted = true;
    console.log('values', this.DividendRateForm.value);
    if (this.DividendRateForm.invalid) {
      return;
    }

    if (this.DividendRateForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.DividendRateForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.DividendRateForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.dividendrateService
      .addRateMasterActData(this.DividendRateForm.value as IRateMasterAct)
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

  updateDividendRate(): void {
    this.submittedTo = true;
    if (this.DividendRateForm.invalid) {
      return;
    }
    let applicableToDate;
    if (this.DividendRateForm.controls.applicableTo.value != null) {
      let applicableFromDate = this.getFormattedDate(
        this.DividendRateForm.controls.applicableFrom.value
      );
      applicableToDate = this.getFormattedDate(
        this.DividendRateForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    this.dividendrateService
      .updateRateMasterActById(this.updateID, applicableToDate)
      .subscribe(
        (res: IRateMasterAct) => {
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
    UtilModule.reset(this.DividendRateForm);
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  fetchDatabyId(id: any): void {
    this.dividendrateService.getRateMasterActById(id).subscribe(
      (result: any) => {
        let resObj: IRateMasterAct = result as IRateMasterAct;
        if (result && result.applicableTo !== null) {
          this.DividendRateForm.get('applicableTo').setValidators(
            Validators.required
          );
        }
        this.headingMsg = 'View';
        this.disablePreviousDate(result.applicableFrom);
        resObj.dividendDeductorTypeId = result.dividendDeductorType.id;
        resObj.shareholderCategoryId = result.shareholderCategory.id;
        resObj.residentialStatus = result.residentialStatus;

        this.DividendRateForm.patchValue(resObj);
        console.log(this.DividendRateForm.getRawValue());
      },
      (error: any) => {}
    );
  }
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/ratemasteract']);
  }

  stateChanger(value: any): any {
    this.actionState = UtilModule.stateChanger(this.DividendRateForm, value, [
      'applicableTo'
    ]);
    this.headingMsg = 'Update';
  }
  getShareholderCategoryByDeductorType(event: any) {
    this.getshareholderlist = [];
    this.getshareholderlist.push({
      label: 'Please Select Shareholder Category ',
      value: ''
    });
    this.dividendrateService.getShareholderCategoryList(event.value).subscribe(
      (result: any) => {
        this.logger.debug('Shareholder category List' + result);
        for (let i = 0; i < result.length; i++) {
          let shareholderCategorylist = {
            label: result[i].shareholderCategory.name,
            value: result[i].shareholderCategory.id,
            exempted: result[i].shareholderCategory.exempted
          };
          this.getshareholderlist = [
            ...this.getshareholderlist,
            shareholderCategorylist
          ];
        }
      },
      (err: any) => {
        this.deductortyprlisterror = true;
      }
    );

    return this.getshareholderlist;
  }

  onChange(event: any) {
    if (
      this.DividendRateForm.controls.dividendDeductorTypeId.value != '' &&
      this.DividendRateForm.controls.shareholderCategoryId.value != '' &&
      this.DividendRateForm.controls.residentialStatus.value != ''
    ) {
      this.dividendrateService
        .getSection(
          this.DividendRateForm.controls.dividendDeductorTypeId.value,
          this.DividendRateForm.controls.shareholderCategoryId.value,
          this.DividendRateForm.controls.residentialStatus.value
        )
        .subscribe(
          (result: any) => {
            if (result.length > 0) {
              if (result[0].section != '') {
                this.logger.debug(
                  'Shareholder category List' + result[0].section
                );
                this.DividendRateForm.controls.section.setValue(
                  result[0].section
                );
                this.sectionerror = false;
              } else {
                this.DividendRateForm.controls.section.setValue('');
                this.sectionerror = true;
              }
            } else {
              this.DividendRateForm.controls.section.setValue('');
              this.sectionerror = true;
            }
          },
          (err: any) => {
            this.deductortyprlisterror = true;
          }
        );
    }
  }

  private dividendRateForm(): void {
    this.DividendRateForm = new FormGroup({
      dividendDeductorTypeId: new FormControl('', Validators.required),
      section: new FormControl('', [Validators.required]),
      tdsRate: new FormControl(null, Validators.required),
      shareholderCategoryId: new FormControl('', Validators.required),
      residentialStatus: new FormControl('', Validators.required),
      exemptionThreshold: new FormControl(null, Validators.required),
      applicableFrom: new FormControl('', Validators.required),
      applicableTo: new FormControl(undefined)
    });
  }
  public stateChecker(): void {
    this.actRoute.queryParams.subscribe(
      (params: any) => {
        if (!params.action || params.action === '') {
          this.actionState = UtilModule.stateChanger(
            this.DividendRateForm,
            'New'
          );
        } else if (params.action.toUpperCase() === 'EDIT') {
          this.actionState = UtilModule.stateChanger(
            this.DividendRateForm,
            params.action
          );
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
            this.updateID = params.id;
          }
        } else {
          this.actionState = UtilModule.stateChanger(
            this.DividendRateForm,
            params.action
          );
          if (params.id && params.id !== 0) {
            this.fetchDatabyId(params.id);
            this.updateID = params.id;
          }
        }
      },
      (error: any) => {
        this.logger.error(error);
      }
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
}
