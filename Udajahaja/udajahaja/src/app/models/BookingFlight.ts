export class BookingFlight{
    constructor(
        public flightId:number,
        public journeydate:Date,
        public from:string,
        public to:string,
        public departureTime:string,
        public arrivalTime:string,
        public isEconomy:boolean,
        public isBusinessClass:boolean,
        public totalPrice:number,
        public aadharNumber:string,
        ){}
}