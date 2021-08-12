export class Scheduler{
    constructor(
 public id:number,
 public  airlineId:number,
 public flightId:number,
 public fromPlace:string,
 public toPlace:string,
 public departureTime:string,
 public arrivalTime:string,
 public economyTicketCost:number,
 public businessTicketCost:number,
 public s:boolean,public m:boolean,public t:boolean,public w:boolean,public th:boolean,public f:boolean,public st:boolean
    ){}
}