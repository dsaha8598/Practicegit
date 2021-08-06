import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-add-airline',
  templateUrl: './add-airline.component.html',
  styleUrls: ['./add-airline.component.scss']
})
export class AddAirlineComponent implements OnInit {

  airlines:number[]=[1];
  airlineForm:any;
  aeroplneForms:any;

  constructor(private formBuilder: FormBuilder,private airlineService:AirlineService) {  }

  ngOnInit(): void {
    this.airlineForm = this.formBuilder.group({
      airlineName: [''],
      airlineLogo: [File],
      contactNumber: [''],
      contactAddress: [''],
      aeroplanes:this.formBuilder.array([])
    })

  }

  get aeroplaneForms(){
    return this.airlineForm.get('aeroplanes');
  }

  addNewAeroplaneForm(){
    const aeroplaneForm=this.formBuilder.group({
      aeroplaneNumber:[],
      businessClassCount:[],
      economyClassCount:[],
      startDate:[],
      endDate:[]
    })
    this.aeroplaneForms.push(aeroplaneForm);
  }

  deleteAeroplaneForm(index:number){
    this.aeroplaneForms.removeAt(index);
  }

  saveAirlineForm(){
    console.log(this.airlineForm.value);
    this.airlineService.saveAirline(this.airlineForm.value)
    .subscribe(res=>{
      console.log(res);
      // this.bookForm.reset();
      // this.findAllBooks()
      // this.showBookFormFlag = false;
  })
  }

}
