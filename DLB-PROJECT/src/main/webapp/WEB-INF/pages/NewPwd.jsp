<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<title>New Password</title>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head> 
<body>
<div align="center">
<h1 align="center"><font color="darkblue">Create New Password</font></h1>
<form>
<div><label for="Otp">Enter OTP:</label>
<input type="text"  placeholder="Enter otp" name="otp" required>
</div>
<div><label for="newpassword">New Password</label>
<input type="password" name="newPassWord" placeholder="Enter New Password">
</div>
<div><label for="cpassword">Confirm Password</label>
<input type="password" name="cPassWord" placeholder="Confirm Password">
</div>
<div><button type="submit">Ok</button></div>
</form>
</div>
</body>
</html>