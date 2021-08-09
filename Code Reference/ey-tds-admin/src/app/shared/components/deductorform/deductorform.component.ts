import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  Validators,
  FormControl,
  ValidatorFn
} from '@angular/forms';
import { Router } from '@angular/router';
import { UtilModule } from '@app/shared';
import { IDeductor } from '@app/shared/model/deductor.model';
import { MenuItem } from 'primeng/api';
import { DeductorService } from '../deductor/deductor.service';
@Component({
  selector: 'ey-deductorform',
  templateUrl: './deductorform.component.html',
  styleUrls: ['./deductorform.component.scss']
})
export class DeductorformComponent implements OnInit {
  resultIdData: any = '';
  applicableToDateError: boolean;
  gstinField: any;
  get f(): any {
    return this.deductorForm.controls;
  }
  deductorForm: FormGroup;
  submitted = false;
  tanListData: any;
  doesDeductorHasMorethanOne = false;
  actionState: string;
  residentialstatuslist: any;
  totalStatus: any;
  getresidentialstatuserror: any;
  resideductortyprlist: any;
  resideductortyprlisterror: boolean;
  getDividendDeductorlist: any;
  getDividendDeductorlisterror: boolean;
  modeofpaymentlist: any;
  modeofpaymentlisterror: boolean;
  statuslist: any;
  statuslisterror: boolean;
  allstates: any;
  allcountries: any;
  getalldeductors: any;
  getalldeductorserror: boolean;
  deductorFormData: any;
  deductorPan: string;
  submittedTo: boolean = false;
  minDate: any = undefined;
  headingMsg: string;
  stateFilterList: Array<object>;
  stateList: any;
  responsiblePersonDetails: any = [];
  pinCodeNumber: number;
  states: any = [];
  cities: any;
  statesListResult: any;
  allcities: any = [];
  data: any;
  selectedCountry: string;
  @Input() deductorInputId: string;
  @Input() action: string;
  @Output() closePopup: EventEmitter<boolean>;
  actionHandler: string;
  deductorFormValues: any;
  activeIndex: number = 0;
  scopeData = [
    { label: 'TDS configuration', value: 'TDS' },
    { label: 'TCS configuration', value: 'TCS' }
  ];
  selectedModules: any = [];
  checkboxGroup: any;
  hiddenControl: any;
  items: MenuItem[];
  selectedColumns: Array<object>;
  tanList: any;
  constructor(
    private readonly formBuilder: FormBuilder,
    //private readonly actRoute: ActivatedRoute,
    private readonly deductorService: DeductorService,
    private readonly router: Router
  ) {
    this.closePopup = new EventEmitter<boolean>();
  }
  ngOnInit(): void {
    this.initialLoading();
    this.actionHandler = this.action;
    this.selectedColumns = [
      {
        header: 'Tan',
        field: 'tan',
        width: '100px'
      },
      {
        field: 'flatDoorBlockNo',
        header: 'Flat/Door/Block No',
        width: '200px'
      },
      {
        field: 'nameBuildingVillage',
        header: 'Name of building/village',
        width: '200px'
      },
      {
        field: 'roadStreetPostoffice',
        header: 'Road/Street/Post office',
        width: '200px'
      },
      {
        field: 'areaLocality',
        header: 'Area/Locality',
        width: '150px'
      },
      {
        field: 'stateName',
        header: 'State',
        width: '150px'
      },
      {
        field: 'countryName',
        header: 'Country',
        width: '150px'
      },
      {
        header: 'Town/City/District',
        field: 'townCityDistrict',
        width: '200px'
      },
      {
        field: 'pinCode',
        header: 'Pin code',
        width: '100px'
      },
      {
        field: 'stdCode',
        header: 'STD Code',
        width: '100px'
      },
      {
        field: 'name',
        header: 'Name',
        width: '150px'
      },
      {
        field: 'pan',
        header: 'Pan',
        width: '100px'
      },
      {
        field: 'designation',
        header: 'Designation',
        width: '150px'
      },
      {
        header: 'Flat/Door/Block no',
        field: 'personflatDoorBlockNo',
        width: '200px'
      },

      {
        header: 'Name of building/village',
        field: 'buildingName',
        width: '200px'
      },
      {
        field: 'streetName',
        header: 'Street',
        width: '100px'
      },
      {
        field: 'area',
        header: 'Area',
        width: '100px'
      },
      {
        field: 'city',
        header: 'City',
        width: '100px'
      },
      {
        field: 'state',
        header: 'State',
        width: '100px'
      },
      {
        field: 'personpinCode',
        header: 'Pin code',
        width: '130px'
      },
      {
        field: 'personstdCode',
        header: 'STD Code',
        width: '130px'
      },
      {
        field: 'telephone',
        header: 'Telephone',
        width: '130px'
      },
      {
        field: 'alternateTelephone',
        header: 'Alternate telephone',
        width: '200px'
      },
      {
        field: 'mobilenumber',
        header: 'Mobile number',
        width: '200px'
      },
      {
        field: 'email',
        header: 'Email',
        width: '200px'
      },
      {
        field: 'alternateEmail',
        header: 'Alternate email',
        width: '200px'
      },
      {
        field: 'personAddressChange',
        header: 'Address change',
        width: '200px'
      },
      {
        field: 'dvndOptedFor15CaCb',
        header: 'Dividend-Opt-In for form 15CA/CB?',
        width: '200px'
      },
      {
        field: 'Principal area of business',
        header: 'dvndPrincipalAreaOfBusiness',
        width: '200px'
      },
      {
        field: 'dvndNameOfBank',
        header: 'Name of Bank',
        width: '200px'
      },
      {
        field: 'dvndBranchOfBank',
        header: 'Branch of the bank',
        width: '200px'
      },
      {
        field: 'dvndFatherOrHusbandName',
        header: 'Father/Husband Name',
        width: '200px'
      },
      {
        field: 'dvndBsrCodeOfBankBranch',
        header: 'BSR code of the bank branch (7 digit)',
        width: '200px'
      },
      {
        field: 'dvndAccountantName',
        header: 'Accountant Name',
        width: '200px'
      },
      {
        field: 'dvndNameOfProprietorship',
        header: 'Name of proprietorship/firm',
        width: '200px'
      },
      {
        field: 'dvndNameOfPremisesBuildingVillage',
        header: 'Name of Premises/Building/Village',
        width: '200px'
      },
      {
        field: 'dvndFlatDoorBlockNo',
        header: 'Flat/Door/Block No',
        width: '200px'
      },
      {
        field: 'dvndAreaLocality',
        header: 'Area/Locality',
        width: '200px'
      },
      {
        field: 'dvndTownCityDistrict',
        header: 'Town/City/District',
        width: '200px'
      },
      {
        field: 'dvndRoadStreetPostOffice',
        header: 'Road/Street/Post Office',
        width: '200px'
      },
      {
        field: 'dvndState',
        header: 'State',
        width: '200px'
      },
      {
        field: 'dvndPinCode',
        header: 'PIN Code',
        width: '200px'
      },
      {
        field: 'dvndCountry',
        header: 'Country',
        width: '200px'
      },
      {
        field: 'dvndMembershipNumber',
        header: 'Membership Number',
        width: '200px'
      },
      {
        field: 'dvndRegistrationNumber',
        header: 'Registration Number',
        width: '200px'
      },

      {
        type: 'action',
        header: 'Action',
        width: '100px'
      }
    ];
    this.stepper();
  }

