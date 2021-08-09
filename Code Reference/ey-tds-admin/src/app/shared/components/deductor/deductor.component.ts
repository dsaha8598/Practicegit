import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { DeductorService } from './deductor.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, Validators, FormGroup } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-deductor',
  templateUrl: './deductor.component.html',
  styleUrls: ['./deductor.component.scss']
})
export class DeductorComponent implements OnInit {
  deductorList: any[];
  frozenCols: any[];
  scrollableCols: Array<any>;
  items: any[] = [];
  home: any;
  cols: Array<any>;
  selectedColumns: Array<any>;
  columns: Array<any>;
  actionType: string;
  deductorformpopup: boolean;
  onboardingpopup: boolean;
  onboardingpopupTcs: boolean;
  onboardingpopup26AS: boolean;
  deductorPan: string;
  deductorUploadForm: any;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  fileTypeOf: string = 'DEDUCTOR_MASTER_EXCEL';
  display: boolean;
  statusList: Array<IStatus>;
  year: number;
  tabIndex: number;
  constructor(
    private router: Router,
    private deductorService: DeductorService,
    private logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}

  ngOnInit() {
    this.deductorUploadForm = new FormGroup({
      files: new FormControl([], Validators.required)
    });
    this.year = UtilModule.getCurrentFinancialYear();

    this.getDeductorData();
    this.cols = [
      {
        field: 'deductorName',
        header: 'Deductor / Collector name',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'deductorCode',
        header: 'Deductor / Collector code',
        width: '200px'
      },
      {
        field: 'dvndDeductorTypeName',
        header: 'Dividend Deductor Type',
        width: '300px'
      },
      {
        field: 'dueDateOfTaxPayment',
        header: 'Due date of tax payment',
        width: '200px',
        type: 'date'
      },
      {
        field: 'email',
        header: 'Email',
        width: '250px'
      },
      {
        field: 'phoneNumber',
        header: 'Phone number',
        width: '150px'
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
        colspan: '2',
        width: '350px',
        type: 'action'
      }
    ];
    this.selectedColumns = this.cols;
    this.home = { icon: 'pi pi-home' };
    this.scrollableCols = this.cols;
    this.tabIndex = 0;
  }

  getDeductorData(): void {
    this.deductorService.getDeductor().subscribe(
      (result: any[]) => {
        this.deductorList = result;
      },
      (error: any) => this.logger.error(error)
    );
  }

  closePopup(event: any): void {
    this.deductorformpopup = event;
    this.getDeductorData();
  }

  openDeductorForm(type: string, id?: string): void {
    this.deductorPan = id ? id : undefined;
    this.actionType = type;
    this.deductorformpopup = true;
  }

  closeOnboardingPopup(event: any): void {
    this.onboardingpopup = event;
  }

  closeOnboardingPopupTcs(event: any): void {
    this.onboardingpopupTcs = event;
  }

  closeOnboardingPopup26AS(event: any): void {
    this.onboardingpopup26AS = event;
  }

  openOnboardingForm(id: string, pan: string): void {
    this.deductorPan = pan ? pan : undefined;
    this.onboardingpopup = true;
  }

  openOnboardingForm26AS(id: string, pan: string): void {
    this.deductorPan = pan ? pan : undefined;
    this.onboardingpopup26AS = true;
  }

  openOnboardingFormTcs(id: string, pan: string): void {
    this.deductorPan = pan ? pan : undefined;
    this.onboardingpopupTcs = true;
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
  clearUpload(): void {
    this.deductorUploadForm.patchValue({
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
  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getDeductorData();
    } else {
      this.getFileStatus();
    }
  }
  onTabChange(event: any): void {
    this.tabIndex = event.index;
    if (event.index === 0) {
      this.getDeductorData();
    } else {
      this.getFileStatus();
    }
  }

  getFileStatus(): void {
    this.batchService
      .getBatchUploadBasedOnBatchType(this.fileTypeOf, this.year)
      .subscribe(
        (result: any) => {
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
        this.deductorUploadForm.patchValue({
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
    formData.append('file', this.deductorUploadForm.getRawValue().files[0]);
    //console.log(formData);
    this.deductorService.upload(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.deductorUploadForm.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.deductorUploadForm.patchValue({ files: [] });
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
    if (this.deductorUploadForm.getRawValue().files) {
      return this.deductorUploadForm.getRawValue().files.length;
    }
    return 0;
  }

  get rawValues(): any {
    return this.deductorUploadForm.getRawValue();
  }

  removeFiles(index: number): void {
    if (this.deductorUploadForm.getRawValue().files.length > 0) {
      this.deductorUploadForm.getRawValue().files.splice(index, 1);
    }
    if (this.deductorUploadForm.getRawValue().files.length == 0) {
      this.filesExists = false;
    }
    this.cd.markForCheck();
  }
}
