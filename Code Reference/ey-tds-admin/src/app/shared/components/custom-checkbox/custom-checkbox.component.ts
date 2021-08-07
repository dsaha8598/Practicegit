import {
  Component,
  OnInit,
  Output,
  Input,
  EventEmitter,
  SimpleChanges,
  OnChanges
} from '@angular/core';

@Component({
  selector: 'ey-custom-checkbox',
  templateUrl: './custom-checkbox.component.html',
  styleUrls: ['./custom-checkbox.component.scss']
})
export class CustomCheckboxComponent {
  @Input() id: string;
  @Input() set checked(value: boolean) {
    this.isChecked = value;
    this.valueEmitter.emit({ isChecked: this.isChecked, id: this.id });
  }
  isChecked: boolean;
  @Output() readonly valueEmitter: EventEmitter<any> = new EventEmitter();

  emitValue(event: any): void {
    this.isChecked = event.target.checked;
    this.valueEmitter.emit({ isChecked: this.isChecked, id: this.id });
  }
}
