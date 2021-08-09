import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
//import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { TdsrateService } from './tdsrate.service';
import { ITDSRate } from '@app/shared/model/tds.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { UtilModule } from '@app/shared';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-tdsrate',
  templateUrl: './tdsrate.component.html',
  styleUrls: ['./tdsrate.component.scss']
})
export class TdsrateComponent implements OnInit {
  statusList: Array<IStatus>;
  fileTypeOf: string = 'NATURE_OF_PAYMENT_EXCEL';
  display: boolean;
  tdsRateList: Array<ITDSRate>;
  scrollableCols: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  home: any;
  filesExists: boolean;
  uploadFileDisplay = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  tdsRateUploadForm: FormGroup;
  year: number;
  tabIndex: number;
  constructor(
    private readonly tdsrateService: TdsrateService,
    private readonly router: Router,
    private readonly logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}
  ngOnInit(): void {
    this.getTdsData();
    this.year = UtilModule.getCurrentFinancialYear();
    this.cols = [
      {
        field: 'rate',
        header: 'Rate',
        width: '100px',
        type: 'initial'
      },
      {
        field: 'subNaturePaymentMaster',
        header: 'Sub-nature of payment',
        width: '250px'
      },
      {
        field: 'natureOfPaymentMaster',
        header: 'Nature of payment',
        width: '200px'
      },
      {
        field: 'rateForNoPan',
        header: 'No PAN / Aadhar Rate',
        width: '200px'
      },
      {
        field: 'noItrRate',
        header: 'No ITR Rate',
        width: '200px'
      },
      {
        field: 'noPanRateAndNoItrRate',
        header: 'No Pan Rate & No ITR Rate',
        width: '200px'
      },
      {
        field: 'saccode',
        header: 'SAC code',
        width: '150px'
      },
      {
        field: 'isPerTransactionLimitApplicable',
        header: 'Is per transaction applicable',
        width: '350px'
      },
      // {
      //   field: 'isAnnualTransactionLimitApplicable',
      //   header: 'Is annual transaction limit applicable',
      //   width: '350px'
      // },
      // {
      //   field: 'annualTransactionLimit',
      //   header: 'Annual transaction limit',
      //   width: '270px'
      // },
      {
        field: 'perTransactionLimit',
        header: 'Per transaction limit',
        width: '200px'
      },
      {
        field: 'statusName',
        header: 'Deductee status',
        width: '200px'
      },
      {
        field: 'residentialStatusName',
        header: 'Deductee residential status',
        width: '250px',
        type: 'resStatusName'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '200px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '250px',
        type: 'date'
      },
      {
        field: 'action',
        header: 'Action',
        width: '100px',
        type: 'action'
      }
    ];
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
    this.tdsRateUploadForm = new FormGroup({
      file: new FormControl(null, Validators.required)
    });
    this.tabIndex = 0;
  }
  onTabChange(event: any): void {
    this.tabIndex = event.index;
    if (event.index === 0) {
      this.getTdsData();
    } else {
      this.getFileStatus();
    }
  }

  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getTdsData();
    } else {
      this.getFileStatus();
    }
  }

  getFileStatus(): void {
    this.batchService
      .getBatchUploadBasedOnType(this.fileTypeOf, this.year)
      .subscribe(
        (result: any) => {
          //   console.table([result]);
          this.statusList = result;
        },
        (error: any) => this.logger.error(error)
      );
  }
  getSelectedYear(event: any): void {
    this.year = event;
    this.getFileStatus();
  }
  showDialogImport(): void {
    this.display = true;
  }
  clearUpload(): void {
    this.tdsRateUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.fileLoading = false;
    this.uploadSuccess = false;
    this.uploadError = false;
  }
  uploadHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        this.tdsRateUploadForm.patchValue({
          file: this.files
        });
      } else {
        this.filesExists = false;
      }
      this.cd.markForCheck();
    }
  }

  uploadFiles(): void {
    const formData = new FormData();
    formData.append('file', this.tdsRateUploadForm.getRawValue().file[0]);
    this.fileLoading = true;
    this.filesExists = false;
    this.tdsrateService.uploadExcel(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.tdsRateUploadForm.patchValue({ files: [] });

        setTimeout(() => {
          this.display = false;
        }, 2000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.tdsRateUploadForm.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 2000);
      }
    );
    this.cd.markForCheck();
  }

  formatBytes(size: number): string {
    return UtilModule.formatBytes(size);
  }
  removeFiles(index: number): void {
    if (this.tdsRateUploadForm.getRawValue().files.length > 0) {
      this.tdsRateUploadForm.getRawValue().files.splice(index, 1);
    }
    if (this.tdsRateUploadForm.getRawValue().files.length == 0) {
      this.filesExists = false;
    }
    this.cd.markForCheck();
  }

  get rawValues(): any {
    return this.tdsRateUploadForm.getRawValue();
  }

  get filesLength(): number {
    if (this.tdsRateUploadForm.getRawValue().file) {
      return this.tdsRateUploadForm.getRawValue().file.length;
    }
    return 0;
  }
  getTdsData(): void {
    this.tdsrateService.getTds().subscribe(
      (result: Array<ITDSRate>) => {
        this.tdsRateList = result;
      },
      (error: any) => this.logger.error(error)
    );
  }
  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
