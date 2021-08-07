import { Component, OnInit } from '@angular/core';
import { SurchargeService } from './surcharge.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-surcharge',
  templateUrl: './surcharge.component.html',
  styleUrls: ['./surcharge.component.scss']
})
export class SurchargeComponent implements OnInit {
  surchargeMasterlist: Array<any>;
  frozenCols: Array<any>;
  cols: Array<any>;
  home: any;
  scrollableCols: Array<any>;
  selectedColumns: Array<any>;

  constructor(
    private readonly surchargeService: SurchargeService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.getData();
    this.cols = [
      {
        field: 'surchargeApplicable',
        header: 'Is surcharge applicable',
        width: '300px',
        type: 'initial'
      },
      {
        field: 'surchargeRate',
        header: 'Rate',
        width: '200px',
        type: 'rateCondition'
      },
      {
        field: 'bocNatureOfPayment',
        header: 'Nature of payment',
        width: '200px'
      },
      {
        field: 'bocDeducteeStatus',
        header: 'Deductee status',
        width: '200px'
      },
      {
        field: 'bocDeducteeResidentialStatus',
        header: 'Deductee residential status',
        width: '250px'
      },
      {
        field: 'bocInvoiceSlab',
        header: 'Invoice slab',
        width: '200px'
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
    this.surchargeService.getSurchargeMasterList().subscribe(
      (result: any) => {
        this.surchargeMasterlist = result;
      },
      (error: any) => this.logger.error(error)
    );
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
