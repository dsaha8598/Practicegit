
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>forgot password</title>

<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">

<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<style type="text/css">



body {
	background: rgba(231, 232, 234, 0.5);
}



input[type="email"] {
	background-color: rgb(211, 211, 211);
	transition: all 0.5s;
}

input[type="email"]:focus {
	background-color: rgb(126, 115, 109);
	color: white;
	font-size: 18px;
}

.container {
	height: 200px;
	width: 350px;
	padding: 50px;
	margin: 30px;
	margin-left: 500px;
	margin-top: 150px;
	box-shadow: 0px 20px 30px black;
	border: 2px ridge darkcyan;
	border-radius: 20px;
}


</style>


</head>
<body>

	<div align="center" class="container">
		<form action="forgotpassword" modelAttribute="domain" method="post">
			<div class="form-inline">
				<label for="email">Email id:</label> <input type="email"
					class="form-control" name="email" placeholder="Enter email">
			</div>
			<br> 
	<div>
	<button type="submit" class="btn btn-success">Submit</button>
	</div>
	</form>
	</div>

</body>
</html>

