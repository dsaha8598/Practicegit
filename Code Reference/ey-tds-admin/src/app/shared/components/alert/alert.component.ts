import { Component, OnInit } from '@angular/core';
import { Alert } from '@app/shared/model/alert.model';
import { AlertService } from './alert.service';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';

@Component({
  selector: 'ey-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.scss']
})
export class AlertComponent implements OnInit {
  alerts: Alert[] = [];
  constructor(
    private readonly alertService: AlertService,
    private logger: CustomLoggerService
  ) {}

  ngOnInit(): void {
    this.alertService.getAlert().subscribe((alert: Alert) => {
      if (!alert) {
        // clear alerts when an empty alert is received
        this.alerts = [];
        return;
      }

      // add alert to array
      this.alerts.push(alert);
    });
  }

  removeAlert(alert: Alert): void {
    this.alerts = this.alerts.filter(x => x !== alert);
  }

  cssClass(alert: Alert): string {
    if (!alert) {
      return;
    }

    // return css class based on alert type
    switch (alert.type) {
      case 'success':
        return 'alert alert-success';
      case 'error':
        return 'alert alert-danger';
      case 'info':
        return 'alert alert-info';
      case 'warning':
        return 'alert alert-warning';
      default:
        this.logger.debug('default');
    }
  }
}
