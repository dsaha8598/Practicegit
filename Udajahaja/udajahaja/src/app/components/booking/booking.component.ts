import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { SearchedFlights } from 'src/app/models/SearchedFlights';
import { BookingService } from 'src/app/services/BookingService';

@Component({
  selector: 'app-booking',
  templateUrl: './booking.component.html',
  styleUrls: ['./booking.component.scss']
})
export class BookingComponent implements OnInit {

  onewayTrip: SearchedFlights[] = []
  allTrip: SearchedFlights[] = []
  returnTrip: SearchedFlights[] = []
  journeyDate: any
  bookingFormsArray: any[]=[];
  bookingFlightForm: any
  isBookingDetailsVisible:boolean=false
  noOfBookingScreens:number[]=[];
  economy:number
  businessClass:number


  constructor(private service: BookingService, private formBuilder: FormBuilder) {
    this.bookingFlightForm = this.formBuilder.group({
      flightId: [''],
      journeydate: [
        '', Validators.required
      ],
      from: [
        ''
      ],
      to: [
        ''
      ],

      departureTime: [
        ''
      ],
      arrivalTime: [
       ''
      ],
      isEconomy: [
       ''
      ],
      isBusinessClass: [
       ''
      ],
      totalPrice: [
       ''
      ],
      aadharNumber: []

    })
    
  }

  ngOnInit(): void {
    this.searchFlight();
  }

  searchFlight() {
    console.log("from booking component")
    console.log(this.service.getsearch())
    this.journeyDate = this.service.getsearch().fromDate;
    this.service.getAllSearchedFlight(this.service.getsearch())
      .subscribe((res: any) => {
        console.log(res);
        this.allTrip = res;
        //console.log('printing airline object' + this.oneWayTrip)
        for (var search of this.allTrip) {
          if (search.isReturn == "Yes") {
            this.returnTrip.push(search)
          } else {
            this.onewayTrip.push(search)
          }
        }
      })
  }

  onstartDateBookingClick(id: number) {
    this.onewayTrip[id];
    this.bookingFlightForm = this.formBuilder.group({
      flightId: [this.onewayTrip[id].flightId],
      journeydate: [
        this.onewayTrip[id].startDate, Validators.required
      ],
      from: [
        this.onewayTrip[id].fromPlace
      ],
      to: [
        this.onewayTrip[id].toPlace
      ],

      departureTime: [
        this.onewayTrip[id].departureTime
      ],
      arrivalTime: [
        this.onewayTrip[id].arrivalTime
      ],
      isEconomy: [
       ''
      ],
      isBusinessClass: [
       ''
      ],
      totalPrice: [
       '',Validators.required
      ],
      aadharNumber: ['',Validators.required]

    })
    this.bookingFormsArray.push(this.bookingFlightForm)
    console.log('from booking start date')
    console.log(this.bookingFlightForm)
    this.isBookingDetailsVisible=true;
    this.economy=this.onewayTrip[id].economyTicketCost
    this.businessClass=this.onewayTrip[id].businessTicketCost
    this.noOfBookingScreens.push(1);

  }

  onreturnDateBookingClick(id:number){
    this.returnTrip[id];
    this.bookingFlightForm = this.formBuilder.group({
      flightId: [this.returnTrip[id].flightId],
      journeydate: [
        this.returnTrip[id].returnDate, Validators.required
      ],
      from: [
        this.returnTrip[id].fromPlace
      ],
      to: [
        this.returnTrip[id].toPlace //fromPlace
      ],

      departureTime: [
        this.returnTrip[id].departureTime
      ],
      arrivalTime: [
        this.returnTrip[id].arrivalTime
      ],
      isEconomy: [
       ''
      ],
      isBusinessClass: [
       ''
      ],
      totalPrice: [
       '',Validators.required
      ],
      aadharNumber: ['',Validators.required]

    })
    this.bookingFormsArray.push(this.bookingFlightForm)
    console.log('from booking start date')
    console.log(this.bookingFlightForm)
    this.isBookingDetailsVisible=true;
    this.noOfBookingScreens.push(1);
    this.economy=this.returnTrip[id].economyTicketCost
    this.businessClass=this.onewayTrip[id].businessTicketCost
  }

  econmyClassAmount(id:number){
    console.log('check box')
    console.log(this.economy)
      this.bookingFlightForm.patchValue({
      totalPrice:this.economy
    })
  }
  businessClassAmount(id:number){
    console.log('check box')
    console.log(this.economy)
      this.bookingFlightForm.patchValue({
      totalPrice:this.businessClass
    })
  }



}
