<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%--<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>--%>

<%--<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <script src="js/jquery-3.1.1.min.js"></script>
    <link rel="stylesheet" href="css/login.css">
    <script src="js/login.js"></script>
    <script type="text/javascript" src="js/jquery.cookie.js"></script>
</head>

<body>
<div class="container">
    <label><b>Логин</b></label>
    <input id="login" type="text" placeholder="Введите логин" name="uname" required>

    <label><b>Пароль</b></label>
    <input id="password" type="password" placeholder="Введите пароль" name="psw" required>

    <p id="resultText"></p>
    <button id="enterButton">Войти</button>
</div>
</body>
</html>