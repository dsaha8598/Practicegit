import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'ey-redirect-to-route',
  templateUrl: './redirect-to-route.component.html',
  styleUrls: ['./redirect-to-route.component.scss']
})
export class RedirectToRouteComponent implements OnInit {
  @Input() data: any;
  @Input() colField: any;
  @Input() action: string;
  @Input() type: string;
  fieldValue: string;
  queryParams: any;

  ngOnInit(): void {
    if (this.action.toUpperCase() === 'EDIT') {
      this.fieldValue = 'Edit';
    } else {
      console.log(this.data, this.colField);
      this.fieldValue = this.data[this.colField];
    }
    this.queryParams = {
      action: this.action,
      id: this.data.id,
      type: this.type
    };
    // console.log(this.queryParams);
  }
}
