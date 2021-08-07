import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { CurrencyConvertorMasterService } from './currency-convertor-master.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { ITableConfig } from '@app/shared/model/common.model';
import { DatePipe, formatDate } from '@angular/common';
import {
  NgbDateNativeAdapter,
  NgbDateAdapter,
  NgbDatepickerConfig
} from '@ng-bootstrap/ng-bootstrap';
@Component({
  selector: 'ey-currency-convertor-master',
  templateUrl: './currency-convertor-master.component.html',
  styleUrls: ['./currency-convertor-master.component.scss']
})
export class CurrencyConvertorMasterComponent implements OnInit {
  display: boolean;
  currencyConvertorUploadForm: FormGroup;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  filesExists: boolean;
  files: Array<any>;
  currencyDate: any;
  currencyRateList: Array<Object>;
  statusList: Array<Object>;
  currencyRateCols: Array<ITableConfig>;
  statusCols: Array<ITableConfig>;
  currencyRateSelectedColumns: Array<ITableConfig>;
  statusSelectedColumns: Array<ITableConfig>;
  date: any;
  year: number;
  fileType: string = 'CURRENCY_CONVERTER';
  constructor(
    private readonly currencyConvertorMasterService: CurrencyConvertorMasterService,
    private readonly cd: ChangeDetectorRef,
    private logger: CustomLoggerService,
    private datePipe: DatePipe
  ) {}

  ngOnInit() {
    this.currencyConvertorUploadForm = new FormGroup({
      file: new FormControl(null, Validators.required)
    });
    this.currencyRateCols = [
      {
        field: 'currencyName',
        header: 'Currency Name',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'buyingBill',
        header: 'Buying bill',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'crossBuyingRate',
        header: 'Cross Buying Rate',
        width: '200px'
      },
      {
        field: 'crossSellingRate',
        header: 'Cross Selling Rate',
        width: '200px'
      },
      {
        field: 'sellingBill',
        header: 'Selling Bill',
        width: '200px'
      },
      {
        field: 'sellingTT',
        header: 'Selling IT',
        width: '200px'
      }
    ];
    /* this.statusCols = [
      {
        field: 'fileName',
        header: 'File name',
        width: '200px'
      },
      {
        field: 'createdDate',
        header: 'Uploaded Date',
        width: '200px',
        type:'date'
      },

      {
        field: 'link',
        header: 'Download File',
        width: '200px',
        type: 'link'
      }
    ];
     this.statusSelectedColumns = this.statusCols;
  */
    this.year = UtilModule.getCurrentFinancialYear();
    this.currencyRateSelectedColumns = this.currencyRateCols;
    this.getCurrencyRatesData();
  }

  getCurrencyRatesData(): void {
    this.currencyConvertorMasterService.getCurrencyRatesMasterList().subscribe(
      (result: any) => {
        this.currencyRateList = result;
        //   console.log('currencyRateList', this.currencyRateList);
      },
      (error: any) => this.logger.error(error)
    );
  }

  refreshData(): void {
    this.getCurrencyRatesData();
  }
  /* getStatusData(): void {
    this.currencyConvertorMasterService.getCurrencyStatusData().subscribe(
      (result: any) => {
        this.statusList = result;
      },
      (error: any) => this.logger.error(error)
    );
  }*/

  filerCurrencyData() {
    this.currencyDate = this.datePipe.transform(
      this.currencyDate,
      'dd-MM-yyyy'
    );
    this.currencyConvertorMasterService
      .getFilteredCurrency(this.currencyDate)
      .subscribe(
        (res: any) => {
          this.currencyRateList = res.data;
        },
        error => {}
      );
  }

  showDialog(): void {
    this.display = true;
  }

  clearUpload(): void {
    this.currencyConvertorUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.fileLoading = false;
    this.uploadSuccess = false;
    this.uploadError = false;
  }

  get rawValues(): any {
    return this.currencyConvertorUploadForm.getRawValue();
  }

  formatBytes(size: number): string {
    return UtilModule.formatBytes(size);
  }

  uploadFileHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        this.currencyConvertorUploadForm.patchValue({
          file: this.files[0]
        });
      } else {
        this.filesExists = false;
      }
      this.cd.markForCheck();
    }
  }

  uploadFiles(): void {
    const formData = new FormData();
    formData.append('year', UtilModule.getCurrentFinancialYear().toString());
    formData.append('type', 'CURRENCY_CONVERTER');
    formData.append(
      'file',
      this.currencyConvertorUploadForm.getRawValue().file
    );
    this.fileLoading = true;
    this.filesExists = false;
    this.currencyConvertorMasterService.uploadPdfs(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.currencyConvertorUploadForm.patchValue({
          file: null
        });
        setTimeout(() => {
          this.display = false;
          this.getCurrencyRatesData();
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.currencyConvertorUploadForm.patchValue({
          file: null
        });
        setTimeout(() => {
          this.display = false;
          this.getCurrencyRatesData();
        }, 3000);
      }
    );
  }

  removeFiles(): void {
    this.currencyConvertorUploadForm.patchValue({
      file: null
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }
}
