import { Component, OnInit } from '@angular/core';
import { BookingFlight } from 'src/app/models/BookingFlight';
import { BookingService } from 'src/app/services/BookingService';

@Component({
  selector: 'app-manage-booking',
  templateUrl: './manage-booking.component.html',
  styleUrls: ['./manage-booking.component.scss']
})
export class ManageBookingComponent implements OnInit {

  isSearchenable:boolean=true;
  emailid:any=''
  tickets:any=[]
  constructor(private service:BookingService) { }

  ngOnInit(): void {
    console.log("booking on it")
    if(sessionStorage.getItem("email")!=null){
        this.emailid=sessionStorage.getItem("email")
    }
    this.service.getAllBookedTickets(this.emailid)
    .subscribe(res=>{
      this.tickets=res;
      console.log("view ticket details")
      console.log(this.tickets)
    })
  }

}
