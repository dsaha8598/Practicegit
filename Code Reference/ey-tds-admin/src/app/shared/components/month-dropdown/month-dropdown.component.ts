import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'ey-month-dropdown',
  templateUrl: './month-dropdown.component.html',
  styleUrls: ['./month-dropdown.component.scss']
})
export class MonthDropdownComponent implements OnInit {
  monthList: Array<{}>;
  selectedValue: string;
  @Input() id: string;
  @Input() defaultMonth: number;

  @Output() selectedMonth: EventEmitter<any> = new EventEmitter();

  constructor() {}

  ngOnInit(): void {
    this.selectedValue = this.defaultMonth
      ? this.defaultMonth.toString()
      : (new Date().getMonth() + 1).toString();
    this.monthList = [
      {
        label: 'January',
        value: 1
      },
      {
        label: 'February',
        value: 2
      },
      {
        label: 'March',
        value: 3
      },
      {
        label: 'April',
        value: 4
      },
      {
        label: 'May',
        value: 5
      },
      {
        label: 'June',
        value: 6
      },
      {
        label: 'July',
        value: 7
      },
      {
        label: 'August',
        value: 8
      },
      {
        label: 'September',
        value: 9
      },
      {
        label: 'October',
        value: 10
      },
      {
        label: 'November',
        value: 11
      },
      {
        label: 'December',
        value: 12
      }
    ];
  }

  handleSelectChange(): void {
    this.selectedMonth.emit({ selectedValue: this.selectedValue });
  }
}
