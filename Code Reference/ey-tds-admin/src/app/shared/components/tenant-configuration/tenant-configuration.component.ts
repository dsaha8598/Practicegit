import { Component, OnInit, Input } from '@angular/core';
import {
  FormGroup,
  FormControl,
  Validators,
  FormBuilder,
  FormArray
} from '@angular/forms';
import { TenantConfigurationService } from './tenant-configurationservice.service';
import { environment } from '../../../../environments/environment';
import { MenuItem } from 'primeng/api';
import { ActivatedRoute, Router } from '@angular/router';
import { DeductorService } from '@app/shared/components/deductor/deductor.service';
import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'ey-tenant-configuration',
  templateUrl: './tenant-configuration.component.html',
  styleUrls: ['./tenant-configuration.component.scss']
})
export class TenantConfigurationComponent implements OnInit {
  isTenantId = false;
  items: MenuItem[];
  empty: any = '';
  sftpConfigurationForm: FormGroup;
  reportName: any;
  reportId: any;
  reports: any = [];
  reportsForm: FormArray;
  blobConfigurationForm: FormGroup;
  tenantFormGroup: FormGroup;
  submitted = false;
  tenantNameSubmitted = false;
  allDeductors: any = [];
  tenantname: string;
  submitted1 = false;
  tenantId: string;
  deductId: any;
  tenant = false;
  submitted2 = false;
  submitted3 = false;
  deductorPan: string;
  actionState: any;
  isAddTenant = true;
  configTypeData: any;
  activeIndex: number = 0;
  databaseConfigurationsForm: FormGroup;
  powerbiConfigurationsForm: FormGroup;
  headingMsg: string;
  constructor(
    private tenantService: TenantConfigurationService,
    private readonly activeRoute: ActivatedRoute,
    private deductorService: DeductorService,
    private readonly alertService: AlertService,
    private router: Router,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.buildFormGroup();
    this.stepper();
    const formType = this.activeRoute.snapshot.queryParams['action'];
    if (formType === 'view') {
      this.isAddTenant = false;
      const tanantId = this.activeRoute.snapshot.queryParams['tenantId'];
      this.getTenantConfigDetails(tanantId);
      this.getDeductorData();
    }
    this.headingMsg = 'Add';
  }

  stepper(): void {
    this.items = [
      {
        label: 'Tenant Creation',
        command: (event: any) => {
          this.activeIndex = 0;
        }
      },
      {
        label: 'Tenant Configurations',
        command: (event: any) => {
          this.activeIndex = 1;
        }
      }
    ];
  }

  getTenantConfigDetails(tenantId: any): void {
    this.tenantService.getTenantConfigDetails(tenantId).subscribe(
      (res: any) => {
        this.deductId = res.data.deductorPan;
        this.tenantname = res.data.tenantName;
        this.setDatabaseConfigValues(res.data);
        this.setSFTPConfigValues(res.data);
        this.setBlobConfigValues(res.data);
        this.setPowerbiConfigValues(res.data);
      },
      error => {
        //  TODO: logger service
      }
    );
  }

