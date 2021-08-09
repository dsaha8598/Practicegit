import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Params,
  PRIMARY_OUTLET,
  Router
} from '@angular/router';
import { filter } from 'rxjs/operators';
import { IBreadcrumb } from './breadcrumbs.model';
import { BreadcrumbsService } from './breadcrumbs.service';
import { RouteaccessService } from '@app/shell/authentication/routeaccess.service';
import { AuthenticationService } from '@app/shell/authentication/authentication.service';
import { StorageService } from '@app/shell/authentication/storageservice';
//import { ManageTracesCredentialsService } from '@app/masters/manage-traces-credentials/manage-traces-credentials.service';
@Component({
  selector: 'ey-breadcrumbs',
  templateUrl: 'breadcrumbs.component.html',
  styleUrls: ['breadcrumbs.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [BreadcrumbsService]
})
export class BreadcrumbsComponent implements OnInit {
  // All the breadcrumbs
  breadcrumbs: Array<IBreadcrumb>;

  @Input() allowBootstrap: boolean;

  @Input() addClass: string;
  private ROUTE_DATA_BREADCRUMB = 'breadcrumb';
  private ROUTE_PARAM_BREADCRUMB = 'breadcrumb';
  private PREFIX_BREADCRUMB = 'prefixBreadcrumb';

  // The breadcrumbs of the current route
  private currentBreadcrumbs: Array<IBreadcrumb>;
  isToggle: boolean;
  moduleScopValue: string;
  moduleSelected: string;
  scopeList: any = [];
  isTcsModule: boolean;
  isTdsModule: boolean;
  selectedPan: any;

  constructor(
    private breadcrumbService: BreadcrumbsService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private readonly routeAccessService: RouteaccessService,
    private readonly authenticationService: AuthenticationService,
    private readonly storageService: StorageService /* ,
    private readonly mctService: ManageTracesCredentialsService */
  ) {
    breadcrumbService.get().subscribe((breadcrumbs: Array<IBreadcrumb>) => {
      this.breadcrumbs = breadcrumbs as Array<IBreadcrumb>;
    });
  }

  isSuperAdmin(): boolean {
    return this.routeAccessService.hasAccess(['SUPER ADMIN']);
  }

  get showTanHandler(): boolean {
    return this.router.url !== '/dashboard/dashboards';
  }

  hasParams(breadcrumb: IBreadcrumb) {
    return Object.keys(breadcrumb.params).length
      ? [breadcrumb.url, breadcrumb.params]
      : [breadcrumb.url];
  }

  ngOnInit() {
    if (this.router.navigated) {
      this.generateBreadcrumbTrail();
    }

    // subscribe to the NavigationEnd event
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(event => {
        this.generateBreadcrumbTrail();
      });
  }

  getScopeDetails(pan: string): void {
    /*  this.authenticationService.getScopeModule(pan.slice(0, 10)).subscribe(
      (res: any) => {
        this.scopeList = res.data;
        if (res.data.hasTds === true && res.data.hasTcs === false) {
          this.moduleSelected = 'TDS';
          this.storageService.setItem('moduleScopeSelected', 'TDS');
        } else if (res.data.hasTds === false && res.data.hasTcs === true) {
          this.moduleSelected = 'TCS';
          this.storageService.setItem('moduleScopeSelected', 'TCS');
        } else if (res.data.hasTds === true && res.data.hasTcs === true) {
        }
      },
      (err: any) => {
        console.log(err, 'errorHandler(err)');
        this.errorHandler();
      }
    ); */
  }
  errorHandler(): void {
    this.router
      .navigate(['/unauthorized'])
      .then()
      .catch();
  }

  getOnlyPANFromDetails(data: string): string {
    return data.split(' - ')[0];
  }

  getOnlyTANFromDetails(data: string): string {
    return data.split(' - ')[0];
  }

  private generateBreadcrumbTrail() {
    // reset currentBreadcrumbs
    this.currentBreadcrumbs = [];

    // get the root of the current route
    let currentRoute: ActivatedRoute = this.activatedRoute.root;

    // set the url to an empty string
    let url = '';

    // iterate from activated route to children
    while (currentRoute.children.length > 0) {
      const childrenRoutes: Array<ActivatedRoute> = currentRoute.children;
      let breadCrumbLabel = '';

      // iterate over each children
      childrenRoutes.forEach(route => {
        // Set currentRoute to this route
        currentRoute = route;
        // Verify this is the primary route
        if (route.outlet !== PRIMARY_OUTLET) {
          return;
        }
        const hasData = route.routeConfig && route.routeConfig.data;
        const hasDynamicBreadcrumb: boolean = route.snapshot.params.hasOwnProperty(
          this.ROUTE_PARAM_BREADCRUMB
        );

        if (hasData || hasDynamicBreadcrumb) {
          /*
          Verify the custom data property "breadcrumb"
          is specified on the route or in its parameters.
          Route parameters take precedence over route data
          attributes.
          */
          if (hasDynamicBreadcrumb) {
            breadCrumbLabel = route.snapshot.params[
              this.ROUTE_PARAM_BREADCRUMB
            ].replace(/_/g, ' ');
          } else if (
            route.snapshot.data.hasOwnProperty(this.ROUTE_DATA_BREADCRUMB)
          ) {
            breadCrumbLabel = route.snapshot.data[this.ROUTE_DATA_BREADCRUMB];
          }
          // Get the route's URL segment
          const routeURL: string = route.snapshot.url
            .map(segment => segment.path)
            .join('/');
          url += `/${routeURL}`;
          // Cannot have parameters on a root route
          if (routeURL.length === 0) {
            route.snapshot.params = {};
          }
          // Add breadcrumb
          const breadcrumb: IBreadcrumb = {
            label: breadCrumbLabel,
            params: route.snapshot.params,
            url
          };
          // Add the breadcrumb as 'prefixed'. It will appear before all breadcrumbs
          if (route.snapshot.data.hasOwnProperty(this.PREFIX_BREADCRUMB)) {
            this.breadcrumbService.storePrefixed(breadcrumb);
          } else {
            this.currentBreadcrumbs.push(breadcrumb);
          }
        }
      });
      this.breadcrumbService.store(this.currentBreadcrumbs);
    }
  }
}
