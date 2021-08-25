import { Component, OnInit } from '@angular/core';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {

  constructor(private service:AirlineService) { }

  ngOnInit(): void {
  }

  generateReport(){
    this.service.generateReport().subscribe();
  }

}
