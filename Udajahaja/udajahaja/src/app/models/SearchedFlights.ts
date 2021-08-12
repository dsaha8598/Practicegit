export class SearchedFlights{
    constructor(
        public  id:number,
	public  airlineId:number,
	public  flightId:number,
	public  fromPlace:string,
	public  toPlace:string,
	public  departureTime:string,
	public  arrivalTime:string,
	public  economyTicketCost:number,
	public  businessTicketCost:number,
	public  airlineName:string,
	public  flightName:string,
	public isReturn:string,
	public  startDate:Date,
	public  returnDate:Date
    ){}
}