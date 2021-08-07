import {
  Component,
  OnInit,
  Input,
  SimpleChanges,
  Output,
  EventEmitter
} from '@angular/core';
import { OnboardingService } from '../onboarding/onboarding.service';
import { FormGroup, FormControl } from '@angular/forms';
import { DeductorService } from '../deductor/deductor.service';
@Component({
  selector: 'ey-customtransformation',
  templateUrl: './customtransformation.component.html',
  styleUrls: ['./customtransformation.component.scss']
})
export class CustomtransformationComponent implements OnInit {
  deductorPan: string;
  custmTransferForm: FormGroup;
  @Input() custTransform: boolean;
  @Input() deductorInputId: string;
  @Output() modelStateEmmiter: EventEmitter<boolean> = new EventEmitter<
    boolean
  >();
  deductorList: any[];
  panList: any = [];

  constructor(
    private onboarding: OnboardingService,
    private deductorService: DeductorService
  ) {}

  ngOnInit() {
    this.custmTransform();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const newVal = changes['custTransform'].currentValue;
    if (newVal) {
      this.custTransform = newVal;
      this.openCustomTransForm(this.deductorInputId);
    }
  }

  custmTransform(): void {
    this.custmTransferForm = new FormGroup({
      invoice_excel: new FormControl(''),
      provisions: new FormControl(''),
      advances: new FormControl(''),
      gl: new FormControl('')
    });
  }

  getDeductorData(): void {
    this.deductorService.getDeductor().subscribe(
      (res: any[]) => {
        this.deductorList = res;
        for (let i = 0; i < this.deductorList.length; i++) {
          this.panList.push(this.deductorList[i]['pan']);
        }
        console.log(this.panList);
      },
      error => {
        // this.logger.error(error)
      }
    );
  }

  openCustomTransForm(deductorInputId: any) {
    this.onboarding.getConfigurations(deductorInputId).subscribe(
      (res: any) => {
        if (res.data == null) {
          this.custmTransferForm.reset();
        } else {
          this.custmTransferForm.patchValue(res.data.customJobs);
        }
      },
      error => {}
    );
  }
  submitCustTransformData(): void {
    const obj = {
      config: {
        invoice_excel: this.custmTransferForm.controls.invoice_excel.value,
        provisions: this.custmTransferForm.controls.provisions.value,
        advances: this.custmTransferForm.controls.advances.value,
        gl: this.custmTransferForm.controls.gl.value
      },
      deductorPan: this.deductorInputId
    };
    this.deductorService.postCustmTranfromData(obj).subscribe(
      (res: any) => {
        this.custmTransferForm.reset();
      },
      error => {
        // this.alertService.error(error.error.message);
      }
    );
  }
}
