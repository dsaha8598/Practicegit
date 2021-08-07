import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ITableConfig } from '@app/shared/model/common.model';
import { IRateMasterTreaty } from '@app/shared/model/rateMaster.treaty.model';
import { RateMasterTreatyService } from './ratemastertreaty.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { HttpHeaders } from '@angular/common/http';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { BatchService } from '@app/shared/services/batch/batch.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { IStatus } from '@app/shared/model/status.model';

@Component({
  selector: 'ey-ratemasterTreaty',
  templateUrl: './ratemastertreaty.component.html',
  styleUrls: ['./ratemastertreaty.component.scss']
})
export class RateMasterTreatyComponent implements OnInit {
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  rateMasterTreatyPageObj: any;
  rateMasterTreatyList: Array<IRateMasterTreaty>;
  display: boolean;
  filesExists: boolean = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  ratemasterUploadForm: FormGroup;
  fileTypeOf: string = 'DIVIDEND_RATE_TREATY_EXCEL';
  statusList: Array<IStatus>;
  dividendTreatyYear: number;
  tabIndex: number;
  constructor(
    private readonly rateMasterTreatyService: RateMasterTreatyService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly authenticationService: AuthenticationService,
    private readonly batchService: BatchService,
    private readonly cd: ChangeDetectorRef,
    private readonly sanitizer: DomSanitizer
  ) {}
  ngOnInit(): void {
    this.dividendTreatyYear = UtilModule.getCurrentFinancialYear();
    this.rateMasterTreatyList = [];
    this.cols = [
      {
        field: 'countryname',
        header: 'Name of country',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'taxTreatyClause',
        header: 'Relevant tax treaty clause',
        width: '200px'
      },
      {
        field: 'mfnClauseDisplay',
        header: 'Most favoured nation (MFN) clause exists',
        width: '350px'
      },
      {
        field: 'mfnAvailedCompanyTaxRateDisplay',
        header:
          'Withholding tax rate applicable to company(MFN clause is availed)',
        width: '500px'
      },
      {
        field: 'mfnAvailedNonCompanyTaxRateDisplay',
        header:
          'Withholding tax rate applicable to non-company(MFN clause is availed)',
        width: '500px'
      },
      {
        field: 'mfnNotAvailedNonCompanyTaxRateDisplay',
        header:
          'Withholding tax rate applicable to company(MFN clause is not availed)',
        width: '500px'
      },
      {
        field: 'mfnNotAvailedCompanyTaxRateDisplay',
        header:
          'Withholding tax rate applicable to non-company(MFN clause is not availed)',
        width: '500px'
      },
      {
        field: 'mliArticle8ApplicableDisplay',
        header:
          'Article 8 of multilateral instrument (MLI) (>=365 days period Of shareholding)',
        width: '520px'
      },
      {
        field: 'mliPptConditionSatisfiedDisplay',
        header: 'MLI principle purpose test condition satisfied',
        width: '400px'
      },
      {
        field: 'mliSlobConditionSatisfiedDisplay',
        header: 'MLI simplified limitation on benefit condition satisifed',
        width: '400px'
      },
      {
        field: 'foreignCompShareholdingInIndComp',
        header: 'Foreign company shareholding in indian company (%)',
        width: '400px'
      },
      {
        field: 'PERIOD_OF_SHAREHOLDING',
        header: 'Period of shareholding',
        width: '200px'
      },
      {
        field: 'SHAREHOLDING_IN_FOREIGN_COMPANY',
        header:
          'Shareholding in foreign company by persons other than individuals residents',
        width: '500px'
      },
      {
        field: 'IS_DIVIDEND_TAXABLE_AT_A_RATE_Display',
        header: 'Is Dividend Taxable At a Rate Lower Than  Corporate Tax Rate',
        width: '450px'
      },
      {
        field: 'BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display',
        header:
          'Beneficial owner of dividend is foreign government or political sub division or a local authority or the central bank  or other governmental agencies or governmental financial institutions',
        width: '1000px'
      },
      {
        field: 'DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY',
        header:
          'Dividend derived from immovable property by an investment vehicle whose income from such immovable property is exempt from tax',
        width: '1000px'
      }
    ];
    this.ratemasterUploadForm = new FormGroup({
      file: new FormControl(null, Validators.required)
    });
    this.getData();
    this.tabIndex = 0;
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }
  showDialog(): void {
    this.display = true;
  }
  getData(): void {
    this.logger.debug(this.rateMasterTreatyList);
    this.rateMasterTreatyList = [];
    this.rateMasterTreatyService.getRateMasterTreatyList().subscribe(
      (result: Array<any>) => {
        result.forEach(data => {
          let data1: IRateMasterTreaty = data;
          data1.countryname = data.country.name;
          if (data.country.id == 31) {
            if (
              data.countrySpecificRules &&
              data.countrySpecificRules.rules != null
            ) {
              data1.PERIOD_OF_SHAREHOLDING =
                data.countrySpecificRules.rules.PERIOD_OF_SHAREHOLDING;
              data1.PERIOD_OF_SHAREHOLDING =
                data.countrySpecificRules.rules.PERIOD_OF_SHAREHOLDING;
            }
          } else {
            data1.PERIOD_OF_SHAREHOLDING = 'NA';
          }

          if (data.country.id == 8) {
            if (
              data.countrySpecificRules &&
              data.countrySpecificRules.rules != null
            ) {
              data1.SHAREHOLDING_IN_FOREIGN_COMPANY =
                data.countrySpecificRules.rules.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS;
            }
            // data1.IS_DIVIDEND_TAXABLE_AT_A_RATE =
            // data.countrySpecificRules.rules.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE;
            if (
              data.countrySpecificRules &&
              data.countrySpecificRules.rules &&
              data.countrySpecificRules.rules
                .IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE
            ) {
              data1.IS_DIVIDEND_TAXABLE_AT_A_RATE_Display = 'YES';
            } else {
              data1.IS_DIVIDEND_TAXABLE_AT_A_RATE_Display = 'NO';
            }
          } else {
            data1.SHAREHOLDING_IN_FOREIGN_COMPANY = 'NA';
            data1.IS_DIVIDEND_TAXABLE_AT_A_RATE_Display = 'NA';
          }
          if (data.country.id == 153) {
            //data1.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL =
            //data.countrySpecificRules.rules.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY;
            if (
              data.countrySpecificRules &&
              data.countrySpecificRules.rules &&
              data.countrySpecificRules.rules
                .BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY
            ) {
              data1.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display = 'YES';
            } else {
              data1.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display = 'NO';
            }
          } else {
            data1.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_Display = 'NA';
          }
          if (data.country.id == 225) {
            //  data1.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY =
            //  data.countrySpecificRules.rules.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX;
            if (
              data.countrySpecificRules &&
              data.countrySpecificRules.rules &&
              data.countrySpecificRules.rules
                .DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX
            ) {
              data1.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY = 'YES';
            } else {
              data1.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY = 'NO';
            }
          } else {
            data1.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_DISPLAY = 'NA';
          }
          if (data.mfnClauseExists) {
            data1.mfnClauseDisplay = 'YES';
          } else {
            data1.mfnClauseDisplay = 'NO';
          }
          if (data.mliArticle8Applicable) {
            data1.mliArticle8ApplicableDisplay = 'YES';
          } else if (data.mliArticle8Applicable == null) {
            data1.mliArticle8ApplicableDisplay = 'NA';
          } else {
            data1.mliArticle8ApplicableDisplay = 'NA';
          }

          if (data.mliPptConditionSatisfied) {
            data1.mliPptConditionSatisfiedDisplay = 'YES';
          } else if (data.mliPptConditionSatisfied == null) {
            data1.mliPptConditionSatisfiedDisplay = 'NA';
          } else {
            data1.mliPptConditionSatisfiedDisplay = 'NA';
          }

          if (data.mliSlobConditionSatisfied) {
            data1.mliSlobConditionSatisfiedDisplay = 'YES';
          } else if (data.mliSlobConditionSatisfied == null) {
            data1.mliSlobConditionSatisfiedDisplay = 'NA';
          } else {
            data1.mliSlobConditionSatisfiedDisplay = 'NA';
          }

          if (
            data.mfnAvailedCompanyTaxRate != 0 &&
            data.mfnAvailedCompanyTaxRate != null
          ) {
            data1.mfnAvailedCompanyTaxRateDisplay =
              data.mfnAvailedCompanyTaxRate;
          } else {
            data1.mfnAvailedCompanyTaxRateDisplay = 'NA';
          }
          if (
            data.mfnAvailedNonCompanyTaxRate != 0 &&
            data.mfnAvailedNonCompanyTaxRate != null
          ) {
            data1.mfnAvailedNonCompanyTaxRateDisplay =
              data.mfnAvailedNonCompanyTaxRate;
          } else {
            data1.mfnAvailedNonCompanyTaxRateDisplay = 'NA';
          }
          if (
            data.mfnNotAvailedNonCompanyTaxRate != 0 &&
            data.mfnNotAvailedNonCompanyTaxRate != null
          ) {
            data1.mfnNotAvailedNonCompanyTaxRateDisplay =
              data.mfnNotAvailedNonCompanyTaxRate;
          } else {
            data1.mfnNotAvailedNonCompanyTaxRateDisplay = 'NA';
          }
          if (
            data.mfnNotAvailedCompanyTaxRate != 0 &&
            data.mfnNotAvailedCompanyTaxRate != null
          ) {
            data1.mfnNotAvailedCompanyTaxRateDisplay =
              data.mfnNotAvailedCompanyTaxRate;
          } else {
            data1.mfnNotAvailedCompanyTaxRateDisplay = 'NA';
          }
          if (
            data.foreignCompShareholdingInIndComp != '' &&
            data.foreignCompShareholdingInIndComp != null
          ) {
            data1.foreignCompShareholdingInIndComp =
              data.foreignCompShareholdingInIndComp;
          } else {
            data1.foreignCompShareholdingInIndComp = 'NA';
          }
          this.rateMasterTreatyList.push(data1);
        });
        this.logger.debug('********Treaty Result from service*****' + result);
        // this.rateMasterTreatyList = result;
        this.logger.debug(
          '********Treaty Result from handle local variable*****' + result
        );
      },
      (error: any) => this.logger.error(error)
    );
  }

