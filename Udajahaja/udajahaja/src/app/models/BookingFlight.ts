export class BookingFlight{
    constructor(
        public airlineId:number,
        public flightId:number,
        public journeydate:Date,
        public from:string,
        public to:string,
        public departureTime:string,
        public arrivalTime:string,
        public isEconomy:boolean,
        public isBusinessClass:boolean,
        public totalPrice:number,
        public email:string,
        public flightNumber:string,
        public classType:string,
        public pnrNumber:string,
        public id:number,
        public status:string
        ){}
}