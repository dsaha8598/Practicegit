import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Airline } from "../models/Airline";
import { Coupon } from "../models/Coupon";
import { Scheduler } from "../models/Scheduler";

@Injectable({ providedIn: "root" })
export class AirlineService {

    private url: string = "http://localhost:8001/admin/save";

    constructor(private httpClient: HttpClient) { }

    getAllBooks() {

        return this.httpClient.get(this.url);
    }

    saveAirline(book: Airline) {

        console.log('from service')
        console.log(book)
        return this.httpClient.post(this.url, book);

    }
    getAllAirlines() {
        return this.httpClient.get("http://localhost:8001/admin/findAirlines");
    }

    getAllAirlinesById(id: string) {

        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8001/admin/findAirlinesById/" + id, { params });

    }

    getAllAeroplanesById(id: string) {

        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8001/admin/findAeroplanesById/" + id, { params });

    }

    saveScheduler(scheduler:Scheduler){
        console.log("scheduler service")
        console.log(scheduler)
        return this.httpClient.post("http://localhost:8001/admin/saveScheduler", scheduler);
    }

    getSchedulerByAirlinesId(id: string) {

        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8001/admin/findScheduler/byAirlineId/" + id, { params });

    }

    getSchedulerById(id: string) {

        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8001/admin/findScheduler/byId/" + id, { params });

    }

    saveCoupon(coupon: Coupon) {

        console.log('from service')
        console.log(coupon)
        return this.httpClient.post("http://localhost:8001/admin/saveCoupon", coupon);

    }

    getAllCoupons() {

        return this.httpClient.get("http://localhost:8001/admin/getAllCoupons");

    }

    getCouponsById(id: string) {

        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8001/admin/findCoupon/byId/" + id, { params });

    }

   



}