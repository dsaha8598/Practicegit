import { Component, OnInit } from '@angular/core';
import { SurchargeTcsService } from './surcharge-tcs.service';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-surcharge-tcs',
  templateUrl: './surcharge-tcs.component.html',
  styleUrls: ['./surcharge-tcs.component.scss']
})
export class SurchargeTcsComponent implements OnInit {
  surchargeTcsMasterlist: Array<any>;
  frozenCols: Array<any>;
  cols: Array<any>;
  home: any;
  scrollableCols: Array<any>;
  selectedColumns: Array<any>;

  constructor(
    private readonly surchargeTcsService: SurchargeTcsService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.getData();
    this.cols = [
      /*  {
        field: 'surchargeApplicable',
        header: 'Is surcharge applicable ?',
        width: '300px',
        type: 'initial'
      },
      */ {
        field: 'rate',
        header: 'Surcharge rate',
        width: '200px',
        type: 'rateCondition'
      },
      /*  {
        field: 'natureOfIncome',
        header: 'Nature of income',
        width: '350px'
      },
      */ {
        field: 'collecteeStatus',
        header: 'Collectee status',
        width: '200px'
      },
      {
        field: 'collecteeResidentialStatus',
        header: 'Collectee residential status',
        width: '250px',
        type: 'resStatusName'
      },
      {
        field: 'bocInvoiceSlab',
        header:
          'Invoice slab-Aggregate of amount collected subject to collection',
        width: '450px',
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

    this.home = { icon: 'pi pi-home' };
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }

  getData(): void {
    this.surchargeTcsService.getSurchargeMasterList().subscribe(
      (result: any) => {
        this.surchargeTcsMasterlist = result;
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
