import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
//import { start } from 'repl';
import { Airline } from 'src/app/models/Airline';
import { AirlineService } from 'src/app/services/airlinr.service';
import { EditAirlineService } from 'src/app/services/edit-airline.service';

@Component({
  selector: 'app-admin-home-page',
  templateUrl: './admin-home-page.component.html',
  styleUrls: ['./admin-home-page.component.scss']
})
export class AdminHomePageComponent implements OnInit {

  airlines: Airline[] = [];
  airline: Airline;
  constructor(private router: Router, private airlineService: AirlineService, private editservice: EditAirlineService) { }

  ngOnInit(): void {
    console.log("onit")
    this.airlineService.getAllAirlines()
      .subscribe((res: any) => {
        console.log(res);
        this.airlines = res;
        console.log('printing airline object' + this.airlines)
      })
  }


  addNewAirline() {
    this.router.navigate(['/newAirline'])
  }

  viewAirline(id: number) {
    console.log("view airline")
    console.log(id)
    //this.router.routerState=start;
    this.airlineService.getAllAirlinesById(id + '')
      .subscribe((res: any) => {
        console.log(res);
        //this.airlines=res;
        //let
        this.airline = new Airline(res.airlineName, res.contactNumber, res.contactAddress, null, res.aeroplanes, res.id);
        console.log(this.airline)
        this.editservice.setAirline(this.airline)

        this.router.navigate(['/editAirline'])

      })
  }

  viewAirlineDetailsById(id: number) {

  }


}