  onTabChange(event: any): void {
    this.tabIndex = event.index;
    if (event.index === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }

  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }

  getStatus(): void {
    this.batchService
      .getBatchUploadBasedOnType(this.fileTypeOf, this.dividendTreatyYear)
      .subscribe(
        (result: any) => {
          console.table([result]);
          this.statusList = result;
        },
        (error: any) => this.logger.error(error)
      );
  }
  getSelectedYear(event: any): void {
    this.dividendTreatyYear = event;
    this.getStatus();
  }

  uploadHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        // this.shareholderUploadForm.controls.file = this.files[0];
        this.ratemasterUploadForm.patchValue({ file: this.files[0] });
      } else {
        this.filesExists = false;
      }

      this.cd.detectChanges();
    }
  }

  get f(): any {
    return this.ratemasterUploadForm.controls;
  }
  clearUpload(): void {
    this.ratemasterUploadForm.patchValue({
      files: []
    });
    this.filesExists = false;
    this.fileLoading = false;
    this.uploadSuccess = false;
    this.uploadError = false;
  }
  formatBytes(size: number): string {
    return UtilModule.formatBytes(size);
  }
  get rawValues(): any {
    return this.ratemasterUploadForm.getRawValue();
  }
  uploadFiles(): void {
    //  this.resetPageState();
    const formData = new FormData();
    formData.append('file', this.ratemasterUploadForm.getRawValue().file);
    this.fileLoading = true;
    this.filesExists = false;
    this.rateMasterTreatyService.uploadRateMasterTreaty(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.ratemasterUploadForm.patchValue({ file: null });
        setTimeout(() => {
          this.display = false;
          this.uploadSuccess = false;
          this.getStatus();
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.ratemasterUploadForm.patchValue({ file: null });
        setTimeout(() => {
          this.display = false;
          this.getStatus();
        }, 3000);
      }
    );
    this.cd.markForCheck();
  }
  removeFiles(): void {
    this.ratemasterUploadForm.patchValue({
      files: []
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }

  sanitize(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }
  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
