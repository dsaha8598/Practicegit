import { Aeroplane } from "./Aeroplane";

export  class Airline{
    constructor(public airlineName:string,public contactNumber:number,public contactAddress:string,public airlineLogo:File,public aeroplanes:Aeroplane[],public id:number){}
}