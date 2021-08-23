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
  email:string="";
  constructor(private service:BookingService) { }

  ngOnInit(): void {
    console.log("booking on it")
    this.getBookedTicketsByEmail()
  }

  cancelTicket(id:string){
    this.service.cancelTicket(id)
    .subscribe(res=>{
      this.getBookedTicketsByEmail()
    })
  }

  getBookedTicketsByEmail(){
    if(sessionStorage.getItem("email")!=null){
      this.email=sessionStorage.getItem("email")
  }
  this.service.getAllBookedTickets(this.email)
  .subscribe(res=>{
    this.tickets=res;
    console.log("view ticket details")
    console.log(this.tickets)
  })
  }

  getBookedTicketsByEmailFromSearch(){
    console.log("retrievig available ticket  = "+this.email)
    sessionStorage.setItem("email",this.email)
    
  this.service.getAllBookedTickets(this.email)
  .subscribe(res=>{
    this.tickets=res;
    console.log("view ticket details")
    console.log(this.tickets)
  })
  }

  downloadTicket(id:string){
    this.service.downloadTicket(id)
    .subscribe()
  }

}
