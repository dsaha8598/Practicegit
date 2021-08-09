import { Component, OnInit } from '@angular/core';
import { AdminService } from './admin.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
//import { computeStyle } from '@angular/animations/browser/src/util';
import { MenuItem } from 'primeng/api';
import { AlertService } from '@app/shared/components/alert/alert.service';

@Component({
  selector: 'ey-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  response: object;
  index: any;
  items: MenuItem[];
  exceptionItems: MenuItem[];
  tenantObject: any;
  keys1: any;
  keys2: any;
  keys1Array: Array<any> = [];
  result: any;
  result2: any;
  refreshIndex: number;
  buildNumberResponse: object;
  constructor(
    private adminService: AdminService,
    private logger: CustomLoggerService,
    private alertService: AlertService
  ) {}

  ngOnInit() {
    this.loadItems();
    this.loadExceptionItems();
  }

  loadExceptionItems(): void {
    this.exceptionItems = [
      {
        items: [
          {
            label: 'Test exception',
            command: () => {
              this.getTestException();
            }
          },
          {
            label: 'Custom exception',
            command: () => {
              this.getCustomException();
            }
          }
        ]
      }
    ];
  }

  loadItems(): void {
    this.items = [
      {
        items: [
          {
            label: 'Nature of payment',
            command: () => {
              this.refreshNatureOfPayment();
            }
          },
          {
            label: 'Nature of income',
            command: () => {
              this.refreshNatureOfIncome();
            }
          },

          {
            label: 'TDS month tracker',
            command: () => {
              this.refreshTdsMonthTracker();
            }
          },
          {
            label: 'TCS month tracker',
            command: () => {
              this.refreshTcsMonthTracker();
            }
          },

          {
            label: 'TDS chart of accounts',
            command: () => {
              this.refreshChartOfAccounts();
            }
          },
          {
            label: 'TCS chart of accounts',
            command: () => {
              this.refreshTcsChartOfAccounts();
            }
          },
          {
            label: 'Collectee exempt status',
            command: () => {
              this.refreshCollecteeExemptStatus();
            }
          },
          {
            label: 'Tenant information',
            command: () => {
              this.refreshTenantInfo();
            }
          }
        ]
      }
    ];
  }
  refreshCollecteeExemptStatus(): void {
    this.adminService.refreshCollecteeExemptStatus().subscribe(
      (res: any) => {
        this.logger.info('collectee exempt status', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('collectee exempt status', error);
      }
    );
  }

  refreshChartOfAccounts(): void {
    this.adminService.refreshChartOfAccounts().subscribe(
      (res: any) => {
        this.logger.info('chart of accounts', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('chart of accounts', error);
      }
    );
  }

  refreshTcsChartOfAccounts(): void {
    this.adminService.refreshTcsChartOfAccounts().subscribe(
      (res: any) => {
        this.logger.info('chart of accounts', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('chart of accounts', error);
      }
    );
  }

  refreshNatureOfPayment(): void {
    this.adminService.refreshNatureOfPayment().subscribe(
      res => {
        this.logger.info('refresh nature of payment', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('refresh nature of payment', error);
      }
    );
  }

  refreshNatureOfIncome(): void {
    this.adminService.refreshNatureOfIncome().subscribe(
      res => {
        this.logger.info('refresh nature of income', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('refresh nature of income', error);
      }
    );
  }

  refreshTdsMonthTracker(): void {
    this.adminService.refreshTdsMonthTracker().subscribe(
      res => {
        this.logger.info('refresh tds month tracker', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('refresh tds month tracker', error);
      }
    );
  }

  refreshTcsMonthTracker(): void {
    this.adminService.refreshTcsMonthTracker().subscribe(
      res => {
        this.logger.info('refresh tcs month tracker', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('refresh tcs month tracker', error);
      }
    );
  }

  refreshTenantInfo(): void {
    this.adminService.refreshTenantInfo().subscribe(
      res => {
        this.logger.info('refresh tenant info', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('refresh tenant info', error);
      }
    );
  }

  notifyNewTenant(): void {
    this.adminService.notifyNewTenant().subscribe(
      res => {
        //
        // const tenant = res['client1.dvtfo.onmicrosoft.com'];
        // var valuesResult = Object.values(tenant);
        // var keysResult = Object.keys(tenant)
        // var result = Object.keys(tenant).map(function(el: any) {
        //   return {el: tenant[el]};
        // });
        this.logger.info('Notify new tenant', res);
      },
      error => {
        this.logger.error('Notify new tenant', error);
      }
    );
  }

  getTestException(): void {
    this.adminService.getTestException().subscribe(
      res => {
        this.logger.info('Test exception', res);
      },
      error => {
        this.logger.error('Test exception', error);
      }
    );
  }

  getCustomException(): void {
    this.adminService.getCustomException().subscribe(
      res => {
        this.logger.info('Custom exception', res);
      },
      error => {
        this.logger.error('Custom exception', error);
      }
    );
  }

  flushRedisData(): void {
    this.adminService.flushData().subscribe(
      res => {
        this.logger.info('flush data', res);
        this.showResponse({ index: 0 });
      },
      error => {
        this.logger.error('flush data', error);
      }
    );
  }

  showResponse(event: any): void {
    const index = event.index;
    if (index === 0) {
      this.adminService.getresponse().subscribe(
        (result: any) => {
          this.refreshIndex = 0;
          this.logger.info('get response', result);
          this.response = result;
        },
        (err: any) => {
          this.logger.error(err);
        }
      );
    } else if (index === 1) {
      this.adminService.buildNumber().subscribe(
        res => {
          this.refreshIndex = 1;
          this.logger.info('build number info', res);
          this.buildNumberResponse = res;
        },
        error => {
          this.alertService.error(error.error.message);
          this.logger.error('build number info', error);
        }
      );
    }
  }
}
