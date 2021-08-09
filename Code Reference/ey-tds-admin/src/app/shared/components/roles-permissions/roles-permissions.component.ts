import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { UserService } from '@app/shared/components/users/user.service';
import { Router } from '@angular/router';
import { CustomLoggerService } from '@app/shared/services/custom-logger.service';
import { StorageService } from '@app/shell/authentication/storageservice';

@Component({
  selector: 'ey-roles-permissions',
  templateUrl: './roles-permissions.component.html',
  styleUrls: ['./roles-permissions.component.scss']
})
export class RolesPermissionsComponent implements OnInit {
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
    private logger: CustomLoggerService
  ) {
    this.closePopup = new EventEmitter<boolean>();
    this.manageForm = new EventEmitter<any>();
  }

  ngOnInit() {
    console.log(this.scopeType);
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
    this.userService.getRolesByPan(this.deductorPAN).subscribe(
      (res: any) => {
        this.rolesData = res.data;
      },
      (error: any) => {}
    );
  }

  showForm(action: string, id?: string) {
    console.log({
      actionType: action,
      roleId: id ? id : undefined,
      toggler: true
    });
    this.manageForm.emit({
      actionType: action,
      roleId: id ? id : undefined,
      toggler: true,
      scopeType: '1'
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
