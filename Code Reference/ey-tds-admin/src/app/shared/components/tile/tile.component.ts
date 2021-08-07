import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'ey-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss']
})
export class TileComponent implements OnInit {
  @Input() label: string;
  @Input() sublabel: string;
  @Input('href') routerLink: string;
  @Input() enableAddNew: boolean;
  @Input() addNewAuthority: string;
  @Input() listViewAuthority: string;
  addNewRouterLink: string;

  ngOnInit(): void {
    this.addNewRouterLink = `${this.routerLink}/form`;
  }
}
