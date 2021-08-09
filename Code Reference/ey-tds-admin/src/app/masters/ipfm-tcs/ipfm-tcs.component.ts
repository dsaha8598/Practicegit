import { Component, OnInit } from '@angular/core';
import { IpfmTcsService } from './ipfm-tcs.service';
import { Router } from '@angular/router';
import { ITableConfig } from '@app/shared/model/common.model';
import { Iipfm } from '@app/shared/model/ipfm.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
@Component({
  selector: 'ey-ipfm-tcs',
  templateUrl: './ipfm-tcs.component.html',
  styleUrls: ['./ipfm-tcs.component.scss']
})
export class IpfmTcsComponent implements OnInit {
  ipfmTcsList: Iipfm[];
  scrollableCols: ITableConfig[];
  cols: ITableConfig[];
  selectedColumns: ITableConfig[];
  constructor(
    private readonly ipfmTcsService: IpfmTcsService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.getIpfmData();
    this.cols = [
      {
        field: 'interestType',
        header: 'Interest type',
        width: '150px',
        type: 'initial'
      },
      {
        field: 'typeOfIntrestCalculation',
        header: 'Type of interest calculation',
        width: '300px'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '180px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '180px',
        type: 'date'
      },
      {
        field: 'rate',
        header: 'Rate',
        width: '150px'
      },
      {
        field: 'finePerDay',
        header: 'Fine per day',
        width: '150px',
        type: 'amount'
      },
      {
        field: 'action',
        header: 'Action',
        width: '100px',
        type: 'action'
      },
      {
        field: 'remarks',
        header: 'Remarks',
        width: '200px'
      }
    ];
    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }
  getIpfmData(): void {
    this.ipfmTcsService.getIpfm().subscribe(
      (result: Iipfm[]) => {
        this.ipfmTcsList = result;
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
