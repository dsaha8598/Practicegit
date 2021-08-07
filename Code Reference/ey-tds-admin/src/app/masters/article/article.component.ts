import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ArticleService } from './article.service';
import { Router } from '@angular/router';
//import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { IArticle } from '@app/shared/model/article.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { FormControl, Validators, FormGroup } from '@angular/forms';
import { UtilModule } from '@app/shared';
import { IStatus } from '@app/shared/model/status.model';
import { BatchService } from '@app/shared/services/batch/batch.service';

@Component({
  selector: 'ey-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss']
})
export class ArticleComponent implements OnInit {
  articleList: IArticle[];
  cols: ITableConfig[];
  selectedColumns: ITableConfig[];
  scrollableCols: ITableConfig[];
  articleUploadForm: any;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  fileTypeOf: string = 'ARTICLE_MASTER_EXCEL';
  display: boolean;
  statusList: Array<IStatus>;
  year: number;
  tabIndex: number;
  constructor(
    private readonly articleService: ArticleService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef,
    private readonly batchService: BatchService
  ) {}

  ngOnInit(): void {
    this.articleUploadForm = new FormGroup({
      files: new FormControl([], Validators.required)
    });
    this.year = UtilModule.getCurrentFinancialYear();
    this.getData();
    this.cols = [
      {
        field: 'articleName',
        header: 'Article name',
        width: '150px',
        type: 'initial'
      },
      { field: 'articleNumber', header: 'Article number', width: '180px' },
      { field: 'country', header: 'Country', width: '150px' },
      { field: 'articleRate', header: 'Rate', width: '150px' },
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
      { field: 'action', header: 'Action', width: '100px', type: 'action' }
    ];
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
    this.tabIndex = 0;
  }

  getData(): void {
    this.articleService.getArticlelist().subscribe(
      (result: IArticle[]) => {
        this.articleList = [];
        this.articleList = result;
        this.logger.debug(this.articleList);
      },
      (error: any) => this.logger.error(error)
    );
  }
  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => this.logger.error(error));
  }

  clearUpload(): void {
    this.articleUploadForm.patchValue({
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
        this.articleUploadForm.patchValue({
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
    formData.append('file', this.articleUploadForm.getRawValue().files[0]);
    //console.log(formData);
    this.articleService.upload(formData).subscribe(
      (result: any) => {
        this.fileLoading = false;
        this.uploadSuccess = true;
        this.articleUploadForm.patchValue({ files: [] });
        setTimeout(() => {
          this.display = false;
        }, 3000);
      },
      (error: any) => {
        this.fileLoading = false;
        this.uploadError = true;
        this.articleUploadForm.patchValue({ files: [] });
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
    if (this.articleUploadForm.getRawValue().files) {
      return this.articleUploadForm.getRawValue().files.length;
    }
    return 0;
  }

  get rawValues(): any {
    return this.articleUploadForm.getRawValue();
  }

  removeFiles(index: number): void {
    if (this.articleUploadForm.getRawValue().files.length > 0) {
      this.articleUploadForm.getRawValue().files.splice(index, 1);
    }
    if (this.articleUploadForm.getRawValue().files.length == 0) {
      this.filesExists = false;
    }
    this.cd.markForCheck();
  }
}
