import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import { UtilModule } from '@app/shared/utils/util';

@Component({
  selector: 'ey-year-dropdown',
  templateUrl: './year-dropdown.component.html',
  styleUrls: ['./year-dropdown.component.scss']
})
export class YearDropdownComponent implements OnInit {
  yearList: Array<{}>;
  selectedValue: number;
  @Input() id: string;
  currentYear: number;
  @Output() selectedYear: EventEmitter<any> = new EventEmitter();

  ngOnInit(): void {
    this.currentYear = UtilModule.getCurrentFinancialYear();
    this.selectedValue = this.currentYear;
    this.yearList = [];
    this.generateYears();
  }

  handleSelectChange(): void {
    this.selectedYear.emit({ selectedValue: this.selectedValue });
  }

  generateYears(): void {
    for (let index = 0; index < 20; index = index + 1) {
      const year = this.currentYear - 10 + index;
      const fYear = year - 1 + '-' + year;
      this.yearList.push({ label: fYear, value: year });
    }
  }
}
