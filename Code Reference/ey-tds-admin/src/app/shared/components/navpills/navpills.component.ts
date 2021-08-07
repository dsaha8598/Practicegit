import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'ey-navpills',
  templateUrl: './navpills.component.html',
  styleUrls: ['./navpills.component.scss']
})
export class NavpillsComponent implements OnInit {
  @Input() label: string;
  @Input('href') routerLink: string;
  @Input() authority: string;
  constructor() {}

  ngOnInit() {}
}
