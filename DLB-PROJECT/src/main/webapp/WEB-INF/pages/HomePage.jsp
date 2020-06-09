<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<!DOCTYPE html>

<html lang="en">
<head>

<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<style type="text/css">
.container {
	height: 325px;
	width: 400px;
	padding: 50px;
	margin: 30px;
	margin-left: 500px;
	margin-top: 50px;
	box-shadow: 0px 20px 30px black;
	border: 2px ridge darkcyan;
	border-radius: 20px;
}

body {
	height: 100%;
	background: rgba(231, 232, 234, 0.5);
}
</style>
</head>



<body>


	<div class="container" align="center">
		<h1 style="color: olive; text-align: center;">Welcome Dear</h1>
		<br> <br> <a href="login" class="btn btn-warning"
			role="button">LOGIN HERE</a> <a href="signupNew"
			class="btn btn-warning" role="button">SignUp HERE</a>

	</div>

</body>


</html>