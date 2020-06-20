<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>  
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
    <html>
    <head>
    <link rel="shortcut icon" type="image/png" href="images/b1.jp">
    </head>
    <body>
    <h1 style="color:green;text-align:center">THIS IS THE HOME LANDING PAGE UNDERCONSTRUCTION</h1>
    
    <form action="signupPost"   modelAttribute="domain" method="POST"
				enctype="multipart/form-data">
				<div class="form-inline">

					<label for="fullName">Name:</label> <input value="${domain.email }"/>
                     <label value="${domain.email}"></label>
				</div>
				</form>
    
    <a href="logOut?email=${domain.email}">logout</a><br>
    
    
    <a href="PasswordUpdation" class="btn btn-warning"
			role="button">Update Password</a>
    </body>
    
    </html>