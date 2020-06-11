<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html>
<head>

<!-- Latest compiled and minified CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">

<!-- jQuery library -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>

<!-- Popper JS -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>

<!-- Latest compiled JavaScript -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>

<link rel="stylesheet"
	href="../node_modules/bootstrap/dist/css/bootstrap.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
</head>
<style>
@import
	url(https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css)
	;

body {
	height: 100%;
	background: rgba(231,232,234,0.5);
}

.form {
	height: 1150px;
	width: 600px;
	padding: 50px;
	margin: 30px;
	margin-left: 300px;
	box-shadow: 0px 20px 30px black;
	border: 2px ridge darkcyan;
	border-radius: 20px;
}

.containerCustom {
	float: left;
	margin-left: 100px;
}
</style>
<body>

	<div class="containerCustom">
		<div class="form">
			<h2 class="text-success font-weight-bolder bg-danger text-center">
				Signup Here</h2>
			<hr>
			<br>
			<form action="signupPost" method="POST" modelAttribute="signUpdomain"
				enctype="multipart/form-data" id="user_form">
				<div class="font-weight-bolder">
					<i class="fa fa-user-circle"></i> Full Name : <input type="text"
						placeholder="Full Name" class="form-control" name="fullName" id="fullName" autocomplete="off" >
                        
                        <h6 id="full"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-user-circle"></i> User Name : <input type="text"
						placeholder="User Name" class="form-control" name="userName" id="userName" autocomplete="off">
                        
                        <h6 id="user"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-envelope"></i> Email : <input type="text"
						placeholder="email" class="form-control" name="email" id="email" autocomplete="off">
                         
                        <h6 id="emailid"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-mobile"></i> Mobile : <input type="text"
						placeholder="Contact Number" class="form-control" name="phNo" id="phNo" autocomplete="off">
                       
                        <h6 id="contact"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-calendar"></i> Date of Birth : <input type="date"
						placeholder="date" class="form-control" name="dateOfBirth" id="dateOfBirth">
                       
                        <h6 id="dob"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-picture-o"></i> Image : <input type="file"
						class="form-control-file" name="file" id="file">
                        
                        <h6 id="imagefile"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-lock"></i> Password : <input type="password"
						class="form-control" placeholder="Password" name="password" id="password" autocomplete="off">
                        
                        <h6 id="pass"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-lock"></i> Confirm Password : <input
						type="password" class="form-control"
						placeholder="confirm password" name="ConfirmPassword" id="confirmPassword" autocomplete="off">
                        
                        <h6 id="cpass"></h6>
				</div>
				<br>
				<div class="font-weight-bolder">
					Gender : <input type="radio"  name="gender" value="male" checked> <i
                        class="fa fa-male"></i> male 
                        <input type="radio" name="gender"
						value="female"> <i class="fa fa-female"></i> female
                       
                        <h6 id="gen"></h6>
				</div>
				<br>
			
				<br>
				<div>
					<button type="reset" class="btn btn-danger font-weight-bolder">Reset</button>
					<button class="btn btn-success font-weight-bolder" id="submitbtn" >
						Register</button>
				</div>
				<br> <a href="login"
					class="btn btn-warning font-weight-bolder text-white" role="button">
					Already have an account</a>
			</form>
		</div>

	</div>


    <script>
        $(document).ready(function(){

            $('#full').hide();
            $('#user').hide();
            $('#emailid').hide();
            $('#contact').hide();
            $('#dob').hide();
            $('#imagefile').hide();
            $('#pass').hide();
            $('#cpass').hide();



            var full_err=true;
            var user_err=true;
            var email_err=true;
            var contact_err = true;
            var dob_err=true;
            var image_err=true;
            var password_err=true;
            var cpassword_err=true;

            $('#fullName').keyup(function(){
                fullName_check();
            });

            function fullName_check(){
                var fullName_val=$('#fullName').val();

                if(fullName_val.length == ''){
                 $('#full').show();
                 $('#full').html("**Please Fill the FullName");
                 $('#full').focus();
                 $('#full').css("color","red");
                 full_err = false;
                 return false;

               }else{
               $('#full').hide();
               }

               if(!isNaN(fullName_val)){
                $('#full').show();
                 $('#full').html("**only characters allowed");
                 $('#full').focus();
                 $('#full').css("color","red");
                 full_err = false;
                 return false;

               }else{
               $('#full').hide();
               }


               if(fullName_val.length<=3 || fullName_val.length>=20){

                $('#full').show();
                 $('#full').html("**name must be between 3 to 20");
                 $('#full').focus();
                 $('#full').css("color","red");
                 full_err = false;
                 return false;

               }else{
               $('#full').hide();
               }
    

            }



            $('#userName').keyup(function(){
                userName_check();
            });

            function userName_check(){
                var userName_val=$('#userName').val();

                if(userName_val.length == ''){
                 $('#user').show();
                 $('#user').html("**Please Fill the username");
                 $('#user').focus();
                 $('#user').css("color","red");
                 user_err = false;
                 return false;

               }else{
               $('#user').hide();
               }

               if(!isNaN(userName_val)){
                $('#user').show();
                 $('#user').html("**only characters allowed");
                 $('#user').focus();
                 $('#user').css("color","red");
                 user_err = false;
                 return false;

               }else{
               $('#user').hide();
               }


               if(userName_val.length<=3 || userName_val.length>=20){

                $('#user').show();
                 $('#user').html("**UserName must be between 3 to 20");
                 $('#user').focus();
                 $('#user').css("color","red");
                 user_err = false;
                 return false;

               }else{
               $('#user').hide();
               }

            }


            $('#email').keyup(function(){
                email_check();
            });
            function email_check(){
                 var email_var=$('#email').val();
                 
                 const re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

                 if(email_var == ''){
                 $('#emailid').show();
                 $('#emailid').html("**Please Fill the emailid");
                 $('#emailid').focus();
                 $('#emailid').css("color","red");
                 email_err = false;
                 return false;

               }else{
               $('#emailid').hide();
               }
               if(re.test(email_var)){
                $('#emailid').hide();
              
               }
               else{
                 $('#emailid').show();
                 $('#emailid').html("**email id not valid");
                 $('#emailid').focus();
                 $('#emailid').css("color","red");
                 email_err = false;
                 return false;

               }
              
            }


            $('#phNo').keyup(function(){
                phNo_check();
            });

            function phNo_check(){
                var phNo_val=$('#phNo').val();

                if(phNo_val==''){
                $('#contact').show();
                 $('#contact').html("**Please Enter the phonenumber");
                 $('#contact').focus();
                 $('#contact').css("color","red");
                 contact_err = false;
                 return false;

               }else{
               $('#contact').hide();
               }

               if(isNaN(phNo_val)){
                $('#contact').show();
                 $('#contact').html("**Invalid Phone Number");
                 $('#contact').focus();
                 $('#contact').css("color","red");
                 contact_err = false;
                 return false;

               }else{
               $('#contact').hide();
               }
               if(phNo_val.length!=10) {
                $('#contact').show();
                 $('#contact').html("**Invalid Phone Number must 10 digit");
                 $('#contact').focus();
                 $('#contact').css("color","red");
                 contact_err = false;
                 return false;

                }else{
               $('#contact').hide();
               }
  
            }


            $('#dateOfBirth').keyup(function(){
                dateOfBirth_check();
            });

            function dateOfBirth_check(){
                var dob_val=$('#dateOfBirth').val();

                if(dob_val==''){
                $('#dob').show();
                 $('#dob').html("**Please Enter DateOfBirth");
                 $('#dob').focus();
                 $('#dob').css("color","red");
                 dob_err = false;
                 return false;

               }else{
               $('#dob').hide();
               }

            }







            $('#file').keyup(function(){
                image_check();
            });

            function image_check(){
                var img_val=$('#file').val();

                if(img_val==''){
                $('#imagefile').show();
                 $('#imagefile').html("**Please select file");
                 $('#imagefile').focus();
                 $('#imagefile').css("color","red");
                 image_err = false;
                 return false;

               }else{
               $('#imagefile').hide();
               }

            }


            
            $('#password').keyup(function(){
                password_check();
            });
            function password_check(){
                var password_val=$('#password').val();

                if(password_val==''){
                $('#pass').show();
                 $('#pass').html("**Please enter password");
                 $('#pass').focus();
                 $('#pass').css("color","red");
                 password_err = false;
                 return false;

               }else{
               $('#pass').hide();
               }

               if((password_val.length < 3 ) || (password_val.length > 10 ) ){
                     $('#pass').show();
                     $('#pass').html("**password length must be between 3 and 10");
                     $('#pass').focus();
                     $('#pass').css("color","red");
                     password_err = false;
                     return false;

                      }else{
                       $('#pass').hide();
                       }
            }

            


            $('#confirmPassword').keyup(function(){
                   con_passwrd();
               });

               function con_passwrd(){

                   var conpass = $('#confirmPassword').val();
                   var passwrdstr = $('#password').val();

                   if(conpass==''){
                       $('#cpass').show();
                       $('#cpass').html("** Enter confirm Password");
                       $('#cpass').focus();
                       $('#cpass').css("color","red");
                       cpassword_err = false;
                        return false;
 
                    }else{
                      $('#cpass').hide();

                   }

                     if(passwrdstr != conpass){
                       $('#cpass').show();
                       $('#cpass').html("** Password is not Matching");
                       $('#cpass').focus();
                       $('#cpass').css("color","red");
                       cpassword_err = false;
                        return false;
 
                    }else{
                      $('#cpass').hide();
                    }
             
             } 


             $('#submitbtn').click(function(){
                 full_err=true;
                 user_err=true;
                 email_err=true;
                 contact_err = true;
                 dob_err=true;
                 image_err=true;
                 password_err=true;
                 cpassword_err=true;


                 fullName_check();
                 userName_check();
                 email_check();
                 phNo_check();
                 dateOfBirth_check();
                 image_check();
                 password_check();
                 con_passwrd();

                 if((full_err==true) && (user_err==true) && (email_err==true) && (contact_err==true)
                 && (dob_err==true) && (image_err==true) && (password_err==true) &&(cpassword_err==true)){
                     return true;
                 }else{
                     return false;
                 }

            })

        });




    </script>
	

</body>
</html>