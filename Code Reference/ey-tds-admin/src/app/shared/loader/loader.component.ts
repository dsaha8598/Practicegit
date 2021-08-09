import { Component, OnInit, Input } from '@angular/core';
import { LoaderService } from './loader.service';
import {
  trigger,
  transition,
  style,
  animate,
  state
} from '@angular/animations';

@Component({
  selector: 'ey-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss'],
  animations: [
    trigger('fadeInOut', [
      state(
        'void',
        style({
          opacity: 0
        })
      ),
      transition('void <=> *', animate(1000))
    ])
  ]
})
export class LoaderComponent implements OnInit {
  @Input() isLoading = false;
  @Input() message: string;

  constructor(private readonly loaderService: LoaderService) {}

  ngOnInit(): void {
    this.loaderService.getLoader().subscribe(result => {
      this.isLoading = result;
    });
  }
}
