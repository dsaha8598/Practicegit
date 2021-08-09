import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'convertlocalamount'
})
export class ConvertlocalamountPipe implements PipeTransform {
  transform(value: any, args?: any): any {
    if (value !== undefined && value !== null) {
      return value.toLocaleString('en-IN');
    }
  }
}
