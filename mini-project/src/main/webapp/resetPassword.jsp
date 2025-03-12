<%@ page language="java" contentType="text/html; charset=US-ASCII"
        pageEncoding="US-ASCII"%>
 <%@ page isELIgnored="false" %>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>X-Workz</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-MrcW6ZMFYlzcLA8Nl+NtUVF0sA7MsXsP1UyJoMp4YLEuNSfAP+JcXn/tWtIaxVXM" crossorigin="anonymous"></script>
     <style>
                .form {
                    height: 50vh;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    background-color: #f8f9fa;
                }
                .custom-card {
                    width: 40vw;
                    height: 55vh;
                    padding: 20px;
                    border-radius: 10px;
                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                }
            </style>
</head>
<body>
<nav class="navbar navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand" href="#">
      <img src="https://x-workz.in/static/media/Logo.cf195593dc1b3f921369.png" alt="" width="125" height="70">
    </a>
    <li class="nav-item">
       </ul>
          <form class="d-flex" action="">
              <a class="nav-link btn-outline-light me-2 px-3 text-info" href="signin">Sign In</a>
              <a class="nav-link btn-primary px-3 text-dark fw-bold" href="index">Home</a>
          </form>
       </li>
  </div>
</nav>
<c:if test="${not empty errorMessage}">
    <h4 style="color: black; text-align: center;">${errorMessage}</h4>
</c:if>
<div class="d-flex justify-content-center align-items-center mt-5">
<div class="card custom-card">
    <div class="card-body">
        <h4 class="card-title text-center">Forget Password</h4>
<form action="resetPassword" method="post">
    <label class="form-label">Email Id</label>
    <input type="text" name="emailId" class="form-control">
    <label class="form-label">Password</label>
    <input type="password" name="password"  class="form-control">
    <label class="form-label">Password</label>
    <input type="password" name="newPassword" class="form-control">

    <label class="form-label">Confirm Password</label>
    <input type="password" name="confirmPassword" class="form-control">

    <div class="mb-3 text-center">
        <button type="submit" class="btn btn-primary mt-3">Submit</button>
    </div>
</form>
    </div>
</div>
</div>
</body>
</html>