  setBlobConfigValues(res: any): void {
    for (let i = 0; i < res.tenantConfig.length; i++) {
      if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.blobConfig + 'protocol'
      ) {
        this.blobConfigurationForm.controls.host.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.blobConfig + 'account-name'
      ) {
        this.blobConfigurationForm.controls.username.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.blobConfig + 'account-key'
      ) {
        this.blobConfigurationForm.controls.accountkey.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.blobConfig + 'container'
      ) {
        this.blobConfigurationForm.controls.container.setValue(
          res.tenantConfig[i].configValue
        );
      }
    }
  }

  setSFTPConfigValues(res: any): void {
    for (let i = 0; i < res.tenantConfig.length; i++) {
      if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'host'
      ) {
        this.sftpConfigurationForm.controls.host.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'user'
      ) {
        this.sftpConfigurationForm.controls.username.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'password'
      ) {
        this.sftpConfigurationForm.controls.password.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'privateKey'
      ) {
        this.sftpConfigurationForm.controls.privateKey.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'classpath'
      ) {
        this.sftpConfigurationForm.controls.classpath.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.sftpConfig + 'privateKeyPassphrase'
      ) {
        this.sftpConfigurationForm.controls.privateKeyPassphrase.setValue(
          res.tenantConfig[i].configValue
        );
      }
    }
  }

  setDatabaseConfigValues(res: any): void {
    for (let i = 0; i < res.tenantConfig.length; i++) {
      if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'contact-points'
      ) {
        this.databaseConfigurationsForm.controls.contactpoints.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'keyspace-name'
      ) {
        this.databaseConfigurationsForm.controls.keyspace.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'schema-action'
      ) {
        this.databaseConfigurationsForm.controls.schemaAction.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'ssl'
      ) {
        this.databaseConfigurationsForm.controls.ssl.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'port'
      ) {
        this.databaseConfigurationsForm.controls.port.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'username'
      ) {
        this.databaseConfigurationsForm.controls.username.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ==
        environment.tenantConfigTypes.databaseConfig + 'password'
      ) {
        this.databaseConfigurationsForm.controls.password.setValue(
          res.tenantConfig[i].configValue
        );
      }
    }
  }

  setPowerbiConfigValues(res: any): void {
    for (let i = 0; i < res.tenantConfig.length; i++) {
      if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'uri.access'
      ) {
        this.powerbiConfigurationsForm.controls.uriAccess.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'uri.embed'
      ) {
        this.powerbiConfigurationsForm.controls.uriEmbed.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'grant.type'
      ) {
        this.powerbiConfigurationsForm.controls.grantType.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'resource'
      ) {
        this.powerbiConfigurationsForm.controls.resource.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'client.id'
      ) {
        this.powerbiConfigurationsForm.controls.clientId.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'client.secret'
      ) {
        this.powerbiConfigurationsForm.controls.clientSecreat.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'user.name'
      ) {
        this.powerbiConfigurationsForm.controls.username.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'password'
      ) {
        this.powerbiConfigurationsForm.controls.password.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (
        res.tenantConfig[i].configCode ===
        environment.tenantConfigTypes.powerbiConfig + 'group.id'
      ) {
        this.powerbiConfigurationsForm.controls.groupId.setValue(
          res.tenantConfig[i].configValue
        );
      } else if (res.tenantConfig[i].configCode.includes('report.id')) {
        const configCode = res.tenantConfig[i].configCode;
        let powerbiReplace = configCode.replace('power-bi.', '');
        let reportName = powerbiReplace.replace('.report.id', '');
        this.reportId = res.tenantConfig[i].configValue;
        this.reportName = reportName;
        this.reportsForm.push(this.initiatedReport());
      }
    }
    this.reportsForm.removeAt(0);
  }

  initiatedReport(): any {
    return this.fb.group({
      reportName: [this.reportName],
      reportId: [this.reportId]
    });
  }

  buildFormGroup(): void {
    this.databaseConfigurationsForm = new FormGroup({
      contactpoints: new FormControl('', [Validators.required]),
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
      keyspace: new FormControl('', [Validators.required]),
      ssl: new FormControl(false, [Validators.required]),
      port: new FormControl('', [Validators.required]),
      schemaAction: new FormControl('')
    });
    this.blobConfigurationForm = new FormGroup({
      host: new FormControl('', [Validators.required]),
      username: new FormControl('', [Validators.required]),
      accountkey: new FormControl('', [Validators.required]),
      container: new FormControl('', [Validators.required])
    });
    this.sftpConfigurationForm = new FormGroup({
      host: new FormControl('', [Validators.required]),
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
      privateKey: new FormControl('', [Validators.required]),
      classpath: new FormControl('', [Validators.required]),
      privateKeyPassphrase: new FormControl('', [Validators.required])
    });
    this.reportsForm = this.fb.array([this.initiateReport()]);
    this.powerbiConfigurationsForm = new FormGroup({
      uriAccess: new FormControl('', [Validators.required]),
      uriEmbed: new FormControl('', [Validators.required]),
      grantType: new FormControl('', [Validators.required]),
      resource: new FormControl('', [Validators.required]),
      clientId: new FormControl('', [Validators.required]),
      clientSecreat: new FormControl('', [Validators.required]),
      groupId: new FormControl('', [Validators.required]),
      username: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required]),
      reports: this.reportsForm
    });
    this.tenantFormGroup = new FormGroup({
      // deductorPan: new FormControl('', [Validators.required]),
      // groupName: new FormControl('', [Validators.required]),
      tenantName: new FormControl('', [Validators.required])
    });
  }

  initiateReport(): FormGroup {
    return this.fb.group({
      reportName: [''],
      reportId: ['']
    });
  }

  addReports(): void {
    this.reportsForm.push(this.initiateReport());
  }

  removeContact(index: any): void {
    this.reportsForm.removeAt(index);
  }

  get f(): any {
    return this.databaseConfigurationsForm.controls;
  }

  get p(): any {
    return this.powerbiConfigurationsForm.controls;
  }

  back(): void {
    this.activeIndex = this.activeIndex - 1;
  }

  continue(): void {
    this.tenantNameSubmitted = true;
    if (this.tenantFormGroup.invalid) {
      return;
    }
    this.activeIndex = this.activeIndex + 1;
  }

  get f1(): any {
    return this.blobConfigurationForm.controls;
  }

  get f2(): any {
    return this.sftpConfigurationForm.controls;
  }

  getDeductorData(): any {
    this.deductorService.getDeductor().subscribe(
      (result: any[]) => {
        for (let i = 0; i < result.length; i = i + 1) {
          const allDeductors = {
            label: result[i].deductorName,
            value: result[i].id
          };
          this.allDeductors = [...this.allDeductors, allDeductors];
        }
      },
      (error: any) => {
        console.log(error);
      }
    );
  }

  submitConfigurations(): void {
    this.submitted = true;
    this.submitted1 = true;
    this.submitted2 = true;
    this.submitted3 = true;
    if (this.tenantFormGroup.invalid) {
      return;
    }
    if (this.blobConfigurationForm.invalid) {
      return;
    }
    if (this.databaseConfigurationsForm.invalid) {
      return;
    }
    if (this.sftpConfigurationForm.invalid) {
      return;
    }
    if (this.powerbiConfigurationsForm.invalid) {
      return;
    }
    const configurationsData = {
      tenantInfo: {
        groupName: this.tenantFormGroup.controls.groupName.value,
        tenantName: this.tenantFormGroup.controls.tenantName.value
      },
      configValues: [
        {
          configCode: environment.tenantConfigTypes.blobConfig + 'protocol',
          configValue: this.blobConfigurationForm.controls.host.value
        },
        {
          configCode: environment.tenantConfigTypes.blobConfig + 'account-name',
          configValue: this.blobConfigurationForm.controls.username.value
        },
        {
          configCode: environment.tenantConfigTypes.blobConfig + 'account-key',
          configValue: this.blobConfigurationForm.controls.accountkey.value
        },
        {
          configCode: environment.tenantConfigTypes.blobConfig + 'container',
          configValue: this.blobConfigurationForm.controls.container.value
        },
        {
          configCode: environment.tenantConfigTypes.sftpConfig + 'host',
          configValue: this.sftpConfigurationForm.controls.host.value
        },
        {
          configCode: environment.tenantConfigTypes.sftpConfig + 'user',
          configValue: this.sftpConfigurationForm.controls.username.value
        },
        {
          configCode: environment.tenantConfigTypes.sftpConfig + 'password',
          configValue: this.sftpConfigurationForm.controls.password.value
        },
        {
          configCode: environment.tenantConfigTypes.sftpConfig + 'privateKey',
          configValue: this.sftpConfigurationForm.controls.privateKey.value
        },
        {
          configCode: environment.tenantConfigTypes.sftpConfig + 'classpath',
          configValue: this.sftpConfigurationForm.controls.classpath.value
        },
        {
          configCode:
            environment.tenantConfigTypes.sftpConfig + 'privateKeyPassphrase',
          configValue: this.sftpConfigurationForm.controls.privateKeyPassphrase
            .value
        },
        {
          configCode:
            environment.tenantConfigTypes.databaseConfig + 'contact-points',
          configValue: this.databaseConfigurationsForm.controls.contactpoints
            .value
        },
        {
          configCode: environment.tenantConfigTypes.databaseConfig + 'username',
          configValue: this.databaseConfigurationsForm.controls.username.value
        },
        {
          configCode:
            environment.tenantConfigTypes.databaseConfig + 'schema-action',
          configValue: this.databaseConfigurationsForm.controls.schemaAction
            .value
        },
        {
          configCode: environment.tenantConfigTypes.databaseConfig + 'password',
          configValue: this.databaseConfigurationsForm.controls.password.value
        },
        {
          configCode:
            environment.tenantConfigTypes.databaseConfig + 'keyspace-name',
          configValue: this.databaseConfigurationsForm.controls.keyspace.value
        },
        {
          configCode: environment.tenantConfigTypes.databaseConfig + 'ssl',
          configValue: this.databaseConfigurationsForm.controls.ssl.value
        },
        {
          configCode: environment.tenantConfigTypes.databaseConfig + 'port',
          configValue: this.databaseConfigurationsForm.controls.port.value
        },
        {
          configCode:
            environment.tenantConfigTypes.powerbiConfig + 'uri.access',
          configValue: this.powerbiConfigurationsForm.controls.uriAccess.value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'uri.embed',
          configValue: this.powerbiConfigurationsForm.controls.uriEmbed.value
        },
        {
          configCode:
            environment.tenantConfigTypes.powerbiConfig + 'grant.type',
          configValue: this.powerbiConfigurationsForm.controls.grantType.value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'resource',
          configValue: this.powerbiConfigurationsForm.controls.resource.value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'client.id',
          configValue: this.powerbiConfigurationsForm.controls.clientId.value
        },
        {
          configCode:
            environment.tenantConfigTypes.powerbiConfig + 'client.secret',
          configValue: this.powerbiConfigurationsForm.controls.clientSecreat
            .value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'user.name',
          configValue: this.powerbiConfigurationsForm.controls.username.value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'group.id',
          configValue: this.powerbiConfigurationsForm.controls.groupId.value
        },
        {
          configCode: environment.tenantConfigTypes.powerbiConfig + 'password',
          configValue: btoa(
            this.powerbiConfigurationsForm.controls.password.value
          )
        }
      ]
    };
    for (
      let i = 0;
      i < this.powerbiConfigurationsForm.value.reports.length;
      i++
    ) {
      let obj = {
        configCode: '',
        configValue: ''
      };
      obj.configCode =
        environment.tenantConfigTypes.powerbiConfig +
        this.powerbiConfigurationsForm.value.reports[i].reportName +
        '.report.id';
      obj.configValue = this.powerbiConfigurationsForm.value.reports[
        i
      ].reportId;
      configurationsData.configValues.push(obj);
    }

    this.tenantService.postConfigurations(configurationsData).subscribe(
      (res: any) => {
        this.tenantFormGroup.reset();
        this.databaseConfigurationsForm.reset();
        this.sftpConfigurationForm.reset();
        this.blobConfigurationForm.reset();
        this.goBack();
      },
      error => {
        this.alertService.error(error.error.message);
      }
    );
  }
  goBack(): void {
    this.router
      .navigate(['/dashboard/masters/deductor-group'])
      .then()
      .catch();
  }
}
