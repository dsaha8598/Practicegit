// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

// `.env.ts` is generated by the `npm run env` command
import env from './.env';
export const protectedResourceMap: [string, string[]][] = [
  ['https://graph.microsoft.com/v1.0/me', ['user.read']]
];
export const environment = {
  production: false,
  version: env.npm_package_version + '-dev',
  serverUrl: '/api',
  defaultLanguage: 'en-US',
  supportedLanguages: ['en-US', 'fr-FR'],
  config: {
    clientID: '375cdaa7-bfb8-4789-9a8c-431c023e6f39',
    authority:
      'https://login.microsoftonline.com/68993871-e3b7-415e-88ea-30d7f913d421/',
    validateAuthority: true,
    redirectUri: 'https://tfo-uattds02admin.eyasp.in/',
    postLogoutRedirectUri: 'https://tfo-uattds02admin.eyasp.in/',
    navigateToLoginRequestUrl: true,
    cacheLocation: 'localStorage',
    consentScopes: [
      'user.read',
      'openid',
      'profile',
      'api://a88bb933-319c-41b5-9f04-eff36d985612/access_as_user'
    ],
    unprotectedResources: ['https://www.microsoft.com/en-us/'],
    protectedResourceMap
  },
  LOGIN_RESOURCE_URL: 'https://login.microsoftonline.com',
  api: {
    authorization: '/api/authorization/',
    administration: '/api/administration/',
    challans: '/api/challans/',
    dashboards: '/api/dashboards/',
    masters: '/api/masters/',
    ingestion: '/api/ingestion/',
    onboarding: '/api/onboarding/',
    rateengine: '/api/rateengine/',
    returns: '/api/returns/',
    auth: '/api/auth/',
    admin: '/api/admin/',
    fvu: '/api/fvu-proxy/generate',
    reports: '/api/reports/',
    validation: '/api/validation/',
    zuulgatway: '/api/zuulgatway/',
    logger: '/api/ui-logger/log',
    panwebsocket: 'wss://tfo-uattds02admin.eyasp.in/ws',
    ldcwebsocket: 'wss://tfo-uattds02admin.eyasp.in//ws-ldc',
    singlepanwebsocket: 'wss://tfo-uattds02admin.eyasp.in//ws-singlepan',
    singleldcwebsocket: 'wss://tfo-uattds02admin.eyasp.in//ws-singleldc',
    filingwebsocket: 'wss://tfo-uattds02admin.eyasp.in//ws-filing',
    utilizationwebsocket: 'wss://tfo-uattds02admin.eyasp.in//ws-utilization',
    csiwebsocket: 'wss://tfo-uattds02admin.eyasp.in//csi',
    consolefilewebsocket: 'wss://tfo-uattds02admin.eyasp.in/consolesubmission',
    consoledownload: 'wss://tfo-uattds02admin.eyasp.in/consoledownload',
    consolesubmission: 'wss://tfo-uattds02admin.eyasp.in/consolesubmission',
    form16download: 'wss://tfo-uattds02admin.eyasp.in/form16download',
    form16submission: 'wss://tfo-uattds02admin.eyasp.in/form16submission',
    justificationdownload:
      'wss://tfo-uattds02admin.eyasp.in/justificationdownload',
    justificationsubmission:
      'wss://tfo-uattds02admin.eyasp.in/justificationsubmission',
    residentsectionDetermination: '/api/flask/resident/section',
    nonResidentSectionDetermination: '/api/flask/non-resident/section',
    afterSectionDetermination: '/api/flask/invoice/tracking',
    flaskapi: 'api/flask',
    sparkapi: 'api/spark'
  },
  tenantConfigTypes: {
    databaseConfig: 'cosmos-config.',
    sftpConfig: 'sftp-config.',
    blobConfig: 'blob-storage.',
    powerbiConfig: 'power-bi.'
  },
  logger: {
    debug: {
      enabled: false
    },
    info: {
      enabled: false
    },
    warn: {
      enabled: true
    },
    error: {
      enabled: true
    }
  }
};