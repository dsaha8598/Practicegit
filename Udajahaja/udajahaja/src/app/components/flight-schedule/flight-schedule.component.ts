import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Aeroplane } from 'src/app/models/Aeroplane';
import { Airline } from 'src/app/models/Airline';
import { Scheduler } from 'src/app/models/Scheduler';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-flight-schedule',
  templateUrl: './flight-schedule.component.html',
  styleUrls: ['./flight-schedule.component.scss']
})
export class FlightScheduleComponent implements OnInit {

  hide:boolean=true;
  schedulerForm:any;
  editSchedulerForm:any;
  numbers:number[]=[1,2,3]
  airlines:Airline
  aeroplane:Aeroplane[]
  scheduler:Scheduler
  editScheduler:Scheduler=new Scheduler(0,0,0,"","","","",1,1,true,true,true,true,true,true,true);
  
  constructor( private formBuilder: FormBuilder,private router:Router,private airlineService:AirlineService) { 
    this.schedulerForm = this.formBuilder.group({
      id:[''],
      airlineId: [
                '',Validators.required
               // Validators.pattern("([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z]{2,4})")
                 ],
      flightId: [
        ''
      ],
      fromPlace: [
        '',Validators.required
      ],
      toPlace: [
        '',Validators.required
      ],
      departureTime: [
        '',Validators.required
      ],
      arrivalTime: [
        '',Validators.required
      ],
      economyTicketCost: [
        '',Validators.required
      ],
      businessTicketCost: [
        '',Validators.required
      ],
      availableDays: [
        ''
      ],
      s: [],m: [],t: [],w: [],th: [],f: [],st: []
     
    })
    this.editSchedulerForm=this.schedulerForm
  }

  ngOnInit(): void {
    this.getAirlines();
  }

  saveSchedule(){
    console.log("saving scheduler")
    console.log(this.schedulerForm.value)
    this.airlineService.saveScheduler(this.schedulerForm.value)
    .subscribe(res=>{
      console.log(res);
      this.schedulerForm.reset()
      this.router.navigate(['/scheduling'])
  })
  this.schedulerForm.reset()
  }

  getAirlines() {
    this.airlineService.getAllAirlines()
      .subscribe((res: any) => {
        console.log(res);
        this.airlines = res;
        console.log('printing airline object' + this.airlines)
      })
  }

  getFligtsById(id:string){
    console.log('fetching by id'+id)
    this.airlineService.getAllAeroplanesById(id)
    .subscribe((res: any) => {
      console.log(res);
      this.aeroplane = res;
      console.log('printing airline object' + this.aeroplane)
    })
  }

  getSchedulesByAirlineId(id:string){
    this.airlineService.getSchedulerByAirlinesId(id)
    .subscribe((res: any) => {
      console.log(res);
      this.scheduler = res;
      console.log('printing airline object' + this.aeroplane)
    })
  }

  viewScheduleById(id:string){
    this.airlineService.getSchedulerById(id)
    .subscribe((res: any) => {
      console.log(res);
      this.editScheduler = res;
      this.hide=false
      this.prepareFormGroupWithAvailableData(this.editScheduler)
     
      //this.editSchedulers.push(this.scheduler)
      //console.log('printing airline object' + this.editSchedulers)
    })
  }

  prepareFormGroupWithAvailableData(schedule:Scheduler){
    console.log(schedule.s)
    this.editSchedulerForm = this.formBuilder.group({
      id:[schedule.id],
      airlineId: [
                schedule.airlineId,Validators.required
                 ],
      flightId: [
        schedule.flightId
      ],
      fromPlace: [
        schedule.fromPlace,Validators.required
      ],
      toPlace: [
        schedule.toPlace,Validators.required
      ],
      departureTime: [
        schedule.departureTime,Validators.required
      ],
      arrivalTime: [
        schedule.arrivalTime,Validators.required
      ],
      economyTicketCost: [
        schedule.economyTicketCost,Validators.required
      ],
      businessTicketCost: [
        schedule.businessTicketCost,Validators.required
      ],
      availableDays: [
       ''
      ],
      s: [schedule.s],m: [schedule.m],t: [schedule.t],w: [schedule.w],th: [schedule.th],f: [schedule.f],st: [schedule.st]
     
    })
  }

  updateSchedulerDetails(){
    this.airlineService.saveScheduler(this.editSchedulerForm.value)
    .subscribe(res=>{
      console.log(res);
     // this.editSchedulerForm.reset()
      this.router.navigate(['/scheduling'])
  })
  this.editSchedulerForm.reset()
  }
  

}
