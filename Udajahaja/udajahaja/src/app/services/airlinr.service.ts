import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Airline } from "../models/Airline";

@Injectable({providedIn: "root"})
export class AirlineService{

    private url:string = "http://localhost:8001/admin/save";
    
    constructor(private httpClient:HttpClient){}

    getAllBooks(){
      
        return this.httpClient.get(this.url);
    }

saveAirline(book:Airline){

console.log('from service')
console.log(book)
return this.httpClient.post(this.url,book );

}
getAllAirlines(){
    return this.httpClient.get("http://localhost:8001/admin/findAirlines");
}

getAllAirlinesById(id:string){

    let params=new HttpParams();
    params.set('id',id);
    return this.httpClient.get("http://localhost:8001/admin/findAirlinesById/"+id,{params});
    
    }


}