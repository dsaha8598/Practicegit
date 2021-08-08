import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-flight-schedule',
  templateUrl: './flight-schedule.component.html',
  styleUrls: ['./flight-schedule.component.scss']
})
export class FlightScheduleComponent implements OnInit {

  numbers:Number[]=[1,2,3,4,5,6,7,8];
  constructor() { }

  ngOnInit(): void {
  }

}
