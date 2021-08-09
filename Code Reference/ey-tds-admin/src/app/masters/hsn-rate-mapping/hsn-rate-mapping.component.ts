import { Component, OnInit } from '@angular/core';
import { HSNRateMappingService } from './hsn-rate-mapping.service';

@Component({
  selector: 'ey-hsn-rate-mapping',
  templateUrl: './hsn-rate-mapping.component.html',
  styleUrls: ['./hsn-rate-mapping.component.scss']
})
export class HsnRateMappingComponent implements OnInit {
  scrollableCols: Array<object>;
  cols: Array<object>;
  selectedColumns: Array<object>;
  seachedData: any = [];
  searchKey: any;
  constructor(private hsnRateSearch: HSNRateMappingService) {}

  ngOnInit() {
    this.cols = [
      {
        field: 'hsnCode',
        header: 'HSN Code',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'nature',
        header: 'Nature',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'section',
        header: 'Section',
        width: '200px'
      },
      {
        field: 'rate',
        header: 'Rate',
        width: '200px'
      },
      {
        field: 'applicableFrom',
        header: 'Applicable From',
        width: '200px'
      },
      {
        field: 'applicableTo',
        header: 'Applicable To',
        width: '200px'
      }
    ];

    this.scrollableCols = this.cols;
    this.selectedColumns = this.cols;
  }

  getSearchData() {
    this.hsnRateSearch.getHsnSearch(this.searchKey).subscribe(
      (res: any) => {
        this.seachedData = res['data'];
      },
      error => {
        console.log(error);
      }
    );
  }
}
