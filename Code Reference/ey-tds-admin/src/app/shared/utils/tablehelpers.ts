interface IHeaders {
  field?: string;
  header?: string;
}
/**
 * TableHelpers class is used to generate table headers dyanamically.
 */
export class TableHelpers {
  public static tableHeaderGenerator(
    requestData: {},
    requiredData: IHeaders[]
  ): IHeaders[] {
    const isObjectWithObjects: boolean = this.typeOfObjectChecker(requestData);
    Object.keys(requestData).forEach((key: string): void => {
      const fieldObject: IHeaders = {};
      if (isObjectWithObjects) {
        // recursive
        requiredData[this.stringConverter(key)] = this.tableHeaderGenerator(
          requestData[key],
          []
        );
      } else {
        fieldObject.field = key;
        fieldObject.header = this.stringConverter(key);
        if (Array.isArray(requiredData)) {
          requiredData.push(fieldObject);
        }
      }
    });

    return requiredData;
  }
  private static stringConverter(key: string): string {
    let temp: string = '';
    const emptyString: string = ' ';
    temp += key.charAt(0).toUpperCase();
    for (let i = 1; i < key.length; i += 1) {
      if (
        key.charAt(i).match(/\d+/) !== null ||
        key.charAt(i).match(/\d+/) !== undefined
      ) {
        if (
          key.charAt(i) === key.charAt(i).toUpperCase() &&
          (key.charAt(i - 1) !== key.charAt(i - 1).toUpperCase() &&
            key.charAt(i + 1) !== key.charAt(i + 1).toUpperCase())
        ) {
          temp += emptyString + key.charAt(i).toUpperCase();
        } else if (
          (key.charAt(i) === key.charAt(i).toUpperCase() &&
            (key.charAt(i - 1) !== key.charAt(i - 1).toUpperCase() &&
              key.charAt(i + 1) === key.charAt(i + 1).toUpperCase())) ||
          (key.charAt(i) === key.charAt(i).toUpperCase() &&
            (key.charAt(i - 1) === key.charAt(i - 1).toUpperCase() &&
              key.charAt(i + 1) !== key.charAt(i + 1).toUpperCase()))
        ) {
          temp += emptyString + key.charAt(i).toUpperCase();
        } else {
          temp += key.charAt(i);
        }
      } else {
        temp += key.charAt(i);
      }
    }

    return temp;
  }

  private static typeOfObjectChecker(requestData: object): boolean {
    let counter: number = 0;
    Object.keys(requestData).forEach((each: string): void => {
      if (typeof requestData[each] === 'object') {
        counter += 1;
      }
    });
    if (counter === Object.keys(requestData).length) {
      return true;
    }

    return false;
  }
}
