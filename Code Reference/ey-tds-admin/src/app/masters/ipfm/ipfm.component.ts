import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { IpfmService } from './ipfm.service';
import { ActivatedRoute, Router } from '@angular/router';
//import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { ITableConfig } from '@app/shared/model/common.model';
import { Iipfm } from '@app/shared/model/ipfm.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, Validators, FormGroup } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-ipfm',
  templateUrl: './ipfm.component.html',
  styleUrls: ['./ipfm.component.scss']
})
export class IpfmComponent implements OnInit {
  ipfmList: Iipfm[];
  scrollableCols: ITableConfig[];
  cols: ITableConfig[];
  selectedColumns: ITableConfig[];
  ipfmUpload: any;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  fileTypeOf: string = 'TDS_FINE_RATE_MASTER_EXCEL';
  display: boolean;
  statusList: Array<IStatus>;
  year: number;
  tabIndex: number;
  constructor(
    private readonly ipfmService: IpfmService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}

  ngOnInit(): void {
    this.ipfmUpload = new FormGroup({
      files: new FormControl([], Validators.required)
    });
    this.year = UtilModule.getCurrentFinancialYear();
    this.tabIndex = 0;
    this.getIpfmData();
    this.cols = [
      {
        field: 'interestType',
        header: 'Interest type',
        width: '150px',
        type: 'initial'
      },
      {
        field: 'typeOfIntrestCalculation',
        header: 'Type of interest calculation',
        width: '300px'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '180px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '180px',
        type: 'date'
      },
      {
        field: 'rate',
        header: 'Rate',
        width: '150px'
      },
      {
        field: 'finePerDay',
        header: 'Fine per day',
        width: '150px'
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
  getIpfmData(): void {
    this.ipfmService.getIpfm().subscribe(
      (result: Iipfm[]) => {
        this.ipfmList = result;
      },
      (error: any) => this.logger.error(error)
    );
  }

  clearUpload(): void {
    this.ipfmUpload.patchValue({
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
      this.getIpfmData();
    } else {
      this.getFileStatus();
    }
  }

  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getIpfmData();
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
        this.ipfmUpload.patchValue({
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
    formData.append('file', this.ipfmUpload.getRawValue().files[0]);
    //console.log(formData);
    this.ipfmService.upload(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.ipfmUpload.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.ipfmUpload.patchValue({ files: [] });
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
    if (this.ipfmUpload.getRawValue().files) {
      return this.ipfmUpload.getRawValue().files.length;
    }
    return 0;
  }

  get rawValues(): any {
    return this.ipfmUpload.getRawValue();
  }

  removeFiles(index: number): void {
    if (this.ipfmUpload.getRawValue().files.length > 0) {
      this.ipfmUpload.getRawValue().files.splice(index, 1);
    }
    if (this.ipfmUpload.getRawValue().files.length == 0) {
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