  initialLoading(): void {
    this.createDedectorForm();
    this.residentialstatuslist = [];
    this.residentialstatuslist.push({
      label: 'Select residential status',
      value: ''
    });
    this.allcountries = [];
    this.allcountries.push({ label: 'Select country', value: '' });
    this.states = [];
    this.states.push({ label: 'Select state', value: undefined });
    this.cities = [];
    this.cities.push({ label: 'Select city', value: undefined });
    this.getCountries();
    this.allstates = []; //this.getStates();
    this.resideductortyprlist = [];
    this.resideductortyprlist.push({
      label: 'Select type',
      value: ''
    });
    this.getDividendDeductorlist = [];
    this.getDividendDeductorlist.push({
      label: 'Select Dividend deductor type',
      value: ''
    });
    this.modeofpaymentlist = [];
    this.modeofpaymentlist.push({
      label: 'Select mode of payment',
      value: ''
    });
    this.statuslist = [];
    this.statuslist.push({ label: 'Select status', value: '' });
    this.getDeductorType();
    this.getDividendDeductorType();
    this.getModeOfPayment();
    this.getStatus();
    this.getResidentialStatus();
    this.headingMsg = 'Add';
    this.statechecker(this.action, this.deductorInputId);
    this.onChanges();
  }

  stepper(): void {
    this.items = [
      {
        label: 'Deductor Details',
        command: (event: any) => {
          this.activeIndex = 0;
        }
      },
      {
        label: 'Tan Details',
        command: (event: any) => {
          this.activeIndex = 1;
        }
      }
    ];
  }

  changeStatesHandler(event: any, index: any): void {
    this.states = [];
    this.deductorService.getStates(event.value).subscribe((result: any) => {
      if (result && result.length > 0) {
        this.states.push({ label: 'Select state', value: undefined });
        for (let i = 0; i < result.length; i++) {
          let states = {
            label: result[i],
            value: result[i]
          };
          this.states = [...this.states, states];
        }

        this.pinCodeNumber = 6;
        let setControl = this.deductorForm.get('tanList') as FormArray;
        setControl.controls[index]['controls']['pinCode'].setValidators([
          Validators.maxLength(6),
          Validators.minLength(6)
        ]);
        setControl.controls[index]['controls']['personpinCode'].setValidators([
          Validators.maxLength(6),
          Validators.minLength(6)
        ]);
        setControl.controls[index]['controls']['city'].enable();
        setControl.controls[index]['controls']['townCityDistrict'].enable();
      } else {
        let states = {
          label: 'State outside India',
          value: 'State outside India'
        };
        this.states = [...this.states, states];
        this.pinCodeNumber = 10;
        let setControl = this.deductorForm.get('tanList') as FormArray;
        setControl.controls[index]['controls']['pinCode'].setValidators([
          Validators.maxLength(10),
          Validators.minLength(10)
        ]);
        setControl.controls[index]['controls']['personpinCode'].setValidators([
          Validators.maxLength(10),
          Validators.minLength(10)
        ]);
        setControl.controls[index]['controls']['city'].disable();
        setControl.controls[index]['controls']['townCityDistrict'].disable();
      }
    });
  }

