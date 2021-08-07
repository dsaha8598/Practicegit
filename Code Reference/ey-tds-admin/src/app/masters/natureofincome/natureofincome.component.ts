import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ITableConfig } from '@app/shared/model/common.model';
import { INatureOfPayment } from '@app/shared/model/natureOfPayment.model';
import { NatureofincomeService } from './natureofincome.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-natureofincome',
  templateUrl: './natureofincome.component.html',
  styleUrls: ['./natureofincome.component.scss']
})
export class NatureofincomeComponent implements OnInit {
  natureOfCollectionList: Array<INatureOfPayment>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;

  constructor(
    private readonly natureofcollectionService: NatureofincomeService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}
  ngOnInit(): void {
    this.natureOfCollectionList = [];
    this.cols = [
      { field: 'section', header: 'Section', width: '150px', type: 'initial' },
      { field: 'nature', header: 'Nature of income', width: '450px' },
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
        header: 'Sub-nature of income',
        type: 'longtext',
        width: '180px'
      },
      {
        field: 'subNaturePaymentMasters',
        header: 'Sub-nature of income values',
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
    this.natureofcollectionService.getNatureOfCollectionList().subscribe(
      (result: any) => {
        this.natureOfCollectionList = result;
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
