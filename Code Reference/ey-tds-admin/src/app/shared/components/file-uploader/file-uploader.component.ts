import {
  Component,
  OnInit,
  ChangeDetectorRef,
  Output,
  Input
} from '@angular/core';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'ey-file-uploader',
  templateUrl: './file-uploader.component.html',
  styleUrls: ['./file-uploader.component.scss']
})
export class FileUploaderComponent implements OnInit {
  @Output() file = new EventEmitter();
  @Input() id: string;
  fileName: string;
  constructor(private readonly cd: ChangeDetectorRef) {}

  ngOnInit() {}

  onFileEventChange(event: any): void {
    if (event.target.files && event.target.files.length) {
      this.fileName = event.target.files[0].name;
      this.file.emit(event.target.files[0]);
      this.cd.markForCheck();
    }
  }
}
