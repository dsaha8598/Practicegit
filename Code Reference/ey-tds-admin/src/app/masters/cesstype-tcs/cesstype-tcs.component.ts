import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CesstypeTcsService } from './cesstype-tcs.service';
import { ICessType } from '@app/shared/model/cesstype.model';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

@Component({
  selector: 'ey-cesstype-tcs',
  templateUrl: './cesstype-tcs.component.html',
  styleUrls: ['./cesstype-tcs.component.scss']
  // providers: [MessageService]
})
export class CesstypeTcsComponent implements OnInit {
  cessTypeTcslist: Array<ICessType>;
  scrollableCols: Array<any>;
  cols: Array<any>;
  selectedColumns: Array<any>;
  constructor(
    private readonly cesslistService: CesstypeTcsService,
    private readonly router: Router,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.cols = [
      {
        field: 'cessType',
        header: 'Cess type',
        width: '150px',
        type: 'initial'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable from',
        width: '150px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable to',
        width: '150px',
        type: 'date'
      },
      { field: 'action', header: 'Action', width: '100px', type: 'action' }
    ];
    this.selectedColumns = this.cols;
    //this.selectedColumns = this.cols;
    this.getData();
  }

  getData(): void {
    this.cesslistService.getCessTypeList().subscribe(
      (result: Array<ICessType>) => {
        this.cessTypeTcslist = result;
      },
      (error: any) => {
        console.error(error);
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
