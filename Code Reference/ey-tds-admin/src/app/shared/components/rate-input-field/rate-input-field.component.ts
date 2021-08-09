import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'ey-rate-input-field',
  templateUrl: './rate-input-field.component.html',
  styleUrls: ['./rate-input-field.component.scss']
})
export class RateInputFieldComponent implements OnInit {
  @Input() index: number;
  rate: number;
  // tslint:disable-next-line:prefer-output-readonly
  @Output() rateData = new EventEmitter<any>();
  constructor() {}

  ngOnInit() {}

  selectRate(rate: any): void {
    this.rateData.emit({ rate: rate, index: this.index });
  }
}
