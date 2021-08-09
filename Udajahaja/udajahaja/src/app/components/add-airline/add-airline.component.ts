import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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
  

  constructor(
    private formBuilder: FormBuilder,
    private airlineService:AirlineService,
    private router:Router
    ) {  
      this.airlineForm = this.formBuilder.group({
        airlineName: [
                  '',Validators.required
                 // Validators.pattern("([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z]{2,4})")
                   ],
        airlineLogo: [
          ''
        ],
        contactNumber: [
          '',Validators.required
        ],
        contactAddress: [
          '',Validators.required
        ],
        aeroplanes:this.formBuilder.array([])
      })
    }

  ngOnInit(): void {
    

    
   

  }

  get aeroplaneForms(){
    return this.airlineForm.get('aeroplanes');
  }

  addNewAeroplaneForm(){
    const aeroplaneForm=this.formBuilder.group({
      aeroplaneNumber:['',Validators.required],
      businessClassCount:['',Validators.required],
      economyClassCount:['',Validators.required],
      startDate:['',Validators.required],
      endDate:[]
    })
    this.aeroplaneForms.push(aeroplaneForm);
    this.aeroplneForms.invalid=true;
  }

  deleteAeroplaneForm(index:number){
    this.aeroplaneForms.removeAt(index);
  }

  saveAirlineForm(){
    console.log('save airline is clicked')
    console.log(this.airlineForm.value);
    this.airlineService.saveAirline(this.airlineForm.value)
    .subscribe(res=>{
      console.log(res);
      this.router.navigate(['/adminHome'])
  })
  }

}
