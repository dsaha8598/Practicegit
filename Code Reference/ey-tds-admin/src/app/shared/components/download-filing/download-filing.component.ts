import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { environment } from '@env/environment';
import { StorageService } from '@app/shell/authentication/storageservice';

@Component({
  selector: 'ey-download-filing',
  templateUrl: './download-filing.component.html',
  styleUrls: ['./download-filing.component.scss']
})
export class DownloadFilingComponent implements OnInit {
  @Input() url: any;
  @Input() token: any;
  downloadURL: any;
  @ViewChild('myFormPost', { static: true }) myFormPost: ElementRef;
  challanData: any;

  constructor(private readonly storageService: StorageService) {}

  ngOnInit() {
    this.downloadURL = `${environment.api.ingestion}blob/download`;
    this.challanData = {};
    this.challanData.url = this.url;
    this.token = this.storageService.getItem('token');
  }

  submitForm(): void {
    this.myFormPost.nativeElement.submit();
  }
}
