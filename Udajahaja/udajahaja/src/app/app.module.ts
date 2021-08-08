import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { HttpClientModule } from "@angular/common/http";
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { FooterComponent } from './components/footer/footer.component';
import { AdminLoginComponent } from './components/admin-login/admin-login.component';
import { RouterModule } from '@angular/router';
import { routes } from './app.routing';
import { HomePageComponent } from './components/home-page/home-page.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AdminHomePageComponent } from './components/admin-home-page/admin-home-page.component';
import { AddAirlineComponent } from './components/add-airline/add-airline.component';
import { AdminHeaderComponent } from './components/admin-header/admin-header.component';
import { FlightScheduleComponent } from './components/flight-schedule/flight-schedule.component';
import { ManageDiscountComponent } from './components/manage-discount/manage-discount.component';
import { EditAirlinesComponent } from './components/edit-airlines/edit-airlines.component';


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    AdminLoginComponent,
    HomePageComponent,
    AdminHomePageComponent,
    AddAirlineComponent,
    AdminHeaderComponent,
    FlightScheduleComponent,
    ManageDiscountComponent,
    EditAirlinesComponent,
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,RouterModule.forRoot(routes), ReactiveFormsModule,HttpClientModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
