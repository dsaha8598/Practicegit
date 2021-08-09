import { HttpParams } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

export class UtilModule {
  // only static letibles can be accessble in static functions
  private static readonly actionStates = {
    view: 'VIEW',
    edit: 'EDIT',
    new: 'NEW'
  };
  static stateChanger(form: FormGroup, state?: string, fields?: Array<string>) {
    let actionState;
    if (state && state !== '') {
      state = state.toUpperCase();
      if (state === this.actionStates.edit) {
        actionState = state;
        this.enableRequiredFields(form, fields);
      } else if (state === this.actionStates.view) {
        actionState = state;
        this.disableForm(form);
      } else {
        actionState = this.actionStates.new;
        // this.reset(form);
      }
    } else {
      actionState = this.actionStates.new;
    }
    return actionState;
  }

  static getCurrentMonth(month: number): string {
    const monthNames = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December'
    ];

    return monthNames[month];
  }

  static reset(form: FormGroup) {
    form.reset();
    for (const i in form.controls) {
      if (form.controls.hasOwnProperty(i)) {
        form.controls[i].setErrors(null);
        form.controls[i].enable();
      }
    }
  }

  static disableForm(form: FormGroup) {
    for (const i in form.controls) {
      if (form.controls.hasOwnProperty(i)) {
        form.controls[i].disable();
      }
    }
  }

  static enableRequiredFields(form: FormGroup, fields: Array<string>) {
    if (fields.length) {
      for (const i in form.controls) {
        if (form.controls.hasOwnProperty(i)) {
          if (fields.includes(i)) {
            form.controls[i].enable();
          } else {
            form.controls[i].disable();
          }
        }
      }
    } else {
      console.error('fields must be an array');
    }
  }

  static enableFields(form: FormGroup) {
    for (const i in form.controls) {
      if (form.controls.hasOwnProperty(i)) {
        form.controls[i].enable();
      }
    }
  }

  static getCurrentFinancialYear(): number {
    if (new Date().getMonth() > 2) {
      return new Date().getFullYear() + 1;
    }

    return new Date().getFullYear();
  }

  static getMonthByMonthString(monthString: string): number {
    const months = {
      january: 1,
      february: 2,
      march: 3,
      april: 4,
      may: 5,
      june: 6,
      july: 7,
      august: 8,
      september: 9,
      october: 10,
      november: 11,
      december: 12
    };

    return months[monthString];
  }

  static formatBytes(bytes: any, decimals = 2): string {
    if (bytes === 0) {
      return '0 Bytes';
    }
    if (bytes < 1024) {
      return bytes + ' Bytes';
    }
    if (bytes < 1048576) {
      return (bytes / 1024).toFixed(3) + ' KB';
    }
    if (bytes < 1073741824) {
      return (bytes / 1048576).toFixed(3) + ' MB';
    } else {
      return (bytes / 1073741824).toFixed(3) + ' GB';
    }
  }

  static parseAmount(amount: string): number {
    return parseFloat(amount.replace(/,/g, ''));
  }

  static convertLocalAmount(amount: number): string {
    return amount.toLocaleString('en-IN');
  }

  static deleteVariablesInObject(list: any, deleteList: Array<string>): void {
    for (let i = 0; i <= deleteList.length; i++) {
      delete list[deleteList[i]];
    }
  }

  static hideLoader(): object {
    return { params: new HttpParams().set('hideLoader', 'true') };
  }
}
