import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-home-page',
  templateUrl: './admin-home-page.component.html',
  styleUrls: ['./admin-home-page.component.scss']
})
export class AdminHomePageComponent implements OnInit {

  constructor(private router:Router) { }

  ngOnInit(): void {
  }

  logOut(){
   this.router.navigate(['/admin'])
  }

}
