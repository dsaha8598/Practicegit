import { Location } from '@angular/common';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DeductorGroupService } from '@app/masters/deductor-group/deductor-groupservice.service';
//import { UserService } from '@app/shared/components/users/user.service';
import { StorageService } from '@app/shell/authentication/storageservice';
import { OnboardingServiceTcs } from '../../onboarding-tcs/onboarding-tcs.service';
import { RolesPermissionsTcsService } from '../roles-permissions-tcs.service';

@Component({
  selector: 'ey-roles-permission-tcs-form',
  templateUrl: './roles-permission-tcs-form.component.html',
  styleUrls: ['./roles-permission-tcs-form.component.scss']
})
export class RolesPermissionTcsFormComponent implements OnInit {
  selectedPermissions: any = [];
  masters: any = [];
  selectedValues: any;
  selectedLabels: any = [];
  selectedGroups: any = [];
  selectedTabs: any = [];
  isMasters: boolean;
  showPermissions: boolean;
  deductorGroupList: any = [];
  rolesListData: any = [];
  isViewPermissions: boolean;
  tabNames: any = ([] = []);
  groupNames: any = [];
  isPermissions: boolean;
  labelIndex: any;
  groupIndex: any;
  finalObj: any = [];
  labelNames: any = [];
  permissionForm: FormGroup;
  permissionKeys: any = [];
  permissionsListData: any = [];
  isValidations: boolean;
  roleName: string;
  submitted: boolean;
  tenantId: any;
  validations: any = [];
  Transactions: any = [];
  isTransactions: boolean;
  selectedTabName: any;
  actionState: any;
  authorizedPermissions: any = [];
  tempObject: any = [];
  roleForm: FormGroup;
  headingMsg: string;
  formType: any;
  selectedPermissionsData: any = [];
  totalRolePermissionListData: any = [];
  @Input() formActionType: string;
  @Input() scopeType: string;
  @Input() deductorPan: string;
  @Output() manageForm: EventEmitter<any>;
  @Output() listHandler: EventEmitter<boolean>;
  @Output() closeRoles: EventEmitter<boolean>;

  constructor(
    private rolePermissionService: RolesPermissionsTcsService,
    private storageService: StorageService,
    //  private location: Location,
    private router: Router,
    private deductorGroupService: DeductorGroupService,
    private onboardingService: OnboardingServiceTcs
  ) {
    this.listHandler = new EventEmitter<boolean>();
    this.manageForm = new EventEmitter<any>();
    this.closeRoles = new EventEmitter<any>();
  }

  ngOnInit() {
    this.getPermssionsList();
    this.getDeductorGroups();
    this.buildFormGroup();
    this.formType = this.formActionType;
  }

  buildFormGroup() {
    this.roleForm = new FormGroup({
      roleName: new FormControl('', [Validators.required])
    });
    this.permissionForm = new FormGroup({
      permissionName: new FormControl('', [Validators.required]),
      tenantName: new FormControl('', [Validators.required]),
      permissionDisplayName: new FormControl('', [Validators.required]),
      permissionType: new FormControl('ALLOW'),
      tabName: new FormControl('', [Validators.required]),
      groupingName: new FormControl(''),
      labelName: new FormControl(''),
      levelId: new FormControl('', [Validators.required]),
      sortId: new FormControl('', [Validators.required])
    });
  }

  get r() {
    return this.roleForm.controls;
  }

  getDeductorGroups() {
    this.deductorGroupService.getDeductorGroups().subscribe(
      (res: any) => {
        for (let i = 0; i < res.data.length; i++) {
          let alldeductorGroupList = {
            label: res.data[i].tenantName,
            value: res.data[i].tenantName
          };
          this.deductorGroupList = [
            ...this.deductorGroupList,
            alldeductorGroupList
          ];
        }
      },
      error => {}
    );
  }

  getOnboardingParameters() {
    this.onboardingService.getConfigurations(this.deductorPan).subscribe(
      (res: any) => {
        this.checkPanLdcExistancy(res.data.onboardingConfigValues.scopeProcess);
        this.removeOnboardingRolePermissions();
      },
      (error: any) => {}
    );
  }