  changeCityHandler(event: any, index: any): any {
    this.cities = [];
    this.cities.push({ label: 'Select city', value: undefined });
    if (event.name !== 'State outside India') {
      this.deductorService.getCities(event.value).subscribe((result: any) => {
        if (result && result.length > 0) {
          for (let i = 0; i < result.length; i++) {
            let cities = {
              label: result[i].city,
              value: result[i].city
            };
            this.cities = [...this.cities, cities];
          }
        }
        this.allcities = result;
      });
    } else {
      return 'State outside India';
    }
  }

  stateOnChange() {
    const index = this.states.findIndex(
      (x: any) => x.value == 'State outside India'
    );
    if (index != -1) {
      return 'State outside India';
    }
  }

  changestdHandler(event: any, index: any, type: any): void {
    const cityName = event.value;
    for (let i = 0; i < this.allcities.length; i++) {
      if (cityName === this.allcities[i].city) {
        let setControl = this.deductorForm.get('tanList') as FormArray;
        if (type === 'tan') {
          setControl.controls[index]['controls']['stdCode'].setValue(
            this.allcities[i].stdCode
          );
        } else {
          setControl.controls[index]['controls']['personstdCode'].setValue(
            this.allcities[i].stdCode
          );
        }
      }
    }
  }
  //View
  fetchDatabyId(params: any): void {
    this.deductorService.getDeductorByPan(params.id).subscribe(
      (result: any) => {
        if (
          params.action.toUpperCase() === 'EDIT' ||
          params.action.toUpperCase() === 'VIEW'
        ) {
          this.data = result;
          this.stateChanger(params.action, this.data);
        } else {
          console.log('inside add');
          this.tanListData = [];
          this.actionState = UtilModule.stateChanger(
            this.deductorForm,
            params.action,
            []
          );
        }
      },
      error => console.error(error)
    );
    /* (result: any) => {
        console.log(result);
        this.editDataBinder(result);
        if (result && result.tanList.length > 0) {
          for (let i = 0; i < result.tanList.length; i++) {
            this.changeStatesHandler(
              { value: result.tanList[i].countryName },
              i
            );
            this.changeCityHandler({ value: result.tanList[i].stateName }, i);
            this.changeCityHandler({ value: result.tanList[i].state }, i);
          }
        }

        if (result && result.applicableTo !== null) {
          this.deductorForm
            .get('applicableTo')
            .setValidators(Validators.required);
        }

        this.headingMsg = 'View';
        this.gstinField = result.gstin;
        this.deductorForm.patchValue(result);
        console.log("printing tandetails",this.deductorForm.value.tanList);
        this.disablePreviousDate(result.applicableFrom);
        if (params.action.toUpperCase() === 'EDIT') {
          this.stateChanger('edit');
        } else {
          this.actionState = UtilModule.stateChanger(
            this.deductorForm,
            params.action,
            []
          );
        }
      },
      (error: any) => {
        console.error(error);
      }
    );
 */
  }

