<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">

<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>




<style type="text/css">
body {
	background:rgba(231,232,234,0.5);
}

.container {
	margin-top: 170px;
}

input[type="email"] {
	background-color: rgb(211,211,211);
	transition: all 0.5s;
}

input[type="email"]:focus {
	background-color: rgb(126, 115, 109);
	color: white;
	font-size: 18px;
}

input[type="password"] {
	background-color: rgb(211,211,211);
	transition: all 0.5s;
}

input[type="password"]:focus {
	background-color: rgb(126, 115, 109);
	color: white;
	font-size: 18px;
}

.container {
	height: 325px;
	width: 400px;
	padding: 50px;
	margin: 30px;
	margin-left: 500px;
	margin-top: 100px;
	box-shadow: 0px 20px 30px black;
	border: 2px ridge darkcyan;
	border-radius: 20px;
}
</style>
</head>



<body>
	
	<div align="center" class="container">
		<h2 style="color: navyblue; text-align: center;">Login Here...</h2>
		<br>
		<form action="loinPostCredentials" modelAttribute="domain"
			method="post">
			<div class="form-inline">
				<label for="email">Email id:</label> <input type="email"
					class="form-control" name="email" 
					placeholder="Enter email">
			</div>
			<br>
			<div class="form-inline">
				<label for="password">Password:</label> <input type="password"
					class="form-control" name="password" 
					placeholder="Enter password">
			</div>

			<div class="checkbox">
				<label><input type="checkbox"> Remember me</label>
			</div>
			<button type="submit" class="btn btn-success">Submit</button>
			<br>
			<br>
				<a href="forgotpassword"
					class="btn btn-danger font-weight-bolder text-white" role="button">
					Forgot Password</a>
			
		</form>
       <b style="color: red; font-size: 20px; text-align: center;">${userMessage}</b>
	</div>
	
</body>
</html>
