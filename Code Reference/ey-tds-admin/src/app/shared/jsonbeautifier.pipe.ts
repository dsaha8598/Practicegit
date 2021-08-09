import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'jsonbeautifier'
})
export class JsonbeautifierPipe implements PipeTransform {
  transform(val: any) {
    return JSON.stringify(val, undefined, 4)
      .replace(/ /g, '&nbsp;')
      .replace(/\n/g, '<br/>');
  }
}
