import { Component, Input, OnInit, ViewChild, ElementRef } from '@angular/core';
import { StorageService } from '@app/shell/authentication/storageservice';
import { environment } from '@env/environment';

@Component({
  selector: 'ey-download-nop',
  templateUrl: './download-nop.component.html',
  styleUrls: ['./download-nop.component.scss']
})
export class DownloadNOPComponent implements OnInit {
  token: string;
  downloadURL: string;
  @ViewChild('myFormPost', { static: true }) myFormPost: ElementRef;
  constructor(private readonly storageService: StorageService) {}
  ngOnInit(): void {
    this.downloadURL = `${environment.api.masters}tds/hsn/nops/export`;
    this.token = this.storageService.getItem('token');
  }

  submitForm(): void {
    this.myFormPost.nativeElement.submit();
  }
}
