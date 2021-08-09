import { Directive, ElementRef, HostListener } from '@angular/core';

@Directive({
  selector: '[eyRatedecimalfloter]'
})
export class RatedecimalfloterDirective {
  constructor(private readonly el: ElementRef) {}

  @HostListener('keydown', ['$event']) onKeyDown(e: any): void {
    this.handeleData(e, 'keypress');
  }

  @HostListener('focusout', ['$event']) onFocusOut(e: any): void {
    this.handeleData(e, 'focus');
  }

  handeleData(e: any, type: string): void {
    try {
      if (e && e.target && e.target.value) {
        if (e.target.value >= 100 || e.target.value < 0) {
          e.target.value = undefined;
        } else {
          if (type === 'focus' && e.target.value) {
            e.target.value = Number.parseFloat(e.target.value).toPrecision(4);
          }
        }
      }
    } catch (error) {
      console.log(error);
      e.target.value = undefined;
    }
  }
}
