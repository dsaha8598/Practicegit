import { Component, Input, OnInit, ViewChild, ElementRef } from '@angular/core';
import { StorageService } from '@app/shell/authentication/storageservice';
import { environment } from '@env/environment';

@Component({
  selector: 'ey-download-report',
  templateUrl: './download-report.component.html',
  styleUrls: ['./download-report.component.scss']
})
export class DownloadReportComponent implements OnInit {
  @Input() tan: string;
  @Input() pan: string;
  @Input() reportValueType: string;
  @Input() assessmentYear: any;
  token: string;
  downloadURL: string;
  @ViewChild('myFormPost', { static: true }) myFormPost: ElementRef;
  constructor(private readonly storageService: StorageService) {}

  ngOnInit() {
    this.downloadURL = `${environment.api.reports}report/by-type/excel`;
    this.token = this.storageService.getItem('token');
  }
  submitForm(): void {
    this.myFormPost.nativeElement.submit();
  }
}
