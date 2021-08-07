import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ITableConfig } from '@app/shared/model/common.model';
import { IRateMasterAct } from '@app/shared/model/rateMaster.act.model';
import { RateMasterActService } from './ratemasteract.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { HttpHeaders } from '@angular/common/http';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { BatchService } from '@app/shared/services/batch/batch.service';
import { IStatus } from '@app/shared/model/status.model';
import { SafeUrl, DomSanitizer } from '@angular/platform-browser';
@Component({
  selector: 'ey-ratemasterAct',
  templateUrl: './ratemasteract.component.html',
  styleUrls: ['./ratemasteract.component.scss']
})
export class RateMasterActComponent implements OnInit {
  // rateMasterActList: Array<IRateMasterAct>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  rateMasterActList: Array<IRateMasterAct>;
  display: boolean;
  filesExists: boolean = false;
  uploadSuccess: boolean;
  uploadError: boolean;
  fileLoading: boolean;
  files: Array<any>;
  ratemasterUploadForm: FormGroup;
  fileTypeOf: string = 'DIVIDEND_RATE_ACT_EXCEL';
  statusList: Array<IStatus>;
  dividendActYear: number;
  tabIndex: number;
  constructor(
    private readonly rateMasterActService: RateMasterActService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly authenticationService: AuthenticationService,
    private readonly batchService: BatchService,
    private readonly cd: ChangeDetectorRef,
    private readonly sanitizer: DomSanitizer
  ) {}
  ngOnInit(): void {
    // this.rateMasterActList = [];
    this.dividendActYear = UtilModule.getCurrentFinancialYear();
    this.cols = [
      {
        field: 'tdsRate',
        header: 'TDS rate',
        width: '100px',
        type: 'initial'
      },
      {
        field: 'section',
        header: 'Section',
        width: '150px'
      },
      {
        field: 'dividendDeductorType',
        header: 'Deductor type',
        width: '300px'
      },
      {
        field: 'shareholderCategory',
        header: 'Category of shareholder',
        width: '300px'
      },
      {
        field: 'residentialStatus',
        header: 'Residential status',
        width: '300px'
      },
      {
        field: 'exemptionThresholdDisplay',
        header: 'Exemption threshhold (in Rs.)',
        width: '300px'
      }
    ];
    this.ratemasterUploadForm = new FormGroup({
      file: new FormControl(null, Validators.required)
    });
    this.getData();
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
    this.tabIndex = 0;
  }
  showDialog(): void {
    this.display = true;
  }
  getData(): void {
    this.rateMasterActList = [];
    this.rateMasterActService.getRateMasterActList().subscribe(
      (result: Array<any>) => {
        for (let i = 0; i <= result.length - 1; i++) {
          let obj: IRateMasterAct = result[i] as IRateMasterAct;
          obj.dividendDeductorType = result[i].dividendDeductorType
            .name as string;
          obj.shareholderCategory = result[i].shareholderCategory
            .name as string;
          if (result[i].residentialStatus === 'RES') {
            obj.residentialStatus = 'Resident';
          } else if (result[i].residentialStatus === 'NR') {
            obj.residentialStatus = 'Non-Resident';
          }
          if (result[i].exemptionThreshold == null) {
            obj.exemptionThresholdDisplay = 'NA';
          } else if (result[i].exemptionThreshold == 0) {
            obj.exemptionThresholdDisplay = 'NO LIMIT';
          } else {
            obj.exemptionThresholdDisplay = result[i].exemptionThreshold;
          }
          this.rateMasterActList.push(obj);
        }
        this.logger.debug(
          '********Act Result from handle local variable*****' +
            this.rateMasterActList
        );
      },
      (error: any) => this.logger.error(error)
    );
  }

  onTabChange(event: any): void {
    this.tabIndex = event.index;
    if (event.index === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }
  refreshData(): void {
    if (this.tabIndex === 0) {
      this.getData();
    } else {
      this.getStatus();
    }
  }

  getStatus(): void {
    this.batchService
      .getBatchUploadBasedOnType(this.fileTypeOf, this.dividendActYear)
      .subscribe(
        (result: any) => {
          console.table([result]);
          this.statusList = result;
        },
        (error: any) => this.logger.error(error)
      );
  }
  getSelectedYear(event: any): void {
    this.dividendActYear = event;
    this.getStatus();
  }
  uploadHandler(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.files = Array.from(event.target.files);
      if (this.files.length > 0) {
        this.filesExists = true;
        // this.shareholderUploadForm.controls.file = this.files[0];
        this.ratemasterUploadForm.patchValue({ file: this.files[0] });
      } else {
        this.filesExists = false;
      }

      this.cd.detectChanges();
    }
  }

  get f(): any {
    return this.ratemasterUploadForm.controls;
  }
  clearUpload(): void {
    this.ratemasterUploadForm.patchValue({
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
  get rawValues(): any {
    return this.ratemasterUploadForm.getRawValue();
  }
  uploadFiles(): void {
    //  this.resetPageState();
    const formData = new FormData();
    //formData.append('type', 'DIVIDEND_RATE_ACT_EXCEL');
    formData.append('file', this.ratemasterUploadForm.getRawValue().file);
    this.fileLoading = true;
    this.filesExists = false;
    this.rateMasterActService.uploadRateMasterAct(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.ratemasterUploadForm.patchValue({ file: null });
        setTimeout(() => {
          this.display = false;
          this.uploadSuccess = false;
          this.getStatus();
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.ratemasterUploadForm.patchValue({ file: null });
        setTimeout(() => {
          this.display = false;
          this.getStatus();
        }, 3000);
      }
    );
    this.cd.markForCheck();
  }
  removeFiles(): void {
    this.ratemasterUploadForm.patchValue({
      files: []
    });
    this.filesExists = false;
    this.cd.markForCheck();
  }

  sanitize(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }
  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
