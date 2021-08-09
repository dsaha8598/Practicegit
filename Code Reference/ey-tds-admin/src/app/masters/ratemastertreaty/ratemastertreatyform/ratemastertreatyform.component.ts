import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  FormControl
} from '@angular/forms';
//import { Http, Response } from '@angular/http';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { SelectItem } from 'primeng/api';
import { RateMasterTreatyService } from '../ratemastertreaty.service';
import {
  NgbDateAdapter,
  NgbDateNativeAdapter
} from '@ng-bootstrap/ng-bootstrap';
import { IRateMasterTreaty } from '@app/shared/model/rateMaster.treaty.model';
import { thisExpression } from 'babel-types';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-dividendrateform',
  templateUrl: './ratemastertreatyform.component.html',
  styleUrls: ['./ratemastertreatyform.component.scss']
})
export class RateMasterTreatyformComponent implements OnInit {
  DividendRateForm: FormGroup;
  submitted = false;
  actionState: string;
  isNatureOfPayment: string;
  minDate: any = undefined;
  valueChanges: boolean = false;
  submittedTo: boolean = false;
  applicableToDateError: boolean;
  headingMsg: string;
  isDeducteeStatus: boolean;
  deducteeSingleStatus: any = [];
  //getForeginShareholdingList: Array<any>;
  //getPeriodShareholdingList: Array<any>;
  // getResidentShareholdingList: Array<any>;
  getCountrylist: any;
  isPeiodOfShareholding: boolean = false;
  isIceland: boolean = false;
  isKuwait: boolean = false;
  isUK: boolean = false;
  updateID: any;
  getForeginShareholdingList: Array<any> = [
    { label: 'Select Company Shareholding', value: '' },
    { label: '<10', value: '< 10.00' },
    { label: '<=10', value: '<= 10.00' },
    { label: '>10', value: '> 10.00' },
    { label: '>=10', value: '>= 10.00' },
    { label: '<25', value: '< 25.00' },
    { label: '<=25', value: '<= 25.00' },
    { label: '>25', value: '> 25.00' },
    { label: '>=25', value: '>= 25.00' }
  ];

  getPeriodShareholdingList: Array<any> = [
    { label: 'Select period', value: '' },
    { label: '<0.5 years', value: '< 0.5' },
    { label: '<2 years', value: '< 2' },
    { label: '>=0.5 years', value: '>= 0.5' },
    { label: '>=2 years', value: '>= 2' }
  ];

