import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HSNApplicationMastersService } from '../hsn-application-masters.service';
import { UtilModule } from '@app/shared';
import { AlertService } from '@app/shared/components/alert/alert.service';

@Component({
  selector: 'ey-hsn-form',
  templateUrl: './hsn-form.component.html',
  styleUrls: ['./hsn-form.component.scss']
})
export class HSNFormComponent implements OnInit {
  hsnForm: FormGroup;
  getnatureofpaymentlist: any = [];
  submitted: boolean;
  headingMsg: string;
  selectedNatureofPayment: any;
  actionState: any;
  sectionlist: any = [];
  selectedSection: any;
  submittedTo: boolean;
  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
    private readonly hsnService: HSNApplicationMastersService,
    private readonly actRoute: ActivatedRoute,
    private readonly alertService: AlertService
  ) {}
  ngOnInit(): void {
    this.submitted = false;
    this.getnatureofpaymentlist = [];
    this.sectionlist = [];
    this.getnatureofpaymentlist.push({
      label: 'Select nature of income',
      value: ''
    });
    this.sectionlist.push({
      label: 'Select section',
      value: ''
    });

    this.getSectionBasedOnNOP();
    this.hsnForm = this.formBuilder.group({
      hsnCode: ['', Validators.required],
      description: ['', Validators.required],
      natureOfPayment: '',
      tdsSection: '',
      id: ''
    });
    this.headingMsg = 'Add';
    this.stateChecker();
    // this.onChanges();
  }

  get f(): any {
    return this.hsnForm.controls;
  }

  fetchDatabyId(params: any): void {
    this.hsnService.getHSNById(params.id).subscribe(
      (result: any) => {
        this.headingMsg = 'View';
        this.hsnForm.patchValue(result);
        this.selectedSection = result.tdsSection;
        this.getNatureofpayment('fetch');
        if (params.action.toUpperCase() === 'EDIT') {
          this.stateChanger('edit');
        } else {
          this.headingMsg = 'View';
          this.actionState = UtilModule.stateChanger(
            this.hsnForm,
            params.action,
            []
          );
        }
      },
      (error: any) => console.error(error)
    );
  }
  getNatureofpayment(value: any): void {
    if (value === 'onChangeSection') {
      this.getnatureofpaymentlist = [];
      this.getnatureofpaymentlist.push({
        label: 'Select nature of income',
        value: ''
      });
    }
    this.hsnService
      .getNatureofPaymentList(this.selectedSection)
      .subscribe((result: any) => {
        for (let i = 0; i < result.length; i++) {
          let nature = {
            label: result[i],
            value: result[i]
          };
          this.getnatureofpaymentlist = [
            ...this.getnatureofpaymentlist,
            nature
          ];
        }
      });
  }

  nopChangeHandler(event: any): void {
    this.hsnForm.controls['natureOfPayment'].setValue(event.value);
  }

  sectionChangeHandler(event: any): void {
    this.selectedSection = event.value;
    this.getNatureofpayment('onChangeSection');
    this.hsnForm.get('natureOfPayment').setValidators(Validators.required);
    this.hsnForm.get('natureOfPayment').updateValueAndValidity();
  }
  getSectionBasedOnNOP(): void {
    this.hsnService.getSectionList().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let section = {
          label: result[i],
          value: result[i]
        };
        this.sectionlist = [...this.sectionlist, section];
      }
    });
  }
  reset(): void {
    UtilModule.reset(this.hsnForm);
    this.submitted = false;
  }

  saveHsn(type: string) {
    this.submitted = true;
    if (this.hsnForm.invalid) {
      return;
    }
    this.hsnService.createHSN(this.hsnForm.getRawValue()).subscribe(
      (res: any) => {
        this.reset();
        window.scrollTo(0, 0);
        if (type === 'back') {
          setTimeout(() => {
            this.goBack();
          }, 500);
        }
      },
      (error: any) => {
        this.reset();
        window.scrollTo(0, 0);
      }
    );
  }
  goBack(): void {
    this.router
      .navigate(['/dashboard/masters/hsn-application'])
      .then()
      .catch();
  }
  updateHsn(): void {
    this.submittedTo = true;
    if (this.hsnForm.invalid) {
      window.scrollTo(0, 0);
      return;
    }
    this.hsnService.update(this.hsnForm.getRawValue()).subscribe(
      (res: any) => {
        this.reset();
        window.scrollTo(0, 0);
        setTimeout(() => {
          this.goBack();
        }, 500);
      },
      (error: any) => {
        this.reset();
        window.scrollTo(0, 0);
      }
    );
  }

  private stateChanger(type: string): void {
    UtilModule.stateChanger(this.hsnForm, type, [
      'hsnCode',
      'description',
      'natureOfPayment',
      'tdsSection'
    ]);
    this.headingMsg = 'Update';
    this.actionState = 'EDIT';
  }

  private stateChecker(): void {
    this.actRoute.queryParams.subscribe((params: any) => {
      console.log(params);
      if (!params.action || params.action === '') {
        this.actionState = UtilModule.stateChanger(this.hsnForm, 'New');
      } else if (
        params.action.toUpperCase() === 'EDIT' ||
        params.action.toUpperCase() === 'VIEW'
      ) {
        console.log(params);
        if (params.id && params.id !== 0) {
          this.fetchDatabyId(params);
        }
      }
    });
  }
}
