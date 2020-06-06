<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<link
	href="https://code.jquery.com/ui/1.10.4/themes/ui-lightness/jquery-ui.css"
	rel="stylesheet">
<script src="https://code.jquery.com/jquery-1.10.2.js"></script>
<script src="https://code.jquery.com/ui/1.10.4/jquery-ui.js"></script>


</head>
<body>

	<div align="center" class="container">
		<h2 style="color: blue; text-align: center;">Signup here....</h2>
		<hr>
		<table>
			<form action="signupPost" method="POST" modelAttribute="signUpdomain"
				enctype="multipart/form-data">
				<div class="form-inline">

					<label for="fullName">Name:</label> <input type="text"
						class="form-control" name="fullName" id="fullName"
						placeholder="Enter full name here">

				</div>
				<div class="form-inline">
					<label for="userName">UserName:</label> <input type="text"
						class="form-control" name="userName" id="userName"
						placeholder="Enter User name here">
				</div>
				<div class="form-inline">
					<label for="password">Password:</label> <input type="password"
						class="form-control" name="password" id="password"
						placeholder="Enter password">
				</div>


				<div class="form-inline">
					<label for="gender">Select Gender:</label>
				</div>
				<div class="radio">
					<label for="gender"><input type="radio" name="gender"
						id="gender" value="Male">Male</label>
				</div>

				<div class="radio">
					<label for="gender"><input type="radio" name="gender"
						id="gender" value="Female">Female</label>
				</div>
				<div class="radio">
					<label for="gender"><input type="radio" name="gender"
						id="gender" value="Others">Others</label>
				</div>

				<div class="form-inline">
					<label for="email">Email id:</label> <input type="email"
						class="form-control" name="email" id="email"
						placeholder="Enter emailid here">
				</div>

				<div class="form-inline">
					<label for="phNo">Phone No:</label> <input type="text"
						class="form-control" name="phNo" id="phNo"
						placeholder="Enter phone number ">
				</div>

				<div>
					<label for="dateOfBirth">Date of Birth</label> <input type="date"
						id="dateOfBirth" name="dateOfBirth">
				</div>


				<div>
					<label for="file">Upload profile pic</label> <input type="file"
						id="file" name="file">
				</div>



<<<<<<< HEAD


			<div class="form-inline">
				<label for="phNo">Phone No:</label> <input type="text"
					class="form-control" name="phNo" id="phNo" placeholder="Enter phone number ">
			</div>

			<div>
				<label for="dateOfBirth">Date of Birth</label> <input type="date"
					id="dateOfBirth" name="dateOfBirth">
			</div>

	
	<div>
		<label for="file">Upload profile pic</label> <input type="file"
			id="file" name="file">
	</div>



			<button type="reset" class="btn btn-info col-2">Reset</button>
			<button type="submit" class="btn btn-info col-2">Submit</button>
		</form>
=======
				<button type="reset" class="btn btn-primary" >Reset</button>
				<button type="submit" class="btn btn-primary">Submit</button>
			</form>
>>>>>>> branch 'master' of https://github.com/dsaha8598/Practicegit.git
	</div>

</body>
</html>