  getResidentShareholdingList: Array<any> = [
    {
      label: 'Select Foreign Company Shareholding',
      value: ''
    },
    { label: '<=25', value: '<= 25' },
    { label: '>25', value: '> 25' }
  ];

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
    private readonly dividendrateService: RateMasterTreatyService,
    private readonly actRoute: ActivatedRoute,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.initialLoading();
  }

  initialLoading(): void {
    this.dividendRateForm();

    this.getCountrylist = [];
    this.getCountrylist.push({ label: 'Select country', value: '' });

    this.headingMsg = 'Add';
    this.getCountry();
    this.stateChecker();
  }

  getCountry() {
    this.dividendrateService.getCountries().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let allcountries = {
          label: result[i].name,
          value: result[i].id
        };
        this.getCountrylist = [...this.getCountrylist, allcountries];
      }
    });
    this.logger.debug('Country list' + this.getCountrylist);
  }

  get f(): any {
    return this.DividendRateForm.controls;
  }

  saveDividendRate(type: any): void {
    this.submitted = true;
    console.log('values', this.DividendRateForm.value);
    if (this.DividendRateForm.invalid) {
      return;
    }
    let applicableFromDate;
    let applicableToDate;
    if (this.DividendRateForm.controls.applicableTo.value != null) {
      applicableFromDate = this.getFormattedDate(
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
    let reqObj = {} as IRateMasterTreaty;
    reqObj.countryId = this.DividendRateForm.controls.countryname.value;
    reqObj.taxTreatyClause = this.DividendRateForm.controls.taxTreatyClause.value;
    reqObj.mfnClauseExists = this.DividendRateForm.controls.mfnClauseExists.value;
    reqObj.mfnAvailedCompanyTaxRate = this.DividendRateForm.controls.mfnAvailedCompanyTaxRate.value;
    reqObj.mfnAvailedNonCompanyTaxRate = this.DividendRateForm.controls.mfnAvailedNonCompanyTaxRate.value;
    reqObj.mfnNotAvailedCompanyTaxRate = this.DividendRateForm.controls.mfnNotAvailedCompanyTaxRate.value;
    reqObj.mfnNotAvailedNonCompanyTaxRate = this.DividendRateForm.controls.mfnNotAvailedNonCompanyTaxRate.value;
    reqObj.mliArticle8Applicable = this.DividendRateForm.controls.mliArticle8Applicable.value;
    reqObj.mliPptConditionSatisfied = this.DividendRateForm.controls.mliPptConditionSatisfied.value;
    reqObj.mliSlobConditionSatisfied = this.DividendRateForm.controls.mliSlobConditionSatisfied.value;
    reqObj.foreignCompShareholdingInIndComp = this.DividendRateForm.controls.foreignCompShareholdingInIndComp.value;
    if (
      this.isPeiodOfShareholding &&
      this.DividendRateForm.controls.PERIOD_OF_SHAREHOLDING.value != ''
    ) {
      reqObj.countrySpecificRules =
        'PERIOD_OF_SHAREHOLDING: ' +
        this.DividendRateForm.controls.PERIOD_OF_SHAREHOLDING.value;
    } else if (this.isIceland) {
      if (
        this.DividendRateForm.controls.SHAREHOLDING_IN_FOREIGN_COMPANY.value !=
        ''
      ) {
        reqObj.countrySpecificRules =
          'SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS : ' +
          this.DividendRateForm.controls.SHAREHOLDING_IN_FOREIGN_COMPANY.value +
          ';' +
          'IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE:' +
          this.DividendRateForm.controls.IS_DIVIDEND_TAXABLE_AT_A_RATE.value;
      } else {
        reqObj.countrySpecificRules =
          'IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE:' +
          this.DividendRateForm.controls.IS_DIVIDEND_TAXABLE_AT_A_RATE.value;
      }
    } else if (this.isKuwait) {
      reqObj.countrySpecificRules =
        'BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY:' +
        this.DividendRateForm.controls.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL
          .value;
    } else if (this.isUK) {
      reqObj.countrySpecificRules =
        'DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX:' +
        this.DividendRateForm.controls.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY
          .value;
    }
    reqObj.applicableFrom = this.DividendRateForm.controls.applicableFrom.value;
    reqObj.applicableTo = this.DividendRateForm.controls.applicableTo.value;

    this.dividendrateService
      .addRateMasterTreatyRate(reqObj as IRateMasterTreaty)
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
      .updateRateMasterTreatyById(this.updateID, applicableToDate)
      .subscribe(
        (res: IRateMasterTreaty) => {
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
    this.dividendrateService.getRateMasterTreatyById(id).subscribe(
      (result: any) => {
        let resObj: IRateMasterTreaty = result;
        if (result && result.applicableTo !== null) {
          this.DividendRateForm.get('applicableTo').setValidators(
            Validators.required
          );
        }
        this.headingMsg = 'View';
        this.disablePreviousDate(result.applicableFrom);
        resObj.countryname = result.country.id;
        if (result.country.id == 8) {
          this.isIceland = true;
          resObj.SHAREHOLDING_IN_FOREIGN_COMPANY =
            result.countrySpecificRules.rules.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS;
          resObj.IS_DIVIDEND_TAXABLE_AT_A_RATE =
            result.countrySpecificRules.rules.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE;
        } else if (result.country.id == 31) {
          this.isPeiodOfShareholding = true;
          resObj.PERIOD_OF_SHAREHOLDING =
            result.countrySpecificRules.rules.PERIOD_OF_SHAREHOLDING;
        } else if (result.country.id == 153) {
          this.isKuwait = true;
          resObj.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL =
            result.countrySpecificRules.rules.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY;
        } else if (result.country.id == 225) {
          this.isUK = true;
          resObj.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY =
            result.countrySpecificRules.rules.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX;
        }
        this.DividendRateForm.patchValue(resObj);
      },
      (error: any) => {}
    );
  }
  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/ratemastertreaty']);
  }

  stateChanger(value: any): any {
    this.actionState = UtilModule.stateChanger(this.DividendRateForm, value, [
      'applicableTo'
    ]);
    this.headingMsg = 'Update';
  }

  private dividendRateForm(): void {
    this.DividendRateForm = new FormGroup({
      countryname: new FormControl('', Validators.required),
      id: new FormControl(''),
      taxTreatyClause: new FormControl('', [
        Validators.required,
        Validators.maxLength(20)
      ]),
      mliArticle8Applicable: new FormControl(false, Validators.required),
      mliPptConditionSatisfied: new FormControl(false, Validators.required),
      mliSlobConditionSatisfied: new FormControl(false, Validators.required),
      foreignCompShareholdingInIndComp: new FormControl(''),
      applicableFrom: new FormControl('', Validators.required),
      applicableTo: new FormControl(undefined),
      mfnClauseExists: new FormControl(false, Validators.required),
      PERIOD_OF_SHAREHOLDING: new FormControl(''),
      SHAREHOLDING_IN_FOREIGN_COMPANY: new FormControl(''),
      IS_DIVIDEND_TAXABLE_AT_A_RATE: new FormControl(false),
      BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL: new FormControl(false),
      DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY: new FormControl(false),
      mfnNotAvailedCompanyTaxRate: new FormControl(null, Validators.required),
      mfnNotAvailedNonCompanyTaxRate: new FormControl(
        null,
        Validators.required
      ),
      mfnAvailedCompanyTaxRate: new FormControl(null),
      mfnAvailedNonCompanyTaxRate: new FormControl(null),
      countryId: new FormControl(0),
      countrySpecificRules: new FormControl('')
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

  changeHandler(): void {
    if (this.DividendRateForm.value.mfnClauseExists) {
      this.DividendRateForm.controls.mfnNotAvailedCompanyTaxRate.enable();
      this.DividendRateForm.controls.mfnNotAvailedNonCompanyTaxRate.enable();
      this.DividendRateForm.controls.mfnAvailedCompanyTaxRate.enable();
      this.DividendRateForm.controls.mfnAvailedNonCompanyTaxRate.enable();

      this.DividendRateForm.get('mfnNotAvailedCompanyTaxRate').setValidators(
        Validators.required
      );
      this.DividendRateForm.get('mfnNotAvailedNonCompanyTaxRate').setValidators(
        Validators.required
      );
      this.DividendRateForm.get('mfnAvailedCompanyTaxRate').setValidators(
        Validators.required
      );
      this.DividendRateForm.get('mfnAvailedNonCompanyTaxRate').setValidators(
        Validators.required
      );
      this.DividendRateForm.get(
        'mfnNotAvailedCompanyTaxRate'
      ).updateValueAndValidity();
      this.DividendRateForm.get(
        'mfnNotAvailedNonCompanyTaxRate'
      ).updateValueAndValidity();
      this.DividendRateForm.get(
        'mfnAvailedCompanyTaxRate'
      ).updateValueAndValidity();
      this.DividendRateForm.get(
        'mfnAvailedNonCompanyTaxRate'
      ).updateValueAndValidity();
    } else {
      this.DividendRateForm.controls.mfnAvailedCompanyTaxRate.disable();
      this.DividendRateForm.controls.mfnAvailedNonCompanyTaxRate.disable();

      this.DividendRateForm.controls.mfnNotAvailedCompanyTaxRate.enable();
      this.DividendRateForm.controls.mfnNotAvailedNonCompanyTaxRate.enable();

      this.DividendRateForm.get('mfnNotAvailedCompanyTaxRate').setValidators(
        Validators.required
      );
      this.DividendRateForm.get('mfnNotAvailedNonCompanyTaxRate').setValidators(
        Validators.required
      );
    }
  }

  onChange(event: any) {
    console.log('' + this.DividendRateForm.controls.countryname);
    if (this.DividendRateForm.controls.countryname.value === 8) {
      //Iceland

      this.isIceland = true;
      this.isPeiodOfShareholding = false;
      this.isKuwait = false;
      this.isUK = false;
      this.DividendRateForm.controls.SHAREHOLDING_IN_FOREIGN_COMPANY.enable();
      /*   this.DividendRateForm.get(
        'SHAREHOLDING_IN_FOREIGN_COMPANY'
      ).setValidators(Validators.required);
      this.DividendRateForm.get(
        'SHAREHOLDING_IN_FOREIGN_COMPANY'
      ).updateValueAndValidity();
     */

      this.DividendRateForm.get('IS_DIVIDEND_TAXABLE_AT_A_RATE').setValidators(
        Validators.required
      );
      this.DividendRateForm.get(
        'IS_DIVIDEND_TAXABLE_AT_A_RATE'
      ).updateValueAndValidity();
    } else if (this.DividendRateForm.controls.countryname.value === 31) {
      this.isPeiodOfShareholding = true;
      this.isIceland = false;
      this.isKuwait = false;
      this.isUK = false;
      this.DividendRateForm.controls.PERIOD_OF_SHAREHOLDING.enable();
      /*   this.DividendRateForm.get('PERIOD_OF_SHAREHOLDING').setValidators(
        Validators.required
      );
      this.DividendRateForm.get(
        'PERIOD_OF_SHAREHOLDING'
      ).updateValueAndValidity();*/
    } else if (this.DividendRateForm.controls.countryname.value === 153) {
      //Kuwait
      this.isKuwait = true;
      this.isIceland = false;
      this.isPeiodOfShareholding = false;
      this.isUK = false;
      this.DividendRateForm.controls.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL.enable();
      this.DividendRateForm.get(
        'BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL'
      ).setValidators(Validators.required);
      this.DividendRateForm.get(
        'BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL'
      ).updateValueAndValidity();
    } else if (this.DividendRateForm.controls.countryname.value === 225) {
      //UK
      this.isUK = true;
      this.isIceland = false;
      this.isPeiodOfShareholding = false;
      this.isKuwait = false;
      this.DividendRateForm.controls.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY.enable();
      this.DividendRateForm.get(
        'DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY'
      ).setValidators(Validators.required);
      this.DividendRateForm.get(
        'DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY'
      ).updateValueAndValidity();
    } else {
      this.isUK = false;
      this.isIceland = false;
      this.isPeiodOfShareholding = false;
      this.isKuwait = false;
    }
  }
}