  removeOnboardingRolePermissions() {
    const tabIndex = this.permissionsListData.findIndex(
      (x: any) => x.tabName == 'Masters'
    );
    if (tabIndex != -1) {
      const onboardingGroupIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex((x: any) => {
        if (x.authority !== null) {
          x.authority.permissionDisplayName == 'Onboarding';
        }
      });
      if (onboardingGroupIndex != -1) {
        this.permissionsListData[tabIndex].groupNameList.splice(
          onboardingGroupIndex,
          1
        );
      }
      const rolepermissionGroupIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex(
        (x: any) => x.authority.permissionDisplayName == 'Roles and Permissions'
      );
      if (rolepermissionGroupIndex != -1) {
        this.permissionsListData[tabIndex].groupNameList.splice(
          rolepermissionGroupIndex,
          1
        );
      }
    }
  }

  checkPanLdcExistancy(res: any) {
    const isPanLdc = res.includes('1');
    if (!isPanLdc) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.authority.permissionDisplayName == 'Validation'
      );
      const ldcIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex(
        (x: any) => x.authority.permissionDisplayName == 'Validation_LCC'
      );
      this.permissionsListData[tabIndex].groupNameList.splice(ldcIndex, 1);
      const panIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex(
        (x: any) => x.authority.permissionDisplayName === 'PAN Validation'
      );
      this.permissionsListData[tabIndex].groupNameList.splice(panIndex, 1);
    }
  }

  checkTransactionMastersExistancy(res: any, res1: any) {
    const isTransactions = res.includes(2);
    const isMasters = res.includes(3);
    if (!isTransactions && !isMasters) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Masters'
      );
      this.permissionsListData.splice(tabIndex, 1);
      const tabIndex1 = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Transactions'
      );
      this.permissionsListData.splice(tabIndex1, 1);
    } else {
      this.checkInvoiceProcess(res1);
    }
  }

  checkChallansExistancy(res: any) {
    const isChallan = res.includes(4);
    if (!isChallan) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Challans'
      );
      this.permissionsListData.splice(tabIndex, 1);
    }
  }

  checkFilingExistancy(res: any) {
    const isFilings = res.includes(5);
    if (!isFilings) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Filing'
      );
      this.permissionsListData.splice(tabIndex, 1);
    } else {
      this.checkConsoleJustificationExistancy(res);
    }
  }

  checkInvoiceProcess(res: any) {
    const tabIndex = this.permissionsListData.findIndex(
      (x: any) => x.tabName == 'Transactions'
    );
    const isExcel = res.includes(2);
    if (!isExcel) {
      const index = this.permissionsListData[tabIndex].groupNameList.findIndex(
        (x: any) => x.authority.permissionDisplayName == 'Invoice excel'
      );
      this.permissionsListData[tabIndex].groupNameList.splice(index, 1);
    }
  }

  checkTransactionGLExistancy(res: any) {
    const isGL = res.includes(6);
    if (!isGL) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Transactions'
      );
      const glIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex(
        (x: any) => x.groupName == 'Tcs-Transactions-gl'
      );
      this.permissionsListData[tabIndex].groupNameList.splice(glIndex, 1);
    }
  }

  checkConsoleJustificationExistancy(res: any) {
    const isConsoleJustificationFiling = res.includes(7);
    if (!isConsoleJustificationFiling) {
      const tabIndex = this.permissionsListData.findIndex(
        (x: any) => x.tabName == 'Filing'
      );
      const JustificationFilingIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex(
        (x: any) => x.groupName == 'Filing-Justification'
      );
      const ConsoleFilingIndex = this.permissionsListData[
        tabIndex
      ].groupNameList.findIndex((x: any) => x.groupName == 'Filing-delayed');
      this.permissionsListData[tabIndex].groupNameList.splice(
        JustificationFilingIndex,
        1
      );
      this.permissionsListData[tabIndex].groupNameList.splice(
        ConsoleFilingIndex,
        1
      );
    }
  }

  casendraRoles() {
    this.rolePermissionService.getRolesByPan(this.deductorPan).subscribe(
      (res: any) => {
        this.totalRolePermissionListData = res.data;
        for (let i = 0; i < res.data.length; i++) {
          let rolesListData = {
            label: res.data[i].roleName,
            value: res.data[i].roleName
          };
          this.rolesListData = [...this.rolesListData, rolesListData];
        }
        this.showPermissions = true;
        const roleId = this.storageService.getItem('roleId');
        const roleIndex = this.totalRolePermissionListData.findIndex(
          (x: any) => x.key.roleId == roleId
        );
        this.roleName = this.rolesListData[roleIndex].label;
        let permissions = this.totalRolePermissionListData[roleIndex]
          .permissionNames;
        this.initiatePermissions(permissions);
      },
      (error: any) => {}
    );
  }

  getPermssionsList() {
    this.rolePermissionService.getPermissions().subscribe(
      (res: any) => {
        this.permissionsListData = res.tabNameList;
        this.getOnboardingParameters();
        if (this.formType == 'form') {
          this.isViewPermissions = false;
          this.getRoles();
        } else {
          this.isViewPermissions = true;
          this.casendraRoles();
        }
      },
      (error: any) => {}
    );
  }

  getRoles() {
    this.rolePermissionService.getRoles().subscribe(
      (res: any) => {
        this.totalRolePermissionListData = res;
        for (let i = 0; i < res.length; i++) {
          let rolesListData = {
            label: res[i].roleName,
            value: res[i].roleName
          };
          this.rolesListData = [...this.rolesListData, rolesListData];
        }
      },
      (error: any) => {}
    );
  }

  showRolePermissions(event: any) {
    this.showPermissions = true;

    const roleIndex = this.rolesListData.findIndex((x: any) => {
      return x.value === event.value;
    });
    if (roleIndex != -1) {
      let permissions = [];
      if (this.formType == 'form') {
        permissions = this.totalRolePermissionListData[roleIndex].permissions;
      } else {
        permissions = this.totalRolePermissionListData[roleIndex]
          .permissionNames;
      }
      this.selectedPermissionsData = [];
      this.unselectCheckBoxes(permissions);
    }
  }

  unselectCheckBoxes(permissions: any) {
    for (let i = 0; i < this.permissionsListData.length; i++) {
      this.permissionsListData[i].isTabChecked = null;
      this.permissionsListData[i].label = 'Root';

      this.groupNames = this.permissionsListData[i]['groupNameList'];

      for (let g = 0; g < this.groupNames.length; g++) {
        this.permissionsListData[i]['groupNameList'][g].isGroupChecked = null;

        this.labelNames = this.groupNames[g]['labels'];

        for (let l = 0; l < this.labelNames.length; l++) {
          this.permissionsListData[i]['groupNameList'][g]['labels'][
            l
          ].isLabelChecked = null;
        }
      }
    }
    this.initiatePermissions(permissions);
  }

  initiatePermissions(res: any) {
    this.authorizedPermissions = [];
    for (let i = 0; i < this.permissionsListData.length; i++) {
      for (let j = 0; j < res.length; j++) {
        if (this.permissionsListData[i].authority.permissionName == res[j]) {
          this.permissionsListData[i].isTabChecked = true;
          this.authorizedPermissions.push(res[j]);
        }
        this.groupNames = this.permissionsListData[i]['groupNameList'];

        this.groupIndex = this.groupNames.findIndex((x: any) => {
          return x.authority.permissionName === res[j];
        });

        if (this.groupIndex != -1) {
          this.permissionsListData[i]['groupNameList'][
            this.groupIndex
          ].isGroupChecked = true;

          let groupName = this.permissionsListData[i]['groupNameList'][
            this.groupIndex
          ].groupName;
          this.selectedGroups[groupName] = true;
          this.authorizedPermissions.push(res[j]);
        }
        if (this.permissionsListData[i]['groupNameList'].length != 0) {
          for (let g = 0; g < this.groupNames.length; g++) {
            this.labelNames = this.permissionsListData[i]['groupNameList'][
              g
            ].labels;
            const labelIndex = this.labelNames.findIndex((x: any) => {
              return x.authority.permissionName == res[j];
            });
            if (labelIndex != -1) {
              this.permissionsListData[i]['groupNameList'][g]['labels'][
                labelIndex
              ].isLabelChecked = true;
              const labelName = this.permissionsListData[i]['groupNameList'][g][
                'labels'
              ][labelIndex].labelName;
              this.authorizedPermissions.push(res[j]);
              this.selectedLabels[labelName] = true;
            }
          }
        }
      }
    }
    this.selectedPermissionsData = this.permissionsListData;
  }

  createRole() {
    this.submitted = true;
    if (this.roleForm.invalid) {
      this.isPermissions = true;
      return;
    }
    const payload = {
      roleName: this.roleName,
      moduleType: this.scopeType,
      deductorPan: this.deductorPan,
      permissionNames: this.authorizedPermissions
    };
    this.rolePermissionService.createRolePermission(payload).subscribe(
      (res: any) => {
        window.scroll(0, 0);
        this.closeRoles.emit(false);
      },
      (error: any) => {}
    );
  }

  onPermissionSelect(
    event: any,
    permission: any,
    value: any,
    tabIndex: any,
    uncheck: any,
    groupIndex: any,
    labelIndex: any
  ) {
    if (uncheck == 'tabtrue') {
      this.tabUncheck(event.target.checked, tabIndex, groupIndex);
    }
    if (uncheck == 'grouptrue') {
      this.groupUncheck(event.target.checked, tabIndex, groupIndex);
    }
    if (event.target.checked) {
      this.authorizedPermissions.push(value);
      this.selectedTabName = permission;
    } else {
      const index = this.authorizedPermissions.findIndex((x: any) => {
        return x === value;
      });
      if (index != -1) {
        this.authorizedPermissions.splice(index, 1);
      }
    }
    if (labelIndex != undefined) {
      this.checkViewPermission(event, tabIndex, groupIndex);
    }
  }

  checkViewPermission(event: any, tabIndex: any, groupIndex: any) {
    if (event.target.checked) {
      let labelsList = this.permissionsListData[tabIndex].groupNameList[
        groupIndex
      ].labels;
      const labelIndex = labelsList.findIndex((x: any) => {
        return x.authority.permissionDisplayName === 'List';
      });
      const labelName = labelsList[labelIndex].labelName;
      this.selectedLabels[labelName] = true;
      this.selectedPermissionsData[tabIndex].groupNameList[groupIndex].labels[
        labelIndex
      ].isLabelChecked = true;
      this.authorizedPermissions.push(
        this.selectedPermissionsData[tabIndex].groupNameList[groupIndex].labels[
          labelIndex
        ].authority.permissionName
      );
    }
  }

  tabUncheck(checked: any, tabIndex: any, groupIndex: any): void {
    if (checked === false) {
      let groupList = this.permissionsListData[tabIndex].groupNameList;
      for (let i = 0; i < groupList.length; i++) {
        let groupName = groupList[i].groupName;
        const groupIndex = this.authorizedPermissions.findIndex((x: any) => {
          return x === groupList[i].authority.permissionName;
        });
        this.authorizedPermissions.splice(groupIndex, 1);
        this.selectedGroups[groupName] = false;
        let lablesList = groupList[i].labels;
        for (let j = 0; j < lablesList.length; j++) {
          let labelName = lablesList[j].labelName;
          this.selectedLabels[labelName] = false;
          const labelIndex = this.authorizedPermissions.findIndex((x: any) => {
            return x === lablesList[j].authority.permissionName;
          });
          this.authorizedPermissions.splice(labelIndex, 1);
        }
      }
    }
  }

  groupUncheck(checked: any, tabIndex: any, groupIndex: any): void {
    if (checked === false) {
      let labelList = this.permissionsListData[tabIndex].groupNameList[
        groupIndex
      ].labels;
      for (let i = 0; i < labelList.length; i++) {
        let labelName = labelList[i].labelName;
        this.selectedLabels[labelName] = false;
        const labelIndex = this.authorizedPermissions.findIndex((x: any) => {
          return x === labelList[i].authority.permissionName;
        });
        this.authorizedPermissions.splice(labelIndex, 1);
      }
    }
  }

  hideForm(): void {
    this.listHandler.emit(false);
  }
}
