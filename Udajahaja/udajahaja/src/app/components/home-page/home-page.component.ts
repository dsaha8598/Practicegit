import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Search } from 'src/app/models/SearchFlight';
import { BookingService } from 'src/app/services/BookingService';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent implements OnInit {

  places: string[] = ["Bhubaneswar", "Hyderabad", "Banglore", "Visakhapatnam", "Delhi", "Goa"]
  searchForm: any
  search:Search=new Search("","",null,null,"")
  oneWayTrip:any


  constructor(private formBuilder: FormBuilder, private router: Router,private service:BookingService) {
    this.searchForm = this.formBuilder.group({
      from: ['Select From', Validators.required],

      to: [
        'Select To', Validators.required
      ],
      fromDate: [
        '', Validators.required
      ],
      toDate: [

      ]

    })
  }

  ngOnInit(): void {
  }

  searchFlights() {
    console.log("search flight")
    console.log(this.searchForm.value)
    this.service.setsearch(this.searchForm.value);
    this.router.navigate(["/booking"])
  }



}
