import { Component, OnInit, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { UserService } from './user.service';
import { StorageService } from '@app/shell/authentication/storageservice';
import { IUser, User, AccessDetails, TanAccess } from './user.model';
import { AlertService } from '../alert/alert.service';

@Component({
  selector: 'ey-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {
  showModal = false;
  selectedColumns: any;
  cols: any;
  userForm: FormGroup;
  selectedTansRolesData: any = [];
  selectedTansData: any = [];
  selectedRolesData: any = [];
  selectedRolesDataTcs: any = [];
  roleTansData: any = [];
  selectedrole: any = [];
  selectedTanIdsData: any = [];
  rolesData: any = [];
  selectedTDSRoles: any = [];
  role: any;
  tansData: any = [];
  isAllTansSelected: boolean;
  submitted = false;
  selectedRole: any = [];
  usersData: any = [];
  formType: any;
  username: string;
  email: string;
  tenantId: string;
  filtedRolesData: any = [];
  dropdownSettings = {};
  userId: any;
  roleName: any;
  roleSelected: any = [];
  roleNameTcs: any;
  roleSelectedTcs: any = [];
  entityData: any;
  @Input() deductorPan: string;
  userAccessHandler: boolean;
  tanValue: string;
  userAccessDetailsObject: IUser;
  userAccessDetailsRequired: any;
  isEmailEmpty: boolean;
  isUserNameEmpty: boolean;
  isAllPansSelected: boolean;
  selectedTansListData: any = [];
  selectedTCSRolesListData: any = [];
  selectedTDSRolesListData: any = [];
  selectedTCSTansListData: any = [];
  selectedTDSTansListData: any = [];
  userCreationObj: any = {
    userId: '',
    deductorPans: [],
    email: '',
    username: '',
    userAccessDetails: []
  };
  constructor(
    private userService: UserService,
    private storageService: StorageService,
    private readonly alertService: AlertService
  ) {}

  ngOnInit() {
    this.initialLoading();
  }

  initialLoading(): void {
    this.cols = [
      {
        field: 'userUsername',
        header: 'User name',
        width: '200px',
        type: 'initial'
      },
      {
        field: 'userEmail',
        header: 'Email',
        width: '400px'
      },
      {
        field: 'action',
        header: 'Action',
        width: '200px'
      }
    ];
    this.selectedColumns = this.cols;
    this.userAccessDetailsObject = new User();
    this.userAccessDetailsObject.userAccessDetails = []; //userAcessDetails is a arry
    this.getUsers(); //calling function to get users
    this.tenantId = this.storageService.getItem('tenantId');
  }

  getEntitiesData(type: any, data: any): void {
    this.userService.getAllDeductorTanAndRoles().subscribe(
      (res: any) => {
        this.entityData = res.data;
        this.buildForm(this.entityData);
        this.userAccessHandler = true;
      },
      (err: any) => {
        console.error(err);
      }
    );
  }

  openModal(type: string, data?: any): any {
    this.resetUserObject();
    this.selectedRolesData = [];
    this.selectedRolesDataTcs = [];
    this.formType = type;
    this.submitted = false;
    this.showModal = true;
    this.getEntitiesData(type, data);
    if (type.toUpperCase() !== 'ADD') {
      this.userService.getUserDetails(data.userId).subscribe(
        (res: any) => {
          this.bindUserData(res);
        },
        (error: any) => {}
      );
    }
  }

  checkPanExistancy(pan: any) {
    const panIndex = this.userCreationObj.userAccessDetails.findIndex(
      (x: any) => x.pan == pan
    );
    if (panIndex != -1) {
      return true;
    }
    return false;
  }

  checkTanExistancy(tan: any) {
    const tanIndex = this.selectedTansListData.findIndex((x: any) => x == tan);
    if (tanIndex != -1) {
      return true;
    }
    return false;
  }

  bindUserData(res: any) {
    this.selectedTansListData = [];
    this.userCreationObj.userId = res.data.userId;
    this.userCreationObj.username = res.data.userUsername;
    this.userCreationObj.email = res.data.userEmail.substring(
      0,
      res.data.userEmail.indexOf('@')
    );
    this.userAccessDetailsObject.userId = res.data.userId;
    for (let i = 0; i < res.data.userAccessDetails.length; i++) {
      this.onPanSelection(true, res.data.userAccessDetails[i].pan);
      this.selectedTansListData.push(res.data.userAccessDetails[i].tan);
      if (res.data.userAccessDetails[i].moduleType == 'TCS') {
        this.onTCSRoleSelection(
          res.data.userAccessDetails[i].pan,
          res.data.userAccessDetails[i].tan,
          res.data.userAccessDetails[i].roleId
        );
        this.selectedTCSRolesListData.push({
          pan: res.data.userAccessDetails[i].pan,
          tan: res.data.userAccessDetails[i].tan,
          roleId: res.data.userAccessDetails[i].roleId
        });
      } else {
        this.onTDSRoleSelection(
          res.data.userAccessDetails[i].pan,
          res.data.userAccessDetails[i].tan,
          res.data.userAccessDetails[i].roleId
        );
        this.selectedTDSRolesListData.push({
          pan: res.data.userAccessDetails[i].pan,
          tan: res.data.userAccessDetails[i].tan,
          roleId: res.data.userAccessDetails[i].roleId
        });
      }
    }
  }

  checkTCSRoleExistancy(pan: any, tan: any) {
    const roleIndex = this.selectedTCSRolesListData.findIndex(
      (x: any) => x.pan == pan && x.tan == tan
    );
    if (roleIndex != -1) {
      return this.selectedTCSRolesListData[roleIndex].roleId;
    }
  }

  checkTDSRoleExistancy(pan: any, tan: any) {
    const roleIndex = this.selectedTDSRolesListData.findIndex(
      (x: any) => x.pan == pan && x.tan == tan
    );
    if (roleIndex != -1) {
      return this.selectedTDSRolesListData[roleIndex].roleId;
    }
  }

  onPanSelection(checked: any, value: any) {
    if (checked) {
      const index = this.userCreationObj.deductorPans.findIndex(
        (x: any) => x == value
      );
      if (index == -1) {
        this.userCreationObj.deductorPans.push(value);
        this.userCreationObj.userAccessDetails.push({
          pan: value,
          userTanLevelAccess: []
        });
      }
    } else {
      const deductorPanIndex = this.userCreationObj.deductorPans.findIndex(
        (x: any) => x == value
      );
      this.userCreationObj.deductorPans.splice(deductorPanIndex, 1);
      const panTanLevelIndex = this.userCreationObj.userAccessDetails.findIndex(
        (x: any) => x.pan == value
      );
      this.userCreationObj.userAccessDetails.splice(panTanLevelIndex, 1);
    }
  }

  onTanSelection(pan: any, tan: any, event: any) {
    const panIndex = this.userCreationObj.userAccessDetails.findIndex(
      (x: any) => x.pan == pan
    );
    if (!event.target.checked) {
      if (panIndex != -1) {
        const tanIndex = this.userCreationObj.userAccessDetails[
          panIndex
        ].userTanLevelAccess.findIndex(
          (x: any) => x[tan] == event.target.value
        );
        if (tanIndex != -1) {
          this.userCreationObj.userAccessDetails[
            panIndex
          ].userTanLevelAccess.splice(tanIndex, 1);
        }
      } else {
        return false;
      }
    } else {
      if (panIndex == -1) {
        event.preventDefault();
      }
    }
  }

  onTDSRoleSelection(pan: any, tan: any, value: any) {
    const panIndex = this.userCreationObj.userAccessDetails.findIndex(
      (x: any) => x.pan == pan
    );
    if (panIndex != -1) {
      let tanIndex = this.selectedTDSTansListData.findIndex(
        (x: any) => x == tan
      );
      if (tanIndex == -1) {
        this.selectedTDSTansListData.push(tan);
        this.userCreationObj.userAccessDetails[
          panIndex
        ].userTanLevelAccess.push({ [tan]: +value });
      } else {
        this.userCreationObj.userAccessDetails[panIndex].userTanLevelAccess[
          tanIndex
        ][this.selectedTDSTansListData[tanIndex]] = +value;
      }
      this.selectedTansListData.push(tan);
    }
  }

  onTCSRoleSelection(pan: any, tan: any, value: any) {
    const panIndex = this.userCreationObj.userAccessDetails.findIndex(
      (x: any) => x.pan == pan
    );
    if (panIndex != -1) {
      let tanIndex = this.selectedTCSTansListData.findIndex(
        (x: any) => x == tan
      );
      if (tanIndex == -1) {
        this.selectedTCSTansListData.push(tan);
        this.userCreationObj.userAccessDetails[
          panIndex
        ].userTanLevelAccess.push({ [tan]: +value });
      } else {
        this.userCreationObj.userAccessDetails[panIndex].userTanLevelAccess[
          tanIndex
        ][this.selectedTCSTansListData[tanIndex]] = +value;
      }
      this.selectedTansListData.push(tan);
    }
  }

  /* bindData(data: any): void {
    console.log(data);
  } */

  get entities(): any {
    if (this.entityData) {
      return Object.keys(this.entityData);
    }
  }

  buildForm(data: any): void {
    Object.keys(data).map((each, index) => {
      this.userAccessDetailsObject.userAccessDetails.push(new AccessDetails());
      this.userAccessDetailsObject.userAccessDetails[
        index
      ].userTanLevelAccess = [];
      data[each].tans.map((eachTan: any) => {
        this.userAccessDetailsObject.userAccessDetails[
          index
        ].userTanLevelAccess.push(new TanAccess());
      });
    });
  }

  createUser(): void {
    if (this.userCreationObj.username && this.userCreationObj.email) {
      this.selectedTDSRoles;
      this.userCreationObj.email = `${this.userCreationObj.email}@${this.tenantId}`;
      // this.userAccessDetailsRequired = new Object();
      // this.userAccessDetailsRequired.deductorPans = [];
      // this.userAccessDetailsRequired.email = `${this.userAccessDetailsObject.email}@${this.tenantId}`;
      // this.userAccessDetailsRequired.username = this.userAccessDetailsObject.username;
      // this.userAccessDetailsRequired.userId = this.userAccessDetailsObject.userId;
      // this.userAccessDetailsRequired.userAccessDetails = [];

      // this.userAccessDetailsObject.userAccessDetails.map(each => {
      //   const panObj = new AccessDetails();
      //   if (each.pan) {
      //     panObj.pan = each.pan;
      //     panObj.userTanLevelAccess = [];
      //     this.userAccessDetailsRequired.deductorPans.push(each.pan);
      //     each.userTanLevelAccess.map(eachObj => {
      //       if (eachObj.tan) {
      //         if (eachObj.roleId) {
      //           const tanObj = new Object();
      //           tanObj[eachObj.tan] = eachObj.roleId;
      //           panObj.userTanLevelAccess.push(tanObj);
      //         }
      //         setTimeout(() => {
      //           if (eachObj.roleIdTcs) {
      //             const tanObjTcs = new Object();
      //             tanObjTcs[eachObj.tan] = eachObj.roleIdTcs;
      //             panObj.userTanLevelAccess.push(tanObjTcs);
      //           }
      //         }, 500);
      //       }
      //     });
      //     this.userAccessDetailsRequired.userAccessDetails.push(panObj);
      //   }
      // });

      console.log('printing the user obj', this.userCreationObj);
      if (
        this.userCreationObj.deductorPans.length > 0 &&
        this.userCreationObj.userAccessDetails.length > 0
      ) {
        this.userService.createUser(this.userCreationObj).subscribe(
          (res: any) => {
            this.showModal = false;
            // this.editModal = false;
            this.getUsers();
            this.reset(this.userAccessDetailsObject);
          },
          error => {
            this.reset(this.userAccessDetailsObject);
            this.alertService.info(error.error.message);
            this.getUsers();
            this.showModal = false;
          }
        );
      } else {
        this.alertService.error(
          'Please select atleast one PAN and respective roles for the TAN.'
        );
        setTimeout(() => {
          this.alertService.clear();
        }, 5000);
      }
    } else {
      if (!this.userAccessDetailsObject.email) {
        this.isEmailEmpty = true;
      }

      if (!this.userAccessDetailsObject.username) {
        this.isUserNameEmpty = true;
      }
    }
  }

  reset(data: any): void {
    data.username = '';
    data.email = '';
    for (let i = 0; i < data.userAccessDetails.length; i++) {
      data.userAccessDetails[i].pan = false;
    }
  }

  resetUserObject() {
    this.userCreationObj = {
      deductorPans: [],
      email: '',
      username: '',
      userAccessDetails: []
    };
    this.selectedTansListData = [];
    this.selectedTCSRolesListData = [];
    this.selectedTDSRolesListData = [];
    this.selectedTCSTansListData = [];
    this.selectedTDSTansListData = [];
  }

  userNameErrorHandler(): void {
    this.isUserNameEmpty = false;
  }

  emailErrorHandler(): void {
    this.isEmailEmpty = false;
  }

  //Gets the list of all created users
  getUsers(): void {
    this.userService.getUsers(this.deductorPan).subscribe(
      (res: any) => {
        this.usersData = res.data;
      },
      error => {}
    );
  }
}
