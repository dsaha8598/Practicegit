import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Coupon } from 'src/app/models/Coupon';
import { AirlineService } from 'src/app/services/airlinr.service';

@Component({
  selector: 'app-manage-discount',
  templateUrl: './manage-discount.component.html',
  styleUrls: ['./manage-discount.component.scss']
})
export class ManageDiscountComponent implements OnInit {

  couponFormGroup: any
  editcouponFormGroup: any
  coupons:any
  editCoupon:Coupon=new Coupon("",0,0,0)

  constructor(private formBuilder: FormBuilder, private router: Router, private airlineService: AirlineService) {
    this.couponFormGroup = this.formBuilder.group({
      id: [''],
      couponcode: [
        '', Validators.required
      ],
      percentage: [
        ''
      ],
      maxamount: [
        '', Validators.required
      ]

    })
    this.editcouponFormGroup=this.couponFormGroup
    
  }

  ngOnInit(): void {
    this.getAllCoupons()
  }

  saveCoupon() {
    console.log("saving coupon")
    console.log(this.couponFormGroup.value)
    this.airlineService.saveCoupon(this.couponFormGroup.value)
      .subscribe(res => {
        console.log(res);
        this.router.navigate(['/discount'])
        this.getAllCoupons();
      })
    this.couponFormGroup.reset()
   
  }

  viewCouponById(id:string){
    console.log("coupon view by id")
     this.airlineService.getCouponsById(id)
    /* .subscribe(res=>{
       //this.editCoupon=res;
       
       this.copyToEditFormGroup(this.editCoupon)
     })*/
     .subscribe((res: any) => {
      console.log(res);
      this.editCoupon = res;
     // this.hide=false
     this.copyToEditFormGroup(this.editCoupon)
     
      //this.editSchedulers.push(this.scheduler)
      //console.log('printing airline object' + this.editSchedulers)
    })
  }

  getAllCoupons(){
    this.airlineService.getAllCoupons()
    .subscribe(res => {
      console.log(res);
      this.router.navigate(['/discount'])
      this.coupons=res;
      
    },err=>{
      console.log('from error')
      this.router.navigate(['/error'])
  }
    )
  }

  copyToEditFormGroup(coupon:Coupon){
    this.editcouponFormGroup = this.formBuilder.group({
      id: [coupon.id],
      couponcode: [
        coupon.couponcode, Validators.required
      ],
      percentage: [
        coupon.percentage
      ],
      maxamount: [
        coupon.maxamount, Validators.required
      ]

    })
  }

  updateDiscount(){
    console.log('updating coupon')
    this.airlineService.saveCoupon(this.editcouponFormGroup.value)
   
    .subscribe(res => {
      console.log(res);
      this.router.navigate(['/discount'])
    })
    this.editcouponFormGroup.reset();  this.getAllCoupons();
  }
}
