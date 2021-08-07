import { Component, OnInit, Output } from '@angular/core';
import { ITableConfig } from '@app/shared/model/common.model';
import { Router, NavigationEnd } from '@angular/router';
import { Location } from '@angular/common';
import { DeductorGroupService } from './deductor-groupservice.service';

@Component({
  selector: 'ey-deductor-group',
  templateUrl: './deductor-group.component.html',
  styleUrls: ['./deductor-group.component.scss']
})
export class DeductorGroupComponent implements OnInit {
  selectedColumns: any = [];
  deductorGroupList: any = [];
  cols: any = [];
  previousUrl: any;

  constructor(
    private router: Router,
    private location: Location,
    private deductorGroupService: DeductorGroupService
  ) {}

  ngOnInit() {
    this.selectedColumns = [
      {
        field: 'tenantName',
        header: 'Tenant Name',
        type: 'initial',
        width: '200px'
      }
    ];
    this.cols = this.selectedColumns;
    this.getDeductorGroups();
  }

  getDeductorGroups() {
    this.deductorGroupService.getDeductorGroups().subscribe(
      (res: any) => {
        this.deductorGroupList = res.data;
      },
      error => {}
    );
  }
}
