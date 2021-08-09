import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ITableConfig } from '@app/shared/model/common.model';
import { INatureOfPayment } from '@app/shared/model/natureOfPayment.model';
import { NatureofpaymentService } from './natureofpayment.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-natureofpayment',
  templateUrl: './natureofpayment.component.html',
  styleUrls: ['./natureofpayment.component.scss']
})
export class NatureofpaymentComponent implements OnInit {
  natureOfPaymentList: Array<INatureOfPayment>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;
  filesExists: boolean;
  fileLoading: boolean;
  uploadSuccess: boolean;
  uploadError: boolean;
  files: Array<any>;
  year: number;
  constructor(
    private readonly natureofpaymentService: NatureofpaymentService,
    private readonly router: Router,
    private logger: CustomLoggerService,
    private readonly cd: ChangeDetectorRef
  ) {}
  ngOnInit(): void {
    this.natureOfPaymentList = [];
    this.cols = [
      { field: 'section', header: 'Section', width: '150px', type: 'initial' },
      { field: 'nature', header: 'Nature of payment', width: '250px' },
      { field: 'displayValue', header: 'Display value', width: '150px' },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        type: 'date',
        width: '200px'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        type: 'date',
        width: '200px'
      },
      {
        field: 'isSubNaturePaymentApplies',
        header: 'Sub-nature of payment',
        type: 'longtext',
        width: '180px'
      },
      {
        field: 'subNaturePaymentMasters',
        header: 'Sub-nature of payment values',
        width: '200px',
        type: 'array'
      },
      { field: 'action', header: 'Action', type: 'action', width: '100px' }
    ];
    this.getData();
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }

  getData(): void {
    this.natureofpaymentService.getNatureOfPaymentList().subscribe(
      (result: any) => {
        this.natureOfPaymentList = result;
      },
      err => {
        this.logger.error(err);
      }
    );
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
