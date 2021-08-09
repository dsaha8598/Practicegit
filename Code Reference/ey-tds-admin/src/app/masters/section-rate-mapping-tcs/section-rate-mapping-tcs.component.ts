import { Location } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ITableConfig } from '@app/shared/model/common.model';
import { SectionRateMappingService } from './section-rate-mapping-tcs.service';

@Component({
  selector: 'ey-section-rate-mapping-tcs',
  templateUrl: './section-rate-mapping-tcs.component.html',
  styleUrls: ['./section-rate-mapping-tcs.component.scss']
})
export class SectionRateMappingTCSComponent implements OnInit {
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
        field: 'noiApplicableFrom',
        header: 'Nature of Income Applicable From',
        width: '200px',
        type: 'date'
      },
      {
        field: 'noiApplicableTo',
        header: 'Nature of Income Applicable To',
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
        field: 'natureOfIncomeId',
        header: 'Nature of income Id',
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
      .getSectionRateMaps('NATUREOFINCOME')
      .subscribe(
        (result: any) => {
          let results = result['redisData']['NATUREOFINCOME'];
          this.sectionRateMappingList = Object.values(results);
        },
        (error: any) => {}
      );
  }
}
