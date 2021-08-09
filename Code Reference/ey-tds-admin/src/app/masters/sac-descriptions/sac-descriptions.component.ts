import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { UtilModule } from '@app/shared';
import { SacDescriptionsService } from './sac-descriptions.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ITableConfig } from '@app/shared/model/common.model';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-sac-descriptions',
  templateUrl: './sac-descriptions.component.html',
  styleUrls: ['./sac-descriptions.component.scss']
})
export class SacDescriptionsComponent implements OnInit {
  sacDescList: any;
  selectedColumns: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  index: number;
  fileTypeOf: string = 'SAC_EXCEL';
  statusList: Array<IStatus>;
  year: number;
  display: boolean;
  sacUploadForm: FormGroup;
  filesExists: boolean;
  uploadFileDisplay = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  tabIndex: number;
  constructor(
    private readonly router: Router,
    private readonly cd: ChangeDetectorRef,
    private readonly sacService: SacDescriptionsService,
    private readonly logger: CustomLoggerService,
    private readonly batchService: BatchService
  ) {}

  ngOnInit() {
    this.year = UtilModule.getCurrentFinancialYear();
    this.index = 0;
    this.sacDescList = [];
    this.getData();
    this.sacUploadForm = new FormGroup({
      file: new FormControl('', Validators.required)
    });
    this.cols = [
      {
        field: 'headingAndGroup',
        header: 'Heading & Group ',
        width: '200px'
      },
      {
        field: 'serviceCode',
        header: 'Service code (Tariff)',
        width: '200px'
      },
      {
        field: 'serviceDescription',
        header: 'Service description',
        width: '200px'
      },
      {
        field: 'tdsSection',
        header: 'TDS section',
        width: '200px'
      },
      {
        field: 'natureOfPayment',
        header: 'Nature of payment',
        width: '200px'
      },
      {
        field: 'hufIndividal',
        header: 'For HUF and Individal service providers',
        width: '350px'
      },
      {
        field: 'otherThanHuf',
        header: 'For Other than HUF and Individuals service providers',
        width: '350px'
      },
      {
        field: 'directTaxTeam',
        header: 'Direct tax team comment',
        width: '200px'
      },
      {
        field: 'keywordsfromWorksheet',
        header: 'Keywords from this worksheet',
        width: '200px'
      },
      {
        field: 'additionalKeywordsfromWorksheet',
        header: 'Additional keywords from the Keywords worksheet',
        width: '350px'
      }
    ];
    this.selectedColumns = this.cols;
    this.tabIndex = 0;
  }

  getData(): void {
    this.sacService.getcoaCodesList().subscribe(
      (result: []) => {
        this.sacDescList = result;
      },
      (error: any) => {
        this.logger.error(error);
      }
    );
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
