import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CessTcsService } from './cess-tcs.service';
import { ICess } from '@app/shared/model/cess.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

@Component({
  selector: 'ey-cess-tcs',
  templateUrl: './cess-tcs.component.html',
  styleUrls: ['./cess-tcs.component.scss']
})
export class CessTcsComponent implements OnInit {
  cessTcsMasterlist: Array<ICess>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;

  constructor(
    private readonly cessService: CessTcsService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.getData();
    this.cols = [
      /*    {
        field: 'isCessApplicable',
        header: 'Is cess applicable?',
        width: '200px',
        type: 'initial'
      },
    */ {
        field: 'rate',
        header: 'Rate',
        width: '100px',
        type: 'rateCondition'
      },
      /* 
      {
        field: 'natureOfIncome',
        header: 'Nature of income',
        width: '450px'
      }, */
      {
        field: 'collecteeStatus',
        header: 'Collectee status',
        width: '200px'
      },
      {
        field: 'collecteeResidentialStatus',
        header: 'Collectee residential status',
        width: '200px',
        type: 'resStatusName'
      },
      {
        field: 'amount',
        header:
          'Invoice slab  - Aggregate of amount collected / subject to collection',
        width: '400px',
        type: 'amount'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '200px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '200px',
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

  getData(): void {
    this.cessService.getCessMasterList().subscribe(
      (result: ICess[]) => {
        this.cessTcsMasterlist = result;
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
      .catch(error => this.logger.error(error));
  }
}
