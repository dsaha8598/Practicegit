import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  ContentChild,
  TemplateRef
} from '@angular/core';
import { HSNRateMappingService } from '@app/masters/hsn-rate-mapping/hsn-rate-mapping.service';
import { PageObject } from './pagenation.model';
import { PagenationService } from './pagenation.service';

@Component({
  selector: 'ey-pagination',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss']
})
export class PaginationComponent implements OnInit, OnChanges {
  tableOptionColumns: any;
  pageObject: any;
  pageSizeList: any;
  invoiceAction: any;
  selectedIds: any;
  mismatchcategory: string;
  selectAll: boolean;
  selectedPageSize: any = 10;
  searchKey: any;
  @Input() toggleColumns: any;
  @Input() tableColumns: any;
  @Input() tableOptions: any;
  @Input() disablePagination: boolean;
  @Output() readonly pageObjectEmitter: EventEmitter<any> = new EventEmitter();
  @Output() readonly rowDataEmitter: EventEmitter<any> = new EventEmitter();
  @Input() private readonly pageObjectInput: any;
  @Input() private readonly type: string;
  @ContentChild('searchFilter', /* TODO: add static flag */ { static: true })
  searchFilterRef: TemplateRef<any>;
  @ContentChild(
    'searchFilterCode',
    /* TODO: add static flag */ { static: true }
  )
  searchFilterCodeRef: TemplateRef<any>;
  @ContentChild('monthYear', /* TODO: add static flag */ { static: true })
  monthYearRef: TemplateRef<any>;
  @ContentChild('tableActions', /* TODO: add static flag */ { static: true })
  tableActionsRef: TemplateRef<any>;
  constructor(
    private readonly pagenationService: PagenationService,
    private hsnService: HSNRateMappingService
  ) {
    this.selectedIds = [];
    this.invoiceAction = '0';
    this.selectAll = false;
    this.pageObject = new PageObject();
    this.toggleColumns = true;
    this.pageObject.count = 0;
    this.pageObject.data = [];
    this.pageObject.paginationStates = [];
    this.pageSizeList = [
      {
        label: '10',
        value: 10
      },
      {
        label: '20',
        value: 20
      },
      {
        label: '50',
        value: 50
      },
      {
        label: '100',
        value: 100
      }
    ];
  }

  emitRowData(data: any): void {
    this.rowDataEmitter.emit(data);
  }

  selectAllHandler(event: any): void {
    this.selectAll = event.isChecked;
    if (this.enableActionsBasedType('adjustments-view')) {
      this.rowDataEmitter.emit({ data: this.selectedIds });
    }
  }

