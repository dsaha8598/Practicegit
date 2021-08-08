import { Routes } from "@angular/router";
import { AddAirlineComponent } from "./components/add-airline/add-airline.component";
import { AdminHomePageComponent } from "./components/admin-home-page/admin-home-page.component";
import { AdminLoginComponent } from "./components/admin-login/admin-login.component";
import { EditAirlinesComponent } from "./components/edit-airlines/edit-airlines.component";
import { FlightScheduleComponent } from "./components/flight-schedule/flight-schedule.component";
import { HomePageComponent } from "./components/home-page/home-page.component";
import { ManageDiscountComponent } from "./components/manage-discount/manage-discount.component";

export const routes: Routes = [
    { path: "admin", component: AdminLoginComponent },
    { path: "admin2", component: AdminLoginComponent },
    { path: "", component: HomePageComponent },
    { path: "adminHome", component: AdminHomePageComponent },
    { path: "newAirline", component: AddAirlineComponent },
    { path: "scheduling", component: FlightScheduleComponent },
    { path: "discount", component: ManageDiscountComponent },
    { path: "editAirline", component: EditAirlinesComponent }
    //{ path: "**", component: HomePageComponent }
    
];