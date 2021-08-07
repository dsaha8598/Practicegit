import { Component, OnInit } from '@angular/core';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { Router } from '@angular/router';
import { MonthTrackerTcsService } from './month-tracker-tcs.service';
import { ITableConfig } from '@app/shared/model/common.model';

@Component({
  selector: 'ey-month-tracker-tcs',
  templateUrl: './month-tracker-tcs.component.html',
  styleUrls: ['./month-tracker-tcs.component.scss']
})
export class MonthTrackerTcsComponent implements OnInit {
  monthlyTrackerTcsList: Array<any>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;

  constructor(
    private readonly monthlyTrackerService: MonthTrackerTcsService,
    private readonly router: Router,
    private readonly logger: CustomLoggerService
  ) {}
  ngOnInit(): void {
    this.monthlyTrackerTcsList = [];
    this.cols = [
      { field: 'monthName', header: 'Month', width: '150px', type: 'initial' },
      { field: 'year', header: 'Year', width: '150px' },
      {
        field: 'dueDateForChallanPayment',
        header: 'Due date for challan payment',
        width: '250px',
        type: 'date'
      },
      {
        field: 'monthClosureForProcessing',
        header: 'Month closure for processing',
        width: '250px',
        type: 'date'
      },
      {
        field: 'dueDateForFiling',
        header: 'Due date for filing',
        width: '250px',
        type: 'date'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '250px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '250px',
        type: 'date'
      },
      { field: 'action', header: 'Action', type: 'action', width: '100px' }
    ];
    this.getData();
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }
  getData(): void {
    this.monthlyTrackerService.getMonthlyTrackerList().subscribe(
      (result: any) => {
        this.monthlyTrackerTcsList = result;
      },
      err => {
        this.logger.error(err);
      }
    );
  }
  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'], {
        queryParams: {
          selectedModule: 'TCS'
        }
      })
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
