import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';
import { HSNApplicationMastersService } from './hsn-application-masters.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ITableConfig } from '@app/shared/model/common.model';

@Component({
  selector: 'ey-hsn-application-masters',
  templateUrl: './hsn-application-masters.component.html',
  styleUrls: ['./hsn-application-masters.component.scss']
})
export class HSNApplicationMastersComponent implements OnInit {
  hsnDescList: any;
  selectedColumns: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  index: number;
  fileTypeOf: string = 'TDS_HSN_CODE_EXCEL';
  statusYear: number;
  statusList: Array<IStatus>;
  display: boolean;
  hsnUploadForm: FormGroup;
  filesExists: boolean;
  uploadFileDisplay = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  pageObj: any = [];
  constructor(
    private readonly router: Router,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService,
    private readonly hsnService: HSNApplicationMastersService,
    private readonly logger: CustomLoggerService
  ) {}

  ngOnInit() {
    this.statusYear = UtilModule.getCurrentFinancialYear();
    this.index = 0;
    this.hsnDescList = [];
    // this.getData();
    this.hsnUploadForm = new FormGroup({
      file: new FormControl('', Validators.required)
    });
    this.cols = [
      {
        field: 'hsnCode',
        header: 'HSN/SAC code',
        width: '100px',
        type: 'initial'
      },
      {
        field: 'description',
        header: 'Description',
        width: '350px'
      },
      {
        field: 'tdsSection',
        header: 'TDS section',
        width: '100px'
      },
      {
        field: 'natureOfPayment',
        header: 'Nature of payment',
        width: '300px'
      },
      {
        field: 'action',
        header: 'Action',
        width: '100px',
        type: 'action'
      }
    ];
    this.selectedColumns = this.cols;
  }

  onTabChange(event: any): void {
    if (event.index === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }

  getSelectedYear(event: any): void {
    this.statusYear = event;
    this.getStatus();
  }

  getStatus(): void {
    this.batchService
      .getBatchUploadBasedOnType(this.fileTypeOf, this.statusYear)
      .subscribe(
        (result: any) => {
          //   console.table([result]);
          this.statusList = result;
        },
        (error: any) => this.logger.error(error)
      );
  }

  refreshData(): void {
    this.getStatus();
  }
  setPageObject(event: any): void {
    this.pageObj = event;
    this.getData();
  }

  getData(): void {
    this.hsnService.getcoaCodesList(this.pageObj).subscribe(
      (result: []) => {
        this.hsnDescList = result;
      },
      (error: any) => {
        this.logger.error(error);
      }
    );
  }

  showDialogImport(): void {
    this.display = true;
  }

  showDialog(): void {
    this.display = true;
  }

  clearUpload(): void {
    this.hsnUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.fileLoading = false;
    this.uploadSuccess = false;
    this.uploadError = false;
  }

  formatBytes(size: number): string {
    return UtilModule.formatBytes(size);
  }

  uploadHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        this.hsnUploadForm.patchValue({
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
    formData.append('type', this.fileTypeOf);
    formData.append('file', this.hsnUploadForm.getRawValue().file[0]);
    formData.append('year', UtilModule.getCurrentFinancialYear().toString());
    this.fileLoading = true;
    this.filesExists = false;
    this.hsnService.uploadExcel(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.hsnUploadForm.reset();
        setTimeout(() => {
          this.display = false;
          this.getData();
        }, 2000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.hsnUploadForm.reset();
        setTimeout(() => {
          this.display = false;
        }, 2000);
      }
    );
    this.cd.markForCheck();
  }

  removeFiles(): void {
    this.hsnUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }

  get f(): any {
    return this.hsnUploadForm.controls;
  }

  get rawValues(): any {
    return this.hsnUploadForm.getRawValue();
  }

  get filesLength(): number {
    if (this.hsnUploadForm.getRawValue().file) {
      return this.hsnUploadForm.getRawValue().file.length;
    }
    return 0;
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => this.logger.error(error));
  }
}
