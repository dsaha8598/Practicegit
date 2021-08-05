import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

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


@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    AdminLoginComponent,
    HomePageComponent,
    AdminHomePageComponent,
    
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,RouterModule.forRoot(routes), ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
