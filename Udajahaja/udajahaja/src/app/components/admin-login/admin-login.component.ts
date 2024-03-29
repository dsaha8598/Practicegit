import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { routes } from 'src/app/app.routing';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-admin-login',
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.scss']
})
export class AdminLoginComponent implements OnInit {

  loginForm:FormGroup;
  isValid:boolean=true;
  
  
  constructor(private router: Router,private service:AirlineService) {
    this.loginForm = new FormGroup({
      email: new FormControl("", [
        Validators.required,
        Validators.pattern("([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z]{2,4})")
    ]),
    password: new FormControl("", [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(8)
    ])
})

//document.getElementById("adminLoginButton").style.visibility = "hidden";
    
   }

  ngOnInit(): void {
  }

  getLoginData(){
   if(this.loginForm.value.email=="dipak@gmail.com"){
     console.log('valid')
   }else{
    console.log('invalid');
    this.isValid=false;
   }
   if(this.loginForm.value.password=="dipak678"){
    console.log('valid')
  }else{
   console.log('invalid')
   this.isValid=false;
  }
  if(this.isValid==true){
    this.service.adminLogin().subscribe(
      res=>{
        let token:any=res;
        sessionStorage.setItem("token",token);
        console.log(sessionStorage.getItem("token"))
        this.router.navigate(['/adminHome'])
      }
    )
    
  }
  }

}
