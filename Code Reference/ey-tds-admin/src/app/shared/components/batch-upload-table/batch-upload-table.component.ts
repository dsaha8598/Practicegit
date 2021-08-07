import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { SafeUrl, DomSanitizer } from '@angular/platform-browser';
import { UtilModule } from '@app/shared/utils/util';

@Component({
  selector: 'ey-batch-upload-table',
  templateUrl: './batch-upload-table.component.html',
  styleUrls: ['./batch-upload-table.component.scss']
})
export class BatchUploadTableComponent implements OnInit {
  selectedStatusColumns: Array<Object> = [];
  cols: Array<Object> = [];
  @Input('tableData') statusList: any = [];
  @Input('fileType') fileType: any;
  @Input('parmasYear') parmasYear: any;
  @Output() actionType = new EventEmitter();
  processFile: boolean = false;
  validateFile: boolean = false;
  viewSummary: boolean = false;
  token: any;
  pan: string;
  tanId: any;
  routerfileType: any;
  selectedYear: number;
  @Output() year = new EventEmitter();
  checkparamyear: any;
  data: any;
  constructor(
    private readonly authenticationService: AuthenticationService,
    private readonly sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.tanId = this.authenticationService.getTan();
    this.pan = this.authenticationService.getPAN();
    this.token = this.authenticationService.getBearerToken();
    this.data = {
      tan: this.tanId,
      pan: this.pan,
      fileType: this.fileType
    };

    if (this.fileType === 'DEDUCTOR_MASTER_EXCEL') {
      this.selectedStatusColumns = [
        {
          field: 'fileName',
          header: 'File name',
          width: '250px'
        },
        {
          field: 'fileStatus',
          header: 'Status',
          width: '150px'
        },
        {
          field: 'dateOfUpload',
          header: 'Uploaded date',
          width: '180px',
          type: 'date'
        },
        {
          field: 'processEndTime',
          header: 'Processed date',
          width: '180px',
          type: 'date'
        },
        {
          field: 'totalRecords',
          header: 'Total records',
          width: '150px'
        },
        {
          field: 'processedRecords',
          header: 'Processed records',
          width: '150px'
        },
        {
          field: 'duplicateRecords',
          header: 'Duplicate records',
          width: '150px'
        },
        {
          field: 'errorRecords',
          header: 'Error records',
          width: '150px'
        },
        {
          field: 'filePath',
          header: 'Uploaded file',
          width: '150px'
        }
      ];
    } else {
      this.selectedStatusColumns = [
        {
          field: 'fileName',
          header: 'File name',
          width: '250px'
        },
        {
          field: 'status',
          header: 'Status',
          width: '150px'
        },
        {
          field: 'createdDate',
          header: 'Uploaded date',
          width: '180px',
          type: 'date'
        },
        {
          field: 'processEndTime',
          header: 'Processed date',
          width: '180px',
          type: 'date'
        },
        {
          field: 'rowsCount',
          header: 'Total records',
          width: '150px'
        },
        {
          field: 'successCount',
          header: 'Processed records',
          width: '150px'
        },
        {
          field: 'duplicateCount',
          header: 'Duplicate records',
          width: '150px'
        },
        {
          field: 'failedCount',
          header: 'Error records',
          width: '150px'
        },
        {
          field: 'filePath',
          header: 'Uploaded file',
          width: '150px'
        }
      ];
    }

    this.cols = this.selectedStatusColumns;
    if (this.parmasYear) {
      this.checkparamyear = {};
      this.checkparamyear.selectedValue = this.parmasYear;
      this.yearTrigger(this.checkparamyear);
    } else {
      this.selectedYear = UtilModule.getCurrentFinancialYear();
    }
  }
  /*   addDownload(): void {
    this.selectedStatusColumns.push({
      field: 'uploadedFileDownloadUrl',
      header: 'Download File',
      width: '150px'
    });
  }
 */
  sanitize(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }

  validateHandler(id: any): void {
    this.actionType.emit(id);
  }

  yearTrigger(event: any): void {
    this.selectedYear = event.selectedValue;
    this.year.emit(this.selectedYear);
  }
}
