import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { INatureOfPayment } from '@app/shared/model/natureOfPayment.model';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class NatureofpaymentService {
  RESOURCE_URL = `${environment.api.masters}nature-of-payment`;
  private readonly NOP_UPLOAD_URL = `${environment.api.masters}nature-of-payment/upload/excel`;
  constructor(private readonly http: HttpClient) {}

  getNatureOfPaymentList(): Observable<any> {
    return this.http
      .get<any>(this.RESOURCE_URL)
      .pipe(map((res: any) => res.data));
  }
  getNatureOfPaymentById(id: number): Observable<INatureOfPayment> {
    return this.http
      .get<INatureOfPayment>(`${this.RESOURCE_URL}/${id}`)
      .pipe(map((res: any) => res.data));
  }

  updateNatureOfPaymentById(data: any): Observable<INatureOfPayment> {
    return this.http
      .put<INatureOfPayment>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }

  addNatureOfPayment(data: any): Observable<INatureOfPayment> {
    return this.http
      .post<INatureOfPayment>(this.RESOURCE_URL, data)
      .pipe(map((res: any) => res.data));
  }
  uploadExcel(formData: FormData): Observable<any> {
    return this.http.post<any>(this.NOP_UPLOAD_URL, formData);
  }
}
