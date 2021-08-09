import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { Router } from '@angular/router';
import { MonthTrackerService } from './month-tracker.service';
import { ITableConfig } from '@app/shared/model/common.model';
import { FormControl, Validators, FormGroup } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-month-tracker',
  templateUrl: './month-tracker.component.html',
  styleUrls: ['./month-tracker.component.scss']
})
export class MonthTrackerComponent implements OnInit {
  monthlyTrackerList: Array<any>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  monthTrackerUpload: any;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  fileTypeOf: string = 'TDS_MONTHLY_TRACKER_EXCEL';
  display: boolean;
  statusList: Array<IStatus>;
  year: number;
  tabIndex: number;
  constructor(
    private readonly monthlyTrackerService: MonthTrackerService,
    private readonly router: Router,
    private readonly logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}
  ngOnInit(): void {
    this.monthTrackerUpload = new FormGroup({
      files: new FormControl([], Validators.required)
    });
    this.year = UtilModule.getCurrentFinancialYear();
    this.monthlyTrackerList = [];
    this.cols = [
      { field: 'monthName', header: 'Month', width: '150px', type: 'initial' },
      { field: 'year', header: 'Year', width: '150px' },
      {
        field: 'dueDateForChallanPayment',
        header: 'Due date for challan payment',
        width: '250px',
        type: 'date'
      },
      {
        field: 'monthClosureForProcessing',
        header: 'Month closure for processing',
        width: '250px',
        type: 'date'
      },
      {
        field: 'dueDateForFiling',
        header: 'Due date for filing',
        width: '250px',
        type: 'date'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '250px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '250px',
        type: 'date'
      },
      { field: 'action', header: 'Action', type: 'action', width: '100px' }
    ];
    this.getData();
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
    this.tabIndex = 0;
  }
  getData(): void {
    this.monthlyTrackerService.getMonthlyTrackerList().subscribe(
      (result: any) => {
        this.monthlyTrackerList = result;
      },
      err => {
        this.logger.error(err);
      }
    );
  }
  clearUpload(): void {
    this.monthTrackerUpload.patchValue({
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

  onTabChange(event: any): void {
    this.tabIndex = event.index;
    if (event.index === 0) {
      this.getData();
    } else {
      this.getFileStatus();
    }
  }
  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getData();
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
  uploadHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        this.monthTrackerUpload.patchValue({
          files: this.files
        });
      } else {
        this.filesExists = false;
      }
      this.cd.markForCheck();
    }
  }

  uploadFiles(): void {
    const formData = new FormData();
    this.fileLoading = true;
    this.filesExists = false;
    formData.append('file', this.monthTrackerUpload.getRawValue().files[0]);
    //console.log(formData);
    this.monthlyTrackerService.upload(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.monthTrackerUpload.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.monthTrackerUpload.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      }
    );
  }

  showDialog(event: any): void {
    this.display = true;
  }

  get filesLength(): number {
    if (this.monthTrackerUpload.getRawValue().files) {
      return this.monthTrackerUpload.getRawValue().files.length;
    }
    return 0;
  }

  get rawValues(): any {
    return this.monthTrackerUpload.getRawValue();
  }

  removeFiles(index: number): void {
    if (this.monthTrackerUpload.getRawValue().files.length > 0) {
      this.monthTrackerUpload.getRawValue().files.splice(index, 1);
    }
    if (this.monthTrackerUpload.getRawValue().files.length == 0) {
      this.filesExists = false;
    }
    this.cd.markForCheck();
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
