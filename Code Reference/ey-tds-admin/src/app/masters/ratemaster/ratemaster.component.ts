import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RatemasterService } from './ratemaster.service';
import { ITDSRate } from '@app/shared/model/tds.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

@Component({
  selector: 'ey-ratemaster',
  templateUrl: './ratemaster.component.html',
  styleUrls: ['./ratemaster.component.scss']
})
export class RatemasterComponent implements OnInit {
  ratemasterList: Array<ITDSRate>;
  scrollableCols: Array<ITableConfig>;
  cols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  home: any;
  constructor(
    private readonly ratemasterService: RatemasterService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}
  ngOnInit(): void {
    this.getTdsData();
    this.cols = [
      {
        field: 'rate',
        header: 'Rate',
        width: '100px',
        type: 'initial'
      },
      {
        field: 'rateForNoPan',
        header: 'Rate No PAN/AADHAR',
        width: '150px'
      },
      {
        field: 'noItrRate',
        header: 'No ITR Rate',
        width: '150px'
      },
      {
        field: 'noPanRateAndNoItrRate',
        header: 'No Pan Rate & No ITR Rate',
        width: '250px'
      },
      {
        field: 'natureOfIncomeMaster',
        header: 'Nature of income',
        width: '300px'
      },
      {
        field: 'isPerTransactionLimitApplicable',
        header: 'Is per transaction limit applicable ?',
        width: '220px'
      },
      {
        field: 'isAnnualTransactionLimitApplicable',
        header: 'Is annual transaction limit applicable',
        width: '250px'
      },
      {
        field: 'annualTransactionLimit',
        header: 'Annual transaction limit',
        width: '200px',
        type: 'amount'
      },

      {
        field: 'perTransactionLimit',
        header: 'Per transaction limit/collection threshold(INR)',
        width: '270px',
        type: 'amount'
      } /* ,
      {
        field: 'statusName',
        header: 'Collectee status',
        width: '200px'
      },
      {
        field: 'residentialStatusName',
        header: 'Collectee residential status',
        width: '250px',
        type: 'resStatusName'
      } */,
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '200px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '250px',
        type: 'date'
      },
      {
        field: 'action',
        header: 'Action',
        width: '100px',
        type: 'action'
      }
    ];
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }
  getTdsData(): void {
    this.ratemasterService.getTds().subscribe(
      (result: Array<ITDSRate>) => {
        this.ratemasterList = result;
      },
      (error: any) => this.logger.error(error)
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
