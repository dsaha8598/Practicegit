import { Injectable } from '@angular/core';
import { Airline } from '../models/Airline';
import { AirlineService } from './airlinr.service';

@Injectable({
  providedIn: 'root'
})
export class EditAirlineService {

  constructor() { }
  airline:Airline;
  public setAirline(airline:Airline){
     this.airline=airline
  }

  public getAirline(){
    return this.airline
  }
}
