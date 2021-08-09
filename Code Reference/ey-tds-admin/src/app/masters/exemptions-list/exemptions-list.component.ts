import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';
import { ExemptionService } from './exemptions-list.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ITableConfig } from '@app/shared/model/common.model';

@Component({
  selector: 'ey-exemptions-list',
  templateUrl: './exemptions-list.component.html',
  styleUrls: ['./exemptions-list.component.scss']
})
export class ExemptionsListComponent implements OnInit {
  exemptionList: any;
  selectedColumns: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  index: number;
  fileTypeOf: string = 'EXEMPTION_LIST';
  statusYear: number;
  statusList: Array<IStatus>;
  display: boolean;
  exemptionUploadForm: FormGroup;
  filesExists: boolean;
  uploadFileDisplay = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  pageObj: any = [];
  tabIndex: number = 0;
  constructor(
    private readonly router: Router,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService,
    private readonly exemptionService: ExemptionService,
    private readonly logger: CustomLoggerService
  ) {}

  ngOnInit() {
    this.statusYear = UtilModule.getCurrentFinancialYear();
    this.index = 0;
    this.exemptionList = [];
    this.exemptionUploadForm = new FormGroup({
      file: new FormControl('', Validators.required)
    });
    this.cols = [
      {
        field: 'deductorType',
        header: 'Deductor Type',
        width: '100px'
      },
      {
        field: 'shareHolderCatagory',
        header: 'Category of shareholder',
        width: '350px'
      },
      {
        field: 'residentialStatus',
        header: 'Residential status',
        width: '100px',
        type: 'resStatus'
      },
      {
        field: 'section',
        header: 'Section',
        width: '100px'
      },
      {
        field: 'isExempted',
        header: 'Is exempted?',
        width: '100px',
        type: 'isExemp'
      }
    ];
    this.selectedColumns = this.cols;
    this.getData();
  }

  onTabChange(event: any): void {
    this.tabIndex = event.index;
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
    if (this.tabIndex === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }
  setPageObject(event: any): void {
    this.pageObj = event;
    this.getData();
  }

  getData(): void {
    this.exemptionService.getList(this.pageObj).subscribe(
      (result: []) => {
        this.exemptionList = result;
      },
      (error: any) => {
        this.logger.error(error);
      }
    );
  }

  showDialog(): void {
    this.display = true;
  }

  clearUpload(): void {
    this.exemptionUploadForm.patchValue({
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
        this.exemptionUploadForm.patchValue({
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
    formData.append('file', this.exemptionUploadForm.getRawValue().file[0]);
    formData.append('year', UtilModule.getCurrentFinancialYear().toString());
    this.fileLoading = true;
    this.filesExists = false;
    this.exemptionService.uploadExcel(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.exemptionUploadForm.reset();
        setTimeout(() => {
          this.display = false;
          this.getData();
        }, 2000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.exemptionUploadForm.reset();
        setTimeout(() => {
          this.display = false;
        }, 2000);
      }
    );
    this.cd.markForCheck();
  }

  removeFiles(): void {
    this.exemptionUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }

  get f(): any {
    return this.exemptionUploadForm.controls;
  }

  get rawValues(): any {
    return this.exemptionUploadForm.getRawValue();
  }

  get filesLength(): number {
    if (this.exemptionUploadForm.getRawValue().file) {
      return this.exemptionUploadForm.getRawValue().file.length;
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
