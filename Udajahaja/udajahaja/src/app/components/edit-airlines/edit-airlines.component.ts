import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Aeroplane } from 'src/app/models/Aeroplane';
import { Airline } from 'src/app/models/Airline';
import { AirlineService } from 'src/app/services/airlinr.service';
import { EditAirlineService } from 'src/app/services/edit-airline.service';

@Component({
  selector: 'app-edit-airlines',
  templateUrl: './edit-airlines.component.html',
  styleUrls: ['./edit-airlines.component.scss']
})
export class EditAirlinesComponent implements OnInit {

  airline:Airline;
  aeroplanes:Aeroplane[]
   numbers:number[]=[1,2,3];
  constructor(private router:Router,private airlineService:AirlineService,private editservice:EditAirlineService) {
    this.airline=editservice.getAirline();
    this.aeroplanes=this.airline.aeroplanes
    console.log("constructor from edit component")
    console.log(this.airline)
   }

  ngOnInit(): void {
  }

  
}
