import { Component, Input, OnInit, ViewChild, ElementRef } from '@angular/core';
import { StorageService } from '@app/shell/authentication/storageservice';
import { environment } from '@env/environment';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
//import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-download-link',
  templateUrl: './download-link.component.html',
  styleUrls: ['./download-link.component.scss']
})
export class DownloadLinkComponent implements OnInit {
  @Input() data: any;
  @Input() rowData: any;
  @Input() type: string;
  @Input() value: any;
  @Input() isDownload: boolean;
  @Input() module: string;
  token: string;
  tenantId: string;
  batchData: any;
  tan: any;
  filingData: any;
  challanData: any;
  cancelData: any;
  @ViewChild('myFormPost', { static: true }) myFormPost: ElementRef;
  downloadURL: string;
  constructor(
    private readonly storageService: StorageService,
    private readonly authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.tenantId = this.authenticationService.getTanName();
    if (this.checkType('batch') || this.checkType('batch-error')) {
      console.log(this.rowData.fileType);
      if (this.rowData.fileType === 'DEDUCTOR_MASTER_EXCEL') {
        this.downloadURL = `${environment.api.ingestion}deductor/batch/download`;
      } else {
        this.downloadURL = `${environment.api.masters}master/batch/download`;
      }
      if (this.data) {
        this.batchData = {};
        this.batchData.tan = 'No Tan';
        this.batchData.pan = 'No Pan';
        this.batchData.typeOfDownload = this.type;
        if (this.rowData.fileType === 'DEDUCTOR_MASTER_EXCEL') {
          this.batchData.uploadType = this.rowData.fileType;
          this.batchData.assessmentYear = this.rowData.year;
        } else {
          this.batchData.uploadType = this.rowData.uploadType;
          this.batchData.assessmentYear = this.rowData.assessmentYear;
        }
        this.batchData.tenantId = this.tenantId;
        this.batchData.batchId = this.rowData.id;
      }
    }
    this.token = this.storageService.getItem('token');
  }

  checkType(type: string): boolean {
    return this.module.toUpperCase() === type.toUpperCase();
  }

  submitForm(): void {
    this.myFormPost.nativeElement.submit();
  }
}
