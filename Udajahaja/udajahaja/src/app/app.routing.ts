import { Routes } from "@angular/router";
import { AdminHomePageComponent } from "./components/admin-home-page/admin-home-page.component";
import { AdminLoginComponent } from "./components/admin-login/admin-login.component";
import { HomePageComponent } from "./components/home-page/home-page.component";

export const routes: Routes = [
    { path: "admin", component: AdminLoginComponent },
    { path: "admin2", component: AdminLoginComponent },
    { path: "", component: HomePageComponent },
    { path: "adminHome", component: AdminHomePageComponent }
    //{ path: "**", component: HomePageComponent }
    
];