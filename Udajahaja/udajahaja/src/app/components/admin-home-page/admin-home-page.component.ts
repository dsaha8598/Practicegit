import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Airline } from 'src/app/models/Airline';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-admin-home-page',
  templateUrl: './admin-home-page.component.html',
  styleUrls: ['./admin-home-page.component.scss']
})
export class AdminHomePageComponent implements OnInit {

  airlines:Airline[]=[];
  constructor(private router:Router,private airlineService:AirlineService) { }

  ngOnInit(): void {
    console.log("onit")
    this.airlineService.getAllAirlines()
    .subscribe((res:any)=>{
      console.log(res);
      this.airlines=res;
      console.log('printing airline object'+this.airlines)
  })
  }

  
  addNewAirline(){
    this.router.navigate(['/newAirline'])
  }

  viewAirline(id:number){
    console.log("view airline")
     console.log(id)
  }


}
