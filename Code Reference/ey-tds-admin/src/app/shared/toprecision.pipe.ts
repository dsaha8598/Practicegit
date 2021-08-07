import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'toprecision'
})
export class ToprecisionPipe implements PipeTransform {
  transform(value: any): any {
    if (value !== undefined && value !== null) {
      return Number.parseFloat(value.toFixed(2));
    }
  }
}
