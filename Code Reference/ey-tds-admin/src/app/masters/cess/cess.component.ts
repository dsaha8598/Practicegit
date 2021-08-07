import { HttpClient } from '@angular/common/http';
import {
  Component,
  ComponentFactory,
  ComponentFactoryResolver,
  ComponentRef,
  OnInit,
  TemplateRef,
  ViewChild,
  ViewContainerRef
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TableHelpers } from '@app/shared/utils/tablehelpers';
import { CessService } from './cess.service';
import { ICess } from '@app/shared/model/cess.model';
import { ITableConfig } from '@app/shared/model/common.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

@Component({
  selector: 'ey-cess',
  templateUrl: './cess.component.html',
  styleUrls: ['./cess.component.scss']
})
export class CessComponent implements OnInit {
  cessMasterlist: Array<ICess>;
  cols: Array<ITableConfig>;
  scrollableCols: Array<ITableConfig>;
  selectedColumns: Array<ITableConfig>;

  constructor(
    private readonly cessService: CessService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.getData();
    this.cols = [
      {
        field: 'isCessApplicable',
        header: 'Is cessapplicable',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'rate',
        header: 'Rate',
        width: '200px'
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

    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }

  getData(): void {
    this.cessService.getCessMasterList().subscribe(
      (result: ICess[]) => {
        this.cessMasterlist = result;
      },
      (error: any) => this.logger.error(error)
    );
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => this.logger.error(error));
  }
}
