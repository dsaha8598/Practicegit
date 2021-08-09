import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
//import { OnboardingService } from '@app/shared/components/onboarding/onboarding.service';
import { StorageService } from '@app/shell/authentication/storageservice';

@Component({
  selector: 'ey-deductor-group-form',
  templateUrl: './deductor-group-form.component.html',
  styleUrls: ['./deductor-group-form.component.scss']
})
export class DeductorGroupFormComponent implements OnInit {
  tenantFormGroup: FormGroup;
  deductorPan: string;
  isOnboardingParameters = false;
  tenantNameSubmitted: boolean;
  tenant: boolean;
  allDeductors: any = [];
  isConfigurations = true;
  constructor(
    private readonly activeRoute: ActivatedRoute,
    private storageService: StorageService
  ) {}

  ngOnInit(): void {
    const formType = this.activeRoute.snapshot.queryParams['action'];
    this.storageService.setItem(
      'tenantId',
      this.activeRoute.snapshot.queryParams['tenantName']
    );
    this.isOnboardingParameters = true;
    if (formType == 'view') {
      this.isConfigurations = false;
    }
  }
}
