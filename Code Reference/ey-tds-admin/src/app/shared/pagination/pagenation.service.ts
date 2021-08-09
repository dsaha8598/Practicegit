import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PagenationService {
  noMoreResultsState = 'NO_MORE_RESULTS';

  pageState(
    state: any,
    pagenationStates: any,
    pageSize?: number,
    pageNumber?: number
  ): any {
    console.log('Inside PageState obj from pagination', pageSize, pageNumber);
    const pageSizeValue = pageSize ? pageSize : 10;
    const pageNumberValue = pageNumber ? pageNumber : 1;
    if (state === 'first') {
      return this.first(pageSizeValue, pageNumberValue);
    }
    if (state === 'next') {
      return this.nextPage(pageSizeValue, pagenationStates, pageNumberValue);
    }

    return this.prevPage(pageSizeValue, pagenationStates, pageNumberValue);
  }

  nextPage(pageSize: number, pagingState: any, pageNumber: number): any {
    const obj = {
      next: true,
      // tslint:disable-next-line: object-shorthand-properties-first
      pageSize,
      pageStates: pagingState,
      pageNumber: pageNumber + 1
    };
    console.log('Printing the next obj in service', obj);
    return obj;
  }

  first(pageSize: number, pageNumber: number): any {
    const obj = {
      next: false,
      // tslint:disable-next-line: object-shorthand-properties-first
      pageSize,
      pageNumber
    };
    console.log('Printing the First obj in service', obj);

    return obj;
  }

  prevPage(pageSize: number, pagingState: any, pageNumber: number): any {
    const obj = {
      next: false,
      // tslint:disable-next-line: object-shorthand-properties-first
      pageSize,
      pageStates: pagingState,
      pageNumber: pageNumber - 1
    };
    console.log('Printing the previous obj in service', obj);

    return obj;
  }
}
