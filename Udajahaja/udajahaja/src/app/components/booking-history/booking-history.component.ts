import { Component, OnInit } from '@angular/core';
import { BookingService } from 'src/app/services/BookingService';

@Component({
  selector: 'app-booking-history',
  templateUrl: './booking-history.component.html',
  styleUrls: ['./booking-history.component.scss']
})
export class BookingHistoryComponent implements OnInit {

  tickets:any=[]
  constructor(private service:BookingService) { }

  ngOnInit(): void {
    this.service.getBookingHistory()
    .subscribe(res=>{
       this.tickets=res
       
    })
  }

}
