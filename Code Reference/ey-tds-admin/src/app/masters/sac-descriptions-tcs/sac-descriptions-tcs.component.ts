import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { UtilModule } from '@app/shared';
import { SacDescriptionsTcsService } from './sac-descriptions-tcs.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ITableConfig } from '@app/shared/model/common.model';

@Component({
  selector: 'ey-sac-descriptions-tcs',
  templateUrl: './sac-descriptions-tcs.component.html',
  styleUrls: ['./sac-descriptions-tcs.component.scss']
})
export class SacDescriptionsTcsComponent implements OnInit {
  sacDescList: any;
  selectedColumns: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  index: number;
  fileTypeOf: string = 'SAC_DESCRIPTIONS';
  statusYear: number;
  display: boolean;
  sacUploadForm: FormGroup;
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
    private readonly sacService: SacDescriptionsTcsService,
    private readonly logger: CustomLoggerService
  ) {}

  ngOnInit() {
    this.statusYear = UtilModule.getCurrentFinancialYear();
    this.index = 0;
    this.sacDescList = [];
    // this.getData();
    this.sacUploadForm = new FormGroup({
      file: new FormControl('', Validators.required)
    });
    this.cols = [
      {
        field: 'hsnSacCode',
        header: 'HSN/SAC code',
        width: '100px'
      },
      {
        field: 'desc',
        header: 'Description',
        width: '350px'
      },
      {
        field: 'tcsSection',
        header: 'TCS section',
        width: '100px'
      },
      {
        field: 'natureOfIncome',
        header: 'Nature of Income',
        width: '300px'
      }
    ];
    this.selectedColumns = this.cols;
  }

  setPageObject(event: any): void {
    this.pageObj = event;
    this.getData();
  }

  getData(): void {
    this.sacService.getcoaCodesList(this.pageObj).subscribe(
      (result: []) => {
        this.sacDescList = result;
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
    this.sacUploadForm.patchValue({
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
        this.sacUploadForm.patchValue({
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
    formData.append('file', this.sacUploadForm.getRawValue().file[0]);
    formData.append('year', UtilModule.getCurrentFinancialYear().toString());
    this.fileLoading = true;
    this.filesExists = false;
    this.sacService.uploadExcel(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.sacUploadForm.reset();
        setTimeout(() => {
          this.display = false;
          this.getData();
        }, 2000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.sacUploadForm.reset();
        setTimeout(() => {
          this.display = false;
        }, 2000);
      }
    );
    this.cd.markForCheck();
  }

  removeFiles(): void {
    this.sacUploadForm.patchValue({
      file: []
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }

  get f(): any {
    return this.sacUploadForm.controls;
  }

  get rawValues(): any {
    return this.sacUploadForm.getRawValue();
  }

  get filesLength(): number {
    if (this.sacUploadForm.getRawValue().file) {
      return this.sacUploadForm.getRawValue().file.length;
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
