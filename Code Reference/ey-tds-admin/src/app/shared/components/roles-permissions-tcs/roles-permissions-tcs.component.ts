import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { UserService } from '@app/shared/components/users/user.service';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { StorageService } from '@app/shell/authentication/storageservice';
import { RolesPermissionsTcsService } from './roles-permissions-tcs.service';

@Component({
  selector: 'ey-roles-permissions-tcs',
  templateUrl: './roles-permissions-tcs.component.html',
  styleUrls: ['./roles-permissions-tcs.component.scss']
})
export class RolesPermissionsTcsComponent implements OnInit {
  rolesData: any = [];
  cols: any = [];
  deductorGroupList: any = [];
  selectedColumns: any = [];
  @Input() deductorPAN: string;
  @Input() scopeType: string;
  @Output() closePopup: EventEmitter<boolean>;
  @Output() manageForm: EventEmitter<any>;
  constructor(
    private userService: UserService,
    private router: Router,
    private storageService: StorageService,
    private logger: CustomLoggerService,
    private rolesPermissionTcsServices: RolesPermissionsTcsService
  ) {
    console.log(this.deductorPAN);
    this.closePopup = new EventEmitter<boolean>();
    this.manageForm = new EventEmitter<any>();
  }

  ngOnInit() {
    this.selectedColumns = [
      {
        field: 'roleName',
        header: 'Role Name',
        width: '300px',
        type: 'initial'
      }
    ];
    this.cols = this.selectedColumns;
    this.getRoles();
  }

  getRoles() {
    this.deductorPAN;
    this.rolesPermissionTcsServices.getRolesByPan(this.deductorPAN).subscribe(
      (res: any) => {
        this.rolesData = res.data;
      },
      (error: any) => {}
    );
  }

  showForm(action: string, id?: string) {
    this.manageForm.emit({
      actionType: action,
      roleId: id ? id : undefined,
      toggler: true,
      scopeType: '2'
    });
    this.storageService.setItem('roleId', id);
  }

  backClick(): void {
    this.router
      .navigate(['/dashboard/masters'])
      .then(val => this.logger.debug('Navigate: ' + val))
      .catch(error => console.error(error));
  }
}