  initAddress(): FormGroup {
    return this.formBuilder.group({
      tan: ['', [Validators.required]],
      flatDoorBlockNo: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      nameBuildingVillage: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      roadStreetPostoffice: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      areaLocality: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      stateName: ['', Validators.required],
      countryName: ['', Validators.required],
      townCityDistrict: ['', [Validators.required]],
      pinCode: ['', [Validators.required]],
      stdCode: [''],
      name: ['', [Validators.required]],
      pan: ['', [Validators.required]],
      designation: ['', [Validators.required]],
      personflatDoorBlockNo: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      buildingName: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      streetName: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      area: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !:@#$&_()\\-^`.+,/]*')
        ]
      ],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]],
      personpinCode: ['', [Validators.required]],
      personstdCode: [''],
      telephone: ['', [Validators.required]],
      alternateTelephone: [''],
      mobilenumber: ['', [Validators.required]],
      email: [
        '',
        [Validators.required, Validators.email, Validators.maxLength(50)]
      ],
      alternateEmail: ['', [Validators.email, Validators.maxLength(50)]],
      personAddressChange: [false],
      dvndNameOfBank: ['', []],
      dvndFatherOrHusbandName: ['', []],
      dvndPrincipalAreaOfBusiness: ['', []],
      dvndBranchOfBank: ['', []],
      dvndBsrCodeOfBankBranch: ['', []],
      dvndNameOfProprietorship: ['', []],
      dvndAccountantName: ['', []],
      dvndNameOfPremisesBuildingVillage: ['', []],
      dvndFlatDoorBlockNo: ['', []],
      dvndRoadStreetPostOffice: ['', []],
      dvndAreaLocality: ['', []],
      dvndTownCityDistrict: ['', []],
      dvndState: ['', []],
      dvndCountry: ['', []],
      dvndPinCode: ['', []],
      dvndMembershipNumber: ['', []],
      dvndRegistrationNumber: ['', []],
      dvndOptedFor15CaCb: [false],
      accountantSalutation: ['']
    });
  }

  public enableDividendFields(event: any): void {
    if (event) {
      let tanListform = this.deductorForm.get('tanList') as FormArray;
      for (let i = 0; i < tanListform.length; i++) {
        tanListform.controls[i].get('dvndNameOfBank').enable();
        tanListform.controls[i].get('dvndFatherOrHusbandName').enable();
        tanListform.controls[i].get('dvndPrincipalAreaOfBusiness').enable();
        tanListform.controls[i].get('dvndBranchOfBank').enable();
        tanListform.controls[i].get('dvndBsrCodeOfBankBranch').enable();
        tanListform.controls[i].get('dvndNameOfProprietorship').enable();
        tanListform.controls[i].get('dvndAccountantName').enable();
        tanListform.controls[i]
          .get('dvndNameOfPremisesBuildingVillage')
          .enable();
        tanListform.controls[i].get('dvndFlatDoorBlockNo').enable();
        tanListform.controls[i].get('dvndRoadStreetPostOffice').enable();
        tanListform.controls[i].get('dvndAreaLocality').enable();
        tanListform.controls[i].get('dvndTownCityDistrict').enable();
        tanListform.controls[i].get('dvndState').enable();
        tanListform.controls[i].get('dvndCountry').enable();
        tanListform.controls[i].get('dvndPinCode').enable();
        tanListform.controls[i].get('dvndMembershipNumber').enable();
        tanListform.controls[i].get('dvndRegistrationNumber').enable();

        tanListform.controls[i]
          .get('dvndNameOfBank')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndFatherOrHusbandName')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndPrincipalAreaOfBusiness')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndBranchOfBank')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndBsrCodeOfBankBranch')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndNameOfProprietorship')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndAccountantName')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndNameOfPremisesBuildingVillage')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndFlatDoorBlockNo')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndRoadStreetPostOffice')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndAreaLocality')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndTownCityDistrict')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndState')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndCountry')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndPinCode')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndMembershipNumber')
          .setValidators(Validators.required);
        tanListform.controls[i]
          .get('dvndRegistrationNumber')
          .setValidators(Validators.required);
      }
    } else {
      let tanListform = this.deductorForm.get('tanList') as FormArray;
      for (let i = 0; i < tanListform.length; i++) {
        tanListform.controls[i].get('dvndNameOfBank').disable();
        tanListform.controls[i].get('dvndFatherOrHusbandName').disable();
        tanListform.controls[i].get('dvndPrincipalAreaOfBusiness').disable();
        tanListform.controls[i].get('dvndBranchOfBank').disable();
        tanListform.controls[i].get('dvndBsrCodeOfBankBranch').disable();
        tanListform.controls[i].get('dvndNameOfProprietorship').disable();
        tanListform.controls[i].get('dvndAccountantName').disable();
        tanListform.controls[i]
          .get('dvndNameOfPremisesBuildingVillage')
          .disable();
        tanListform.controls[i].get('dvndFlatDoorBlockNo').disable();
        tanListform.controls[i].get('dvndRoadStreetPostOffice').disable();
        tanListform.controls[i].get('dvndAreaLocality').disable();
        tanListform.controls[i].get('dvndTownCityDistrict').disable();
        tanListform.controls[i].get('dvndState').disable();
        tanListform.controls[i].get('dvndCountry').disable();
        tanListform.controls[i].get('dvndPinCode').disable();
        tanListform.controls[i].get('dvndMembershipNumber').disable();
        tanListform.controls[i].get('dvndRegistrationNumber').disable();

        tanListform.controls[i].get('dvndNameOfBank').setValidators([]);
        tanListform.controls[i]
          .get('dvndFatherOrHusbandName')
          .setValidators([]);
        tanListform.controls[i]
          .get('dvndPrincipalAreaOfBusiness')
          .setValidators([]);
        tanListform.controls[i].get('dvndBranchOfBank').setValidators([]);
        tanListform.controls[i]
          .get('dvndBsrCodeOfBankBranch')
          .setValidators([]);
        tanListform.controls[i]
          .get('dvndNameOfProprietorship')
          .setValidators([]);
        tanListform.controls[i].get('dvndAccountantName').setValidators([]);
        tanListform.controls[i]
          .get('dvndNameOfPremisesBuildingVillage')
          .setValidators([]);
        tanListform.controls[i].get('dvndFlatDoorBlockNo').setValidators([]);
        tanListform.controls[i]
          .get('dvndRoadStreetPostOffice')
          .setValidators([]);
        tanListform.controls[i].get('dvndAreaLocality').setValidators([]);
        tanListform.controls[i].get('dvndTownCityDistrict').setValidators([]);
        tanListform.controls[i].get('dvndState').setValidators([]);
        tanListform.controls[i].get('dvndCountry').setValidators([]);
        tanListform.controls[i].get('dvndPinCode').setValidators([]);
        tanListform.controls[i].get('dvndMembershipNumber').setValidators([]);
        tanListform.controls[i].get('dvndRegistrationNumber').setValidators([]);
      }
    }
  }

  mapPersonDetails() {
    for (let i = 0; i < this.deductorForm.value.tanList.length; i++) {
      const personResponsibleDetails = {
        name: this.deductorForm.value.tanList[i].name,
        pan: this.deductorForm.value.tanList[i].pan,
        designation: this.deductorForm.value.tanList[i].designation,
        personflatDoorBlockNo: this.deductorForm.value.tanList[i]
          .personflatDoorBlockNo,
        buildingName: this.deductorForm.value.tanList[i].buildingName,
        streetName: this.deductorForm.value.tanList[i].streetName,
        area: this.deductorForm.value.tanList[i].area,
        city: this.deductorForm.value.tanList[i].city,
        state: this.deductorForm.value.tanList[i].state,
        personpinCode: this.deductorForm.value.tanList[i].personpinCode,
        telephone: this.deductorForm.value.tanList[i].telephone,
        alternateTelephone: this.deductorForm.value.tanList[i]
          .alternateTelephone,
        mobilenumber: this.deductorForm.value.tanList[i].mobilenumber,
        email: this.deductorForm.value.tanList[i].email,
        alternateEmail: this.deductorForm.value.tanList[i].alternateEmail,
        personAddressChange: this.deductorForm.value.tanList[i]
          .personAddressChange,
        personstdCode: this.deductorForm.value.tanList[i].personstdCode,
        stdCode: this.deductorForm.value.tanList[i].stdCode,
        dvndNameOfBank: this.deductorForm.value.tanList[i].dvndNameOfBank,
        dvndFatherOrHusbandName: this.deductorForm.value.tanList[i]
          .dvndFatherOrHusbandName,
        dvndPrincipalAreaOfBusiness: this.deductorForm.value.tanList[i]
          .dvndPrincipalAreaOfBusiness,
        dvndBranchOfBank: this.deductorForm.value.tanList[i].dvndBranchOfBank,
        dvndBsrCodeOfBankBranch: this.deductorForm.value.tanList[i]
          .dvndBsrCodeOfBankBranch,
        dvndNameOfProprietorship: this.deductorForm.value.tanList[i]
          .dvndNameOfProprietorship,
        dvndAccountantName: this.deductorForm.value.tanList[i]
          .dvndAccountantName,
        dvndNameOfPremisesBuildingVillage: this.deductorForm.value.tanList[i]
          .dvndNameOfPremisesBuildingVillage,
        dvndFlatDoorBlockNo: this.deductorForm.value.tanList[i]
          .dvndFlatDoorBlockNo,
        dvndRoadStreetPostOffice: this.deductorForm.value.tanList[i]
          .dvndRoadStreetPostOffice,
        dvndAreaLocality: this.deductorForm.value.tanList[i].dvndAreaLocality,
        dvndTownCityDistrict: this.deductorForm.value.tanList[i]
          .dvndTownCityDistrict,
        dvndState: this.deductorForm.value.tanList[i].dvndState,
        dvndCountry: this.deductorForm.value.tanList[i].dvndCountry,
        dvndPinCode: this.deductorForm.value.tanList[i].dvndPinCode,
        dvndMembershipNumber: this.deductorForm.value.tanList[i]
          .dvndMembershipNumber,
        dvndRegistrationNumber: this.deductorForm.value.tanList[i]
          .dvndRegistrationNumber,
        dvndOptedFor15CaCb: this.deductorForm.value.tanList[i]
          .dvndOptedFor15CaCb
      };
      this.deductorFormValues = this.deductorForm.value;
      this.deductorFormValues.tanList[
        i
      ].personResponsibleDetails = personResponsibleDetails;
      delete this.deductorFormValues.tanList[i].name;
      delete this.deductorFormValues.tanList[i].pan;
      delete this.deductorFormValues.tanList[i].designation;
      delete this.deductorFormValues.tanList[i].personflatDoorBlockNo;
      delete this.deductorFormValues.tanList[i].personpinCode;
      delete this.deductorFormValues.tanList[i].buildingName;
      delete this.deductorFormValues.tanList[i].streetName;
      delete this.deductorFormValues.tanList[i].area;
      delete this.deductorFormValues.tanList[i].city;
      delete this.deductorFormValues.tanList[i].state;
      delete this.deductorFormValues.tanList[i].telephone;
      delete this.deductorFormValues.tanList[i].alternateTelephone;
      delete this.deductorFormValues.tanList[i].mobilenumber;
      delete this.deductorFormValues.tanList[i].email;
      delete this.deductorFormValues.tanList[i].alternateEmail;
      delete this.deductorFormValues.tanList[i].personAddressChange;
      delete this.deductorFormValues.tanList[i].personstdCode;
      delete this.deductorFormValues.tanList[i].stdCode;
      delete this.deductorFormValues.tanList[i].dvndNameOfBank;
      delete this.deductorFormValues.tanList[i].dvndFatherOrHusbandName;
      delete this.deductorFormValues.tanList[i].dvndPrincipalAreaOfBusiness;
      delete this.deductorFormValues.tanList[i].dvndBranchOfBank;
      delete this.deductorFormValues.tanList[i].dvndBsrCodeOfBankBranch;
      delete this.deductorForm.value.tanList[i].dvndNameOfProprietorship;
      delete this.deductorForm.value.tanList[i].dvndAccountantName;
      delete this.deductorForm.value.tanList[i]
        .dvndNameOfPremisesBuildingVillage;
      delete this.deductorForm.value.tanList[i].dvndFlatDoorBlockNo;
      delete this.deductorForm.value.tanList[i].dvndRoadStreetPostOffice;
      delete this.deductorForm.value.tanList[i].dvndAreaLocality;
      delete this.deductorForm.value.tanList[i].dvndTownCityDistrict;
      delete this.deductorForm.value.tanList[i].dvndState;
      delete this.deductorForm.value.tanList[i].dvndCountry;
      delete this.deductorForm.value.tanList[i].dvndPinCode;
      delete this.deductorForm.value.tanList[i].dvndMembershipNumber;
      delete this.deductorForm.value.tanList[i].dvndRegistrationNumber;
      delete this.deductorForm.value.tanList[i].dvndOptedFor15CaCb;
    }
  }
  //Adding Tan
  addTan(): void {
    if (this.headingMsg === 'Add') {
      this.tanListData = this.deductorForm.value.tanList;
    }
    const control = this.deductorForm.controls.tanList as FormArray;
    control.push(this.initAddress());
    if (this.action == 'edit') {
      this.actionHandler = undefined;
    }
  }
  //Removing Tan
  removeTan(i: number): void {
    const control = this.deductorForm.controls.tanList as FormArray;
    control.removeAt(i);
  }

  removeTableTan(row: any) {
    const index = this.deductorForm.controls.tanList.value.findIndex(
      (x: any) => x.tan == row.tan
    );
    this.removeTan(index);
  }

  onChanges(): void {
    this.deductorForm.get('applicableTo').valueChanges.subscribe(val => {
      this.applicableToDateError = false;
    });
  }

  panStatusValidator(value: any) {
    if (value.length == 4) {
      this.populateStatue(value);
    } else if (value.length == 10) {
      this.populateStatue(value);
      this.deductorService.getPanStatus(value).subscribe((result: any) => {
        if (result === 'Unique') {
          this.deductorForm.controls.pan.setErrors(null);
        } else {
          this.deductorForm.controls.pan.setErrors({ panValid: true });
        }
      });
    }
  }

  populateStatue(value: any): void {
    for (let i = 0; i < this.totalStatus.length; i++) {
      const label = new String(this.totalStatus[i]['label']);
      const lowercase_lable = label.toLowerCase();
      const uppercase_lable = label.toUpperCase();

      if (value[3] === uppercase_lable[0] || value[3] === lowercase_lable[0]) {
        this.deductorForm.controls.status.setValue(this.statuslist[i].value);
      } else if (value[3] === 'J' || value[3] === 'j') {
        this.deductorForm.controls.status.setValue(this.statuslist[2].value);
      }
    }
  }

  disablePreviousDate(date: any) {
    let nextDay = new Date(date);
    nextDay.setDate(nextDay.getDate() + 1);
    this.minDate = {
      year: nextDay.getFullYear(),
      month: nextDay.getMonth() + 1,
      day: nextDay.getDate()
    };
  }
  getFormattedDate(date1: any) {
    let date = new Date(date1);
    let year = date.getFullYear();
    let month = (1 + date.getMonth()).toString().padStart(2, '0');
    let day = date
      .getDate()
      .toString()
      .padStart(2, '0');
    return year + '-' + month + '-' + day;
  }

  findInvalidControls(): any {
    const invalid = [];
    const controls = this.deductorForm.controls;

    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    console.log('sjdskds', invalid);
    return invalid;
  }
  changeModule() {
    if (this.deductorForm.controls.scopeGroup.get('Tcs').value) {
      if (this.gstinField == '') {
        this.deductorForm.get('gstin').enable();
      }
      this.gstinRequiredFieldsValidation();
    } else {
      if (this.deductorForm.controls.gstin.value.length > 0) {
        this.gstinPatterFieldsValidation();
        return;
      }
      this.gstinClearFieldsValidation();
    }
    this.deductorForm.get('gstin').updateValueAndValidity();
  }
  gstinNumberValidationCheck(event: any) {
    if (event.target.value.length > 0) {
      this.gstinPatterFieldsValidation();
    } else if (
      event.target.value.length == 0 &&
      this.deductorForm.controls.scopeGroup.get('Tcs').value
    ) {
      this.gstinRequiredFieldsValidation();
    } else {
      this.gstinClearFieldsValidation();
    }
    this.deductorForm.get('gstin').updateValueAndValidity();
  }

  gstinRequiredFieldsValidation() {
    this.deductorForm
      .get('gstin')
      .setValidators([
        Validators.required,
        Validators.pattern(/\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}\d{1}/)
      ]);
  }
  gstinPatterFieldsValidation() {
    this.deductorForm
      .get('gstin')
      .setValidators([
        Validators.pattern(/\d{2}[A-Z]{5}\d{4}[A-Z]{1}[A-Z\d]{1}[Z]{1}\d{1}/)
      ]);
  }
  gstinClearFieldsValidation() {
    this.deductorForm.get('gstin').clearValidators();
  }
  //Saving deductor details

  saveDeductor(type: any): void {
    this.findInvalidControls();
    this.submitted = true;
    if (this.deductorForm.invalid) {
      return;
    }
    this.mapPersonDetails();
    if (this.deductorForm.controls.applicableTo.value !== null) {
      let applicableFromDate = this.getFormattedDate(
        this.deductorForm.controls.applicableFrom.value
      );
      let applicableToDate = this.getFormattedDate(
        this.deductorForm.controls.applicableTo.value
      );
      if (applicableToDate <= applicableFromDate) {
        this.applicableToDateError = true;
        return;
      }
    }
    console.log(this.deductorFormValues);
    this.deductorService.addDeductor(this.deductorFormValues).subscribe(
      (res: IDeductor) => {
        this.reset();
        this.closePopup.emit(false);
      },
      (error: any) => {
        this.closePopup.emit(false);
      }
    );
  }

  reset(): void {
    this.deductorForm.reset();
    this.submitted = false;
    this.submittedTo = false;
    this.initialLoading();
  }

  updateDeductor(): void {
    this.submittedTo = true;
    this.submitted = true;
    if (this.deductorForm.invalid) {
      return;
    }
    console.log(this.deductorForm.getRawValue() as IDeductor);
    this.deductorService
      .updateDeductorById(this.deductorForm.getRawValue() as IDeductor)
      .subscribe(
        (res: any) => {
          this.reset();
          this.closePopup.emit(false);
        },
        (error: any) => {
          window.scrollTo(0, 0);
          this.closePopup.emit(false);
        }
      );
  }

  get getTanList() {
    //   this.deductorForm.controls.tanList.reset();
    return this.deductorForm.controls.tanList as FormArray;
  }

  onNext() {
    if (this.headingMsg == 'Add') {
      this.submitted = true;
      this.removeTan(0);
      if (this.deductorForm.invalid) {
        return;
      }
      this.submitted = false;
      this.addTan();
      this.activeIndex = this.activeIndex + 1;
    } else {
      this.activeIndex = this.activeIndex + 1;
    }
  }

  removeNatureOfPayment(i: number): void {
    const control = this.deductorForm.controls.tanList as FormArray;
    control.removeAt(i);
  }

  backFormClick(): void {
    this.router.navigate(['/dashboard/masters/deductor']);
  }

  stateChanger(value: any, data: any): void {
    this.actionHandler = value;
    this.tanListData = [];
    this.editDataBinder(data);
    console.log(data.tanList.length);
    if (data && data.tanList.length > 0) {
      for (let i = 0; i < data.tanList.length; i++) {
        this.changeStatesHandler({ value: data.tanList[i].countryName }, i);
        this.changeCityHandler({ value: data.tanList[i].stateName }, i);
        this.changeCityHandler({ value: data.tanList[i].state }, i);
      }
    }
    data.applicableFrom = new Date(data.applicableFrom);
    if (data && data.applicableTo !== null) {
      data.applicableTo = new Date(data.applicableTo);
      this.deductorForm.get('applicableTo').setValidators(Validators.required);
    }
    this.tanListData = data.tanList;
    this.deductorForm.patchValue(data);
    this.disablePreviousDate(data.applicableFrom);
    if (value.toUpperCase() === 'VIEW') {
      this.headingMsg = 'View';
      this.actionState = UtilModule.stateChanger(
        this.deductorForm,
        value.toUpperCase(),
        []
      );
    } else {
      this.headingMsg = 'Update';
      this.actionState = UtilModule.stateChanger(this.deductorForm, value, [
        'doesDeductorHasMorethanOneBranch',
        'applicableTo',
        'scopeGroup',
        'dvndDeductorTypeName'
      ]);
    }
    /* this.actRoute.queryParams.subscribe((params: any) => {
      this.actionState = UtilModule.stateChanger(this.deductorForm, value, [
         'doesDeductorHasMorethanOneBranch',
         'applicableTo',
         'scopeGroup'
       ]);
    });
    this.headingMsg = 'Update';
  */
  }

  getResidentialStatus(): void {
    this.deductorService.getResidentialStatus().subscribe(
      (result: any) => {
        for (let i = 0; i < result.length; i++) {
          let residentialstatuslist = {
            label: result[i].status,
            value: result[i].status
          };
          this.residentialstatuslist = [
            ...this.residentialstatuslist,
            residentialstatuslist
          ];
        }
      },
      (err: any) => {
        this.getresidentialstatuserror = true;
      }
    );
  }
  /* getStates(): void {
    this.deductorService.getStates().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let allstates = {
          label: result[i].name,
          value: result[i].name
        };
        this.allstates = [...this.allstates, allstates];
      }
      this.statesListResult = result;

      if (this.selectedCountry != undefined) {
        this.changeStatesHandler({
          value: this.selectedCountry
        });
      }
    });
  }
  */ getCountries(): void {
    this.deductorService.getCountries().subscribe((result: any) => {
      for (let i = 0; i < result.length; i++) {
        let allcountries = {
          label: result[i].name,
          value: result[i].name
        };
        this.allcountries = [...this.allcountries, allcountries];
      }
    });
  }

  getDeductorType(): void {
    this.deductorService.getDeductorType().subscribe(
      (result: any) => {
        for (let i = 0; i < result.length; i++) {
          let resideductortyprlist = {
            label: result[i].type,
            value: result[i].type
          };
          this.resideductortyprlist = [
            ...this.resideductortyprlist,
            resideductortyprlist
          ];
        }
      },
      (err: any) => {
        this.resideductortyprlisterror = true;
      }
    );
  }

  //Dividend Deductor Type
  getDividendDeductorType(): void {
    this.deductorService.getDividendDeductorType().subscribe(
      (result: any) => {
        for (let i = 0; i < result.length; i++) {
          let resiDividenddeductortyprlist = {
            label: result[i].name,
            value: result[i].name
          };
          this.getDividendDeductorlist = [
            ...this.getDividendDeductorlist,
            resiDividenddeductortyprlist
          ];
        }
      },
      (err: any) => {
        this.getDividendDeductorlisterror = true;
      }
    );
  }

  getModeOfPayment(): void {
    this.deductorService.getModeOfPayment().subscribe(
      (result: Array<any>) => {
        for (let i = 0; i < result.length; i++) {
          let modeofpaymentlist = {
            label: result[i].mode,
            value: result[i].mode
          };
          this.modeofpaymentlist = [
            ...this.modeofpaymentlist,
            modeofpaymentlist
          ];
        }
      },
      (err: any) => {
        this.modeofpaymentlisterror = true;
      }
    );
  }

  getStatus(): void {
    this.deductorService.getStatus().subscribe(
      (result: Array<any>) => {
        for (let i = 0; i < result.length; i++) {
          let statuslist = {
            label: result[i].status,
            value: result[i].status
          };
          this.statuslist = [...this.statuslist, statuslist];
        }
        this.totalStatus = this.statuslist;
      },
      (err: any) => {
        this.statuslisterror = true;
      }
    );
  }

  get formArr() {
    return this.deductorForm.get('tanList') as FormArray;
  }

  editDataBinder(data: any): void {
    if (data.tanList.length > 0) {
      for (let i = 0; i < data.tanList.length - 1; i++) {
        this.addTan();
      }
    }
  }

  private createDedectorForm(): void {
    this.deductorForm = this.formBuilder.group({
      deductorCode: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 -]*'),
          Validators.maxLength(25)
        ]
      ],
      deductorName: [
        '',
        [
          Validators.required,
          Validators.pattern('[a-zA-Z0-9 !@#*$&_()\\-`.+,/]*'),
          Validators.maxLength(256)
        ]
      ],
      pan: ['', Validators.required],
      residentialStatus: ['', [Validators.required]],
      doesDeductorHasMorethanOneBranch: [false],
      status: ['', [Validators.required]],
      deductorTypeName: ['', [Validators.required]],
      modeOfPaymentType: [''],
      dueDateOfTaxPayment: [undefined],
      email: [
        '',
        [Validators.required, Validators.email, Validators.maxLength(50)]
      ],
      emailAlternate: ['', [Validators.email, Validators.maxLength(50)]],
      phoneNumber: ['', [Validators.required]],
      phoneNumberAlternate: [''],
      gstin: [''],
      // [Validators.required]
      tanList: this.formBuilder.array([this.initAddress()]),
      applicableFrom: ['', [Validators.required]],
      applicableTo: [undefined],
      active: [true],
      deductorSalutation: new FormControl(''),
      scopeGroup: new FormGroup(
        {
          Tds: new FormControl(false),
          Tcs: new FormControl(false)
        },
        this.requireCheckboxesToBeCheckedValidator(1)
      ),
      dvndDeductorTypeName: ['']
    });

    //   console.log("printing form controllers",this.deductorForm.controls);
  }
  requireCheckboxesToBeCheckedValidator(minRequired: any): ValidatorFn {
    return function validate(formGroup: FormGroup) {
      let checked = 0;

      Object.keys(formGroup.controls).forEach(key => {
        const control = formGroup.controls[key];

        if (control.value === true) {
          checked++;
        }
      });

      if (checked < minRequired) {
        return {
          requireOneCheckboxToBeChecked: true
        };
      }

      return null;
    };
  }

  private statechecker(action: string, id: string): void {
    if (!action || action === '') {
      this.tanListData = [];
      this.actionState = UtilModule.stateChanger(this.deductorForm, 'New');
    } else if (
      action.toUpperCase() === 'EDIT' ||
      action.toUpperCase() === 'VIEW'
    ) {
      if (id && +id !== 0) {
        this.deductorPan = id;
        this.fetchDatabyId({ action: action, id: id });
      }
    }
  }
}
