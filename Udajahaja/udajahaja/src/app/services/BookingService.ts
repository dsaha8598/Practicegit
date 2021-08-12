import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Airline } from "../models/Airline";
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

        return this.httpClient.post("http://localhost:8001/admin/find/flights",search);

    }

   



}