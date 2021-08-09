import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class ConfigAssetLoaderService {
  private readonly CONFIG_URL = 'assets/config/configuration.json';
  public configuration$: any;
  public API_URLs: any;

  constructor(private http: HttpClient) {}

  public loadConfigurations(): any {
    this.http.get<any>(this.CONFIG_URL).subscribe(
      (res: any) => {
        this.setConfigurations(res);
      },
      error => {}
    );
  }

  loadWebSockets() {
    environment.api.panwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws';
    environment.api.ldcwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws-ldc';
    environment.api.singlepanwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws-singlepan';
    environment.api.singleldcwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws-singleldc';
    environment.api.filingwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws-filing';
    environment.api.utilizationwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/ws-utilization';
    environment.api.csiwebsocket =
      'wss://' + localStorage.getItem('hostname') + '/csi';
    environment.api.consolefilewebsocket =
      'wss://' + localStorage.getItem('hostname') + '/consolesubmission';
    environment.api.consoledownload =
      'wss://' + localStorage.getItem('hostname') + '/consoledownload';
    environment.api.consolesubmission =
      'wss://' + localStorage.getItem('hostname') + '/consolesubmission';
    environment.api.form16download =
      'wss://' + localStorage.getItem('hostname') + '/form16download';
    environment.api.form16submission =
      'wss://' + localStorage.getItem('hostname') + '/form16submission';
    environment.api.justificationdownload =
      'wss://' + localStorage.getItem('hostname') + '/justificationdownload';
    environment.api.justificationsubmission =
      'wss://' + localStorage.getItem('hostname') + '/justificationsubmission';
  }

  setConfigurations(configurations: any) {
    this.configuration$ = configurations;
    this.API_URLs = this.configuration$.api;
  }

  getConfigurations() {
    return this.configuration$;
  }

  getApiUrls() {
    return this.API_URLs;
  }
}
