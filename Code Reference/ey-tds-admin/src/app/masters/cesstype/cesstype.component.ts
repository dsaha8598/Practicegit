import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CesstypeService } from './cesstype.service';
import { ICessType } from '@app/shared/model/cesstype.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, Validators, FormGroup } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-cesstype',
  templateUrl: './cesstype.component.html',
  styleUrls: ['./cesstype.component.scss']
  // providers: [MessageService]
})
export class CesstypeComponent implements OnInit {
  cessTypelist: Array<ICessType>;
  scrollableCols: Array<any>;
  cols: Array<any>;
  selectedColumns: Array<any>;
  cessTypeUpload: any;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  fileTypeOf: string = 'CESS_TYPE_MASTER_EXCEL';
  display: boolean;
  statusList: Array<IStatus>;
  year: number;
  tabIndex: number;
  constructor(
    private readonly cesslistService: CesstypeService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}

  ngOnInit(): void {
    this.cessTypeUpload = new FormGroup({
      files: new FormControl([], Validators.required)
    });
    this.year = UtilModule.getCurrentFinancialYear();

    this.cols = [
      {
        field: 'cessType',
        header: 'Cess type',
        width: '300px',
        type: 'initial'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '300px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '300px',
        type: 'date'
      },
      { field: 'action', header: 'Action', width: '100px', type: 'action' }
    ];
    this.selectedColumns = this.cols;
    this.selectedColumns = this.cols;
    this.tabIndex = 0;
    this.getData();
  }

  getData(): void {
    this.cesslistService.getCessTypeList().subscribe(
      (result: Array<ICessType>) => {
        this.cessTypelist = result;
      },
      (error: any) => {
        console.error(error);
      }
    );
  }

  clearUpload(): void {
    this.cessTypeUpload.patchValue({
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
        this.cessTypeUpload.patchValue({
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
    formData.append('file', this.cessTypeUpload.getRawValue().files[0]);
    //console.log(formData);
    this.cesslistService.upload(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.cessTypeUpload.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.cessTypeUpload.patchValue({ files: [] });
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
    if (this.cessTypeUpload.getRawValue().files) {
      return this.cessTypeUpload.getRawValue().files.length;
    }
    return 0;
  }

  get rawValues(): any {
    return this.cessTypeUpload.getRawValue();
  }

  removeFiles(index: number): void {
    if (this.cessTypeUpload.getRawValue().files.length > 0) {
      this.cessTypeUpload.getRawValue().files.splice(index, 1);
    }
    if (this.cessTypeUpload.getRawValue().files.length == 0) {
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
