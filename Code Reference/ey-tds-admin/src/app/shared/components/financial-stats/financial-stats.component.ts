import { Component, OnInit, Input } from '@angular/core';
import { UtilModule } from '@app/shared/utils/util';

@Component({
  selector: 'ey-financial-stats',
  templateUrl: './financial-stats.component.html',
  styleUrls: ['./financial-stats.component.scss']
})
export class FinancialStatsComponent implements OnInit {
  @Input() isQuarterRequired: boolean;
  @Input() isMonthRequired: boolean;
  statisticsString: string;
  ngOnInit(): void {
    this.isMonthRequired = true;
    const date = new Date();
    // console.log(date.getMonth());
    this.statisticsString = this.stringGenerator(
      //    date.getMonth(),
      UtilModule.getCurrentFinancialYear()
    );

    //console.log(this.statisticsString);
  }

  stringGenerator(year: number): string {
    let finalString: string;

    finalString = `Financial Year: ${year -
      1} -  ${year} | Assessment Year: ${year} -  ${year + 1}`;
    /* if (this.isMonthRequired) {
      finalString += ` | Month: ${UtilModule.getCurrentMonth(month)}`;
    }*/
    return finalString;
  }

  checkAndReturnQuarter(month: number): string {
    if (month >= 3 && month < 6) {
      return 'April to June (Q1)';
    } else if (month >= 6 && month < 9) {
      return 'July to September (Q2)';
    } else if (month >= 9 && month < 12) {
      return 'October to December (Q3)';
    } else {
      return 'January to March (Q4)';
    }
  }
}
