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



	<style type="text/css">
	.container{
	margin-top: 170px;
	}
	
	</style>
</head>



<body>
<div align="center" >
	
</div>
<div align="center" class="container"  >
  <h2 style="color: orange;text-align: center;">Login Here.......</h2>
  <form action="loinPostCredentials" modelAttribute="domain" method="post"  >
    <div class="form-inline">
					<label for="email">Email id:</label> <input type="email"
						class="form-control" name="email" id="email"
						placeholder="Enter emailid here">
				</div>
   <div class="form-inline">
					<label for="password">Password:</label> <input type="password"
						class="form-control" name="password" id="password"
						placeholder="Enter password">
				</div>
    <div class="checkbox">
      <label><input type="checkbox"> Remember me</label>
    </div>
    <button type="submit" class="btn btn-default">Submit</button>
  </form>

</div>
  <b style="color:red;font-size: 50px;">${userMessage}</b>
</body> 
</html>
