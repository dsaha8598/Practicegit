import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Airline } from "../models/Airline";
import { Coupon } from "../models/Coupon";
import { Scheduler } from "../models/Scheduler";

@Injectable({ providedIn: "root" })
export class AirlineService {

    private url: string = "http://localhost:8003/api/admin/save";

    constructor(private httpClient: HttpClient) { }

    setHeaders(){
        let headers = new HttpHeaders()
        .set('Content-Type', 'application/json')
        .set('Authorization', sessionStorage.getItem("token"));
        return headers;
    }

    adminLogin() {

        return this.httpClient.get("http://localhost:8003/login", { responseType: 'text' });
    }



    saveAirline(book: Airline) {

        console.log('from service')
        console.log(book)
        let headers=this.setHeaders();
        return this.httpClient.post(this.url, book,{'headers':headers});

    }
    getAllAirlines() {
        let headers=this.setHeaders();
        return this.httpClient.get("http://localhost:8003/api/admin/findAirlines",{'headers':headers});
    }

    getAllAirlinesById(id: string) {
        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8003/api/admin/findAirlinesById/" + id, { 'params':params,'headers':headers });

    }

    getAllAeroplanesById(id: string) {

        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8003/api/admin/findAeroplanesById/" + id, {  'params':params,'headers':headers  });

    }

    saveScheduler(scheduler: Scheduler) {
        console.log("scheduler service")
        console.log(scheduler)
        let headers=this.setHeaders();
        return this.httpClient.post("http://localhost:8003/api/admin/saveScheduler", scheduler,{'headers':headers});
    }

    getSchedulerByAirlinesId(id: string) {

        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8003/api/admin/findScheduler/byAirlineId/" + id, { 'params':params,'headers':headers });

    }

    getSchedulerById(id: string) {

        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8003/api/admin/findScheduler/byId/" + id, { 'params':params,'headers':headers  });

    }

    saveCoupon(coupon: Coupon) {

        console.log('from service')
        console.log(coupon)
        let headers=this.setHeaders();
        return this.httpClient.post("http://localhost:8003/api/admin/saveCoupon", coupon,{'headers':headers});

    }

    getAllCoupons() {

        let headers=this.setHeaders();
        return this.httpClient.get("http://localhost:8003/api/admin/getAllCoupons",{'headers':headers});

    }

    getCouponsById(id: string) {

        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id);
        return this.httpClient.get("http://localhost:8003/api/admin/findCoupon/byId/" + id, { 'params':params,'headers':headers  });

    }

    deactivateAirline(id:number){
        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id+"");
        return this.httpClient.get("http://localhost:8003/api/admin/blockAirline/" + id, { 'params':params,'headers':headers});
    }

    activateAirline(id:number){
        let headers=this.setHeaders();
        let params = new HttpParams();
        params.set('id', id+"");
        return this.httpClient.get("http://localhost:8003/api/admin/unblockAirline/" + id, { 'params':params,'headers':headers});
    }

    generateReport(){
        let headers=this.setHeaders();
        return this.httpClient.get("http://localhost:8003/api/admin/download", { 'headers':headers});
    }





}