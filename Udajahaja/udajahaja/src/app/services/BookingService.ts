import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Airline } from "../models/Airline";
import { BookingFlight } from "../models/BookingFlight";
import { Coupon } from "../models/Coupon";
import { Scheduler } from "../models/Scheduler";
import { Search } from "../models/SearchFlight";

@Injectable({ providedIn: "root" })
export class BookingService {


    private search:any;
    constructor(private httpClient: HttpClient) { }

    public setsearch(search:any){
        this.search=search;
    }
    public getsearch(){
        return this.search;
    }

    getAllSearchedFlight(search:Search) {

        return this.httpClient.post("http://localhost:8003/api/user/find/flights",search);

    }

    getAllAvailableCoupons(){
        return this.httpClient.get("http://localhost:8003/api/admin/getAllCoupons");
    }

    saveBooking(booking:BookingFlight){
        return this.httpClient.post("http://localhost:8003/api/user/save/bookings",booking);
    }

    getAllBookedTickets(email:string){
        let params = new HttpParams();
        params.set('id', email);
        return this.httpClient.get("http://localhost:8003/api/user/findAll/bookings/" + email, { 'params':params});
    }

   



}