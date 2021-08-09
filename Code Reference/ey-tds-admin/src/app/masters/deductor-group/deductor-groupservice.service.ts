import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class DeductorGroupService {
  private createMasterServiceURl = '';
  private getDeductorGroupsURL = `${environment.api.administration}tenants`;

  constructor(private httpclient: HttpClient) {}

  postMasterService(reqObj: any) {
    return this.httpclient.post(this.createMasterServiceURl, reqObj);
  }

  getDeductorGroups() {
    return this.httpclient.get(this.getDeductorGroupsURL);
  }
}
