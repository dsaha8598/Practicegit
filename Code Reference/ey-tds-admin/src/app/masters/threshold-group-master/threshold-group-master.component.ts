import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
//import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { ThresholdGroupMasterService } from './threshold-group-master.service';
import { ITHRESHOLD } from '@app/shared/model/thresholdgrp.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { UtilModule } from '@app/shared';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-threshold-group-master',
  templateUrl: './threshold-group-master.component.html',
  styleUrls: ['./threshold-group-master.component.scss']
})
export class ThresholdGroupMasterComponent implements OnInit {
  /* statusList: Array<IStatus>;
  fileTypeOf: string = 'NATURE_OF_PAYMENT_EXCEL';
  display: boolean;
   */ thresholdgroupList: Array<
    ITHRESHOLD
  >;
  scrollableCols: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  home: any;
  /* filesExists: boolean;
  uploadFileDisplay = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  tdsRateUploadForm: FormGroup;
   */ year: number;
  constructor(
    private readonly thresholdgrpmasterService: ThresholdGroupMasterService,
    private readonly router: Router,
    private readonly logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}
  ngOnInit(): void {
    this.getThresholdData();
    this.year = UtilModule.getCurrentFinancialYear();
    this.cols = [
      {
        field: 'groupName',
        header: 'Group name',
        width: '120px',
        type: 'initial'
      },
      {
        field: 'thresholdAmount',
        header: 'Threshold amount',
        width: '150px',
        type: 'amount'
      },
      {
        field: 'nature',
        header: 'Nature of payment',
        width: '500px'
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
        width: '200px',
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
  }
  /*  onTabChange(event: any): void {
    if (event.index === 0) {
      this.getTdsData();
    } else {
      this.getFileStatus();
    }
  }
 */
  /* getFileStatus(): void {
    this.batchService
      .getBatchUploadBasedOnType(this.fileTypeOf, this.year)
      .subscribe(
        (result: any) => {
          //   console.table([result]);
          this.statusList = result;
        },
        (error: any) => this.logger.error(error)
      );
  } */
  /* getSelectedYear(event: any): void {
    this.year = event;
    this.getFileStatus();
  } */
  /*  showDialogImport(): void {
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
 */
  /*   refreshData(): void {
    this.getFileStatus();
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
 */ getThresholdData(): void {
    this.thresholdgrpmasterService.getThresholdList().subscribe(
      (result: Array<ITHRESHOLD>) => {
        this.thresholdgroupList = result;
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