  selectedValuesListner(event: any): void {
    const id = event.id;
    if (event.isChecked) {
      if (!this.selectedIds.includes(id)) {
        this.selectedIds.push(id);
      }
    } else {
      this.selectedIds = this.selectedIds.filter((ele: string) => ele !== id);
    }
    if (this.enableActionsBasedType('adjustments-view')) {
      this.rowDataEmitter.emit({ data: this.selectedIds });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    const newVal = changes['pageObjectInput'].currentValue;
    this.pageObject.pageNumber = newVal['resultsSet'].pageNumber;
    this.insertPageData(newVal);
  }

  ngOnInit(): void {
    this.tableOptionColumns = [];
    this.tableOptionColumns = this.tableColumns;
    this.generatePageObject();
  }

  enableActionsBasedType(type: string): boolean {
    return this.type === type;
  }

  generatePageObject(): void {
    this.pageObject.pageSize = parseInt(this.selectedPageSize);
    this.pageObject.data = [];
    const reqPageObject = this.requestData(
      'first',
      this.pageObject.pageSize,
      this.pageObject.pageNumber
    );
  }

  nextPage(): void {
    if (this.pageObject.count < 10) {
      this.pageObject.isNext = false;
    } else {
      this.pageObject.isNext = true;
    }
    this.pageObject.pageSize = this.pageObject.pageSize
      ? this.pageObject.pageSize
      : parseInt(this.selectedPageSize);
    this.selectAll = false;
    this.pageObject.pagenationState = 'next';
    this.pageObject.pageNumber = this.pageObject.pageNumber++;
    this.requestData(
      this.pageObject.pagenationState,
      this.pageObject.pageSize,
      this.pageObject.pageNumber
    );
  }

  prevPage(): void {
    this.selectAll = false;
    this.pageObject.pageSize = +this.selectedPageSize;
    this.pageObject.pagenationState = 'prev';
    this.requestData(
      this.pageObject.pagenationState,
      this.pageObject.pageSize,
      this.pageObject.pageNumber
    );
  }

  pageSizeChangeHandler(event: any): void {
    this.pageObject.pageSize = event ? parseInt(event.target.value) : 10;
    this.pageObject.pagenationState = 'first';
    this.requestData(
      this.pageObject.pagenationState,
      this.pageObject.pageSize,
      this.pageObject.pageNumber
    );
  }

  getSearchData() {
    if (this.searchKey.length != 0) {
      if (this.type === 'HSN/SAC-NOI mappings') {
        this.hsnService.getHsnSearch(this.searchKey).subscribe(
          (res: any) => {
            this.getTableOptions();
            this.pageObject.data = res['data'];
          },
          error => {
            console.log(error);
          }
        );
      } else {
        this.hsnService.getTdsHsnSearch(this.searchKey).subscribe(
          (res: any) => {
            this.getTableOptionsApplication();
            this.pageObject.data = res['data'];
          },
          error => {
            console.log(error);
          }
        );
      }
    }
    /*  } else {
      this.ngOnInit();
    } */
  }
  getTableOptionsApplication(): any {
    this.tableOptionColumns = [];
    this.tableOptionColumns = [
      {
        field: 'hsnCode',
        header: 'HSN Code',
        width: '200px'
      },
      {
        field: 'description',
        header: 'Description',
        width: '200px'
      },
      {
        field: 'tdsSection',
        header: 'Section',
        width: '200px'
      },
      {
        field: 'natureOfPayment',
        header: 'Nature of payment',
        width: '200px'
      }
    ];
  }
  getTableOptions(): any {
    this.tableOptionColumns = [];
    this.tableOptionColumns = [
      {
        field: 'hsnCode',
        header: 'HSN Code',
        width: '200px'
      },
      {
        field: 'nature',
        header: 'Nature',
        width: '200px'
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
        width: '200px',
        type: 'date'
      },
      {
        field: 'applicableTo',
        header: 'Applicable To',
        width: '200px',
        type: 'date'
      }
    ];
  }
  requestData(pageState: string, pageSize: number, pageNumber: number): any {
    const reqPageData = this.constructPageDataBasedOnType(
      pageState,
      pageSize,
      pageNumber
    );
    const pageObj = this.pagenationService.pageState(
      reqPageData.pagenationState,
      reqPageData.paginationStates,
      reqPageData.pageSize,
      reqPageData.pageNumber
    );
    this.pageObjectEmitter.emit(pageObj);
  }

  insertPageData(result: any): void {
    // TODO: handled for time being because in all scenerios page count wasn't implemented. Fix it later
    if (result && result.count >= 0) {
      this.pageObject.count = result.count;
      this.pageObject.data = result.resultsSet.data;
      this.prevNextStateHandler(result.resultsSet.pageStates);
    } else if (result && result.data) {
      this.pageObject.count = result.data.length;
      this.pageObject.paginationStates = result.pageStates;
      this.pageObject.data = result.data;
    }
  }

  prevNextStateHandler(pageStates: any): void {
    if (this.pageObject.pageNumber != 1) {
      this.pageObject.isPrev = false;
    } else {
      this.pageObject.isPrev = true;
    }
    this.pageObject.isNext = pageStates.includes('NO_MORE_RESULTS');
  }

  constructPageDataBasedOnType(
    pageState: any,
    pageSize: number,
    pageNumber: number
  ): any {
    this.pageObject.pagenationState = pageState;
    this.pageObject.pageSize = pageSize;
    this.pageObject.pageNumber = pageNumber;

    return this.pageObject;
  }

  panStatusCheck(status: any) {
    if (this.type === 'collectee') {
      return status == true ? false : false;
    } else {
      return status ? status.toUpperCase() === 'INVALID' : false;
    }
  }
}
