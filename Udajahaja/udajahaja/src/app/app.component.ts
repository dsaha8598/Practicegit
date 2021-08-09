import { Component } from '@angular/core';
import { Router,Event,NavigationStart,NavigationEnd } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'udajahaja';
  showLoadingIndicator=true;
  constructor(private router:Router){
      this.router.events.subscribe((routerEvent:Event)=>{
         if(routerEvent instanceof NavigationStart){
           this.showLoadingIndicator=true;
           console.log('navigation start')
         }

         if(routerEvent instanceof NavigationEnd){
          this.showLoadingIndicator=false;
          console.log('navigation end')
        }
      });
  }
}
