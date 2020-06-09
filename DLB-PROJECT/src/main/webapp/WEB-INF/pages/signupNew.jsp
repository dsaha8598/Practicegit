<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet"
	href="../node_modules/bootstrap/dist/css/bootstrap.css">
	 <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
</head>
<style>
@import
	url(https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css)
	;

.form {
	height: 900px;
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
			<h2 class="text-monospace font-weight-bolder bg-info text-center">
				Signup Here</h2>
			<hr>
			<br>
			<form action="signupPost" method="POST" modelAttribute="signUpdomain"
				enctype="multipart/form-data">
				<div class="font-weight-bolder">
					<i class="fa fa-user-circle"></i> Full Name : <input type="text"
						placeholder="Full Name" class="form-control" name="fullName">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-user-circle"></i> User Name : <input type="text"
						placeholder="User Name" class="form-control" name="userName">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-envelope"></i> Email : <input type="email"
						placeholder="email" class="form-control" name="email">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-mobile"></i> Mobile : <input type="number"
						placeholder="Contact Number" class="form-control" name="phNo">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-calendar"></i> Date of Birth : <input type="date"
						placeholder="date" class="form-control" name="dateOfBirth">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-picture-o"></i> Image : <input type="file"
						class="form-control-file" name="file">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-lock"></i> Password : <input type="password"
						class="form-control" placeholder="password" name="password">
				</div>
				<br>
				<div class="font-weight-bolder">
					<i class="fa fa-lock"></i> Confirm Password : <input
						type="password" class="form-control"
						placeholder="confirm password" name="confirmPassword">
				</div>
				<br>
				<div class="font-weight-bolder">
					Gender : <input type="radio" name="gender" value="male">
					 <i class="fa fa-male"  ></i> male 
						<input type="radio" name="gender" value="female">
					<i class="fa fa-female"></i> female
				</div>
				<br>
				<div>
					<input type="checkbox"> i've read and accept the Terms and
					Conditions
				</div>
				<br>
				<div>
					 <a href="login" class="btn btn-warning font-weight-bolder text-white" role="button">
						Already have an account</a>
					<button class="btn btn-success font-weight-bolder">
						Register</button>
				</div>
			</form>
		</div>

	</div>

</body>
</html>