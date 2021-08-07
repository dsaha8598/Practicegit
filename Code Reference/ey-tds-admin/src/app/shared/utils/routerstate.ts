import {
  ActivatedRoute,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlSegment
} from '@angular/router';
import { Observable } from 'rxjs';

export interface IRouterState {
  snapshot: RouterStateSnapshot; // returns current snapshot

  root: ActivatedRoute;
}

export interface IActivatedRoute {
  snapshot: ActivatedRouteSnapshot; // returns current snapshot

  url: Observable<UrlSegment[]>;
  params: Observable<{ [name: string]: string }>;
  data: Observable<{ [name: string]: any }>;

  queryParams: Observable<{ [name: string]: string }>;
  fragment: Observable<string>;

  root: ActivatedRoute;
  parent: ActivatedRoute;
  firstchild: ActivatedRoute;
  children: ActivatedRoute[];
}
