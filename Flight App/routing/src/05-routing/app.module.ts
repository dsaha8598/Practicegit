import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { BrowserModule } from "@angular/platform-browser";
import { EventComponent } from "./app.component";
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { AboutComponent } from './about/about.component';
import { LoginComponent } from './login/login.component';
import { RouterModule, Routes } from "@angular/router";

const routes:Routes = [
    { path: "login", component: LoginComponent },
    { path: "about", component: AboutComponent }
];
@NgModule({
    declarations: [EventComponent, HeaderComponent, FooterComponent, AboutComponent, LoginComponent],
    imports: [BrowserModule, RouterModule.forRoot(routes)],
    bootstrap: [EventComponent]
})
export class Eventodule{}