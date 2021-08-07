import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ITableConfig } from '@app/shared/model/common.model';
import { SectionRateMappingService } from './section-rate-mapping.service';

@Component({
  selector: 'ey-section-rate-mapping',
  templateUrl: './section-rate-mapping.component.html',
  styleUrls: ['./section-rate-mapping.component.scss']
})
export class SectionRateMappingComponent implements OnInit {
  sectionRateMappingList: any = [];
  columns: any = [];
  selectedColumns: Array<ITableConfig>;
  scrollableCols: any = [];
  cols: Array<ITableConfig>;
  constructor(
    private location: Location,
    private sectionRateMappingService: SectionRateMappingService
  ) {}

  ngOnInit() {
    this.getData();
    this.selectedColumns = [
      { field: 'nature', header: 'Nature', width: '350px' },
      { field: 'section', header: 'Section', width: '100px' },
      {
        field: 'perTransactionLimitApplicable',
        header: 'Per Transaction Limit Applicable',
        width: '200px'
      },
      {
        field: 'noPanRate',
        header: 'No Pan Rate',
        width: '100px'
      },
      {
        field: 'rate',
        header: 'Rate',
        width: '100px'
      },
      {
        field: 'perTransactionLimit',
        header: 'Per Transaction Limit',
        width: '100px'
      },
      {
        field: 'annualTransaction',
        header: 'Annual Transaction',
        width: '100px'
      },
      {
        field: 'nopApplicableFrom',
        header: 'Nature of Payment Applicable From',
        width: '200px',
        type: 'date'
      },
      {
        field: 'nopApplicableTo',
        header: 'Nature of Payment Applicable To',
        width: '200px',
        type: 'date'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '200px',
        type: 'date'
      },
      {
        field: 'tcsMasterId',
        header: 'TCS Master Id',
        width: '100px'
      },
      {
        field: 'natureOfPaymentId',
        header: 'Nature of Payment Id',
        width: '100px'
      }
    ];
    this.cols = this.selectedColumns;
  }

  backClick() {
    this.location.back();
  }

  getData(): void {
    this.sectionRateMappingService
      .getSectionRateMaps('NATUREOFPAYMENT')
      .subscribe(
        (result: any) => {
          let results = result['redisData']['NATUREOFPAYMENT'];
          this.sectionRateMappingList = Object.values(results);
        },
        (error: any) => {}
      );
  }
}
