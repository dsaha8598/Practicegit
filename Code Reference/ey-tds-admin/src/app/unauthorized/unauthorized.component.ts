import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';

@Component({
  selector: 'ey-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.scss']
})
export class UnauthorizedComponent implements OnInit {
  constructor(
    private readonly router: Router,
    private authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    setTimeout(() => {
      this.goToHomepage();
    }, 15000);
  }

  goToHomepage(): void {
    this.authenticationService.logout();
    this.router
      .navigate(['/'])
      .then()
      .catch();
  }
}
