import { Component, Input, OnInit, ViewChild, ElementRef } from '@angular/core';
import { StorageService } from '@app/shell/authentication/storageservice';
import { environment } from '@env/environment';

@Component({
  selector: 'ey-download-keywords',
  templateUrl: './download-keywords.component.html',
  styleUrls: ['./download-keywords.component.scss']
})
export class DownloadKeywordsComponent implements OnInit {
  @Input() tan: string;
  @Input() pan: string;
  @Input() tenantId: string;
  @Input() year: number;
  @Input() month: number;
  @Input() type: string;
  @Input() value: string;
  token: string;
  downloadURL: string;
  @ViewChild('myFormPost', { static: true }) myFormPost: ElementRef;
  constructor(private readonly storageService: StorageService) {}
  ngOnInit(): void {
    this.downloadURL = `${environment.api.ingestion}${this.type}/export`;
    this.token = this.storageService.getItem('token');
    // console.log(this.tan, this.pan, this.tenantId, this.year, this.month, this.token);
  }

  submitForm(): void {
    this.myFormPost.nativeElement.submit();
  }
}
