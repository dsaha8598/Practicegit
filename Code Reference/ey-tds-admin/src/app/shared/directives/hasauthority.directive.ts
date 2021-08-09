import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { RouteaccessService } from '@app/shell/authentication/routeaccess.service';

@Directive({
  selector: '[HasAnyAuthority]'
})
export class HasAnyAuthorityDirective {
  private authorities: string[];

  constructor(
    private readonly templateRef: TemplateRef<any>,
    private readonly viewContainerRef: ViewContainerRef,
    private readonly routeAccessService: RouteaccessService
  ) {}

  @Input()
  set HasAnyAuthority(value: string | Array<string>) {
    this.authorities = typeof value === 'string' ? [value] : value;

    if (this.routeAccessService.hasAccess(this.authorities)) {
      this.updateView(true);
    }
  }

  private updateView(hasAccess: boolean): void {
    this.viewContainerRef.clear();
    if (hasAccess) {
      this.viewContainerRef.createEmbeddedView(this.templateRef);
    }
  }
}
