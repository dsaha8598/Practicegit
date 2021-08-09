import { Component, OnInit, Input } from '@angular/core';
import { LoaderService } from '@app/shared/loader/loader.service';

@Component({
  selector: 'ey-download-template',
  templateUrl: './download-template.component.html',
  styleUrls: ['./download-template.component.scss']
})
export class DownloadTemplateComponent implements OnInit {
  @Input() templateType: string;
  @Input() templateName: string;
  downloadUrl: string;
  constructor(private readonly loaderService: LoaderService) {}
  ngOnInit(): void {
    this.downloadUrl = `${window.origin}/assets/templates/${this.templateType}_upload_template.xlsx`;
  }

  downloadTemplate(): void {
    this.loaderService.show();
    setTimeout(() => {
      if (this.templateType !== null) {
        window.open(this.downloadUrl);
      }
      this.loaderService.hide();
    }, 1000);
  }
}
