$(document).ready(function () {
    $('#enterButton').on('click', function () {
        checkLogin();
    });
});

function checkLogin() {
    var login = $('#login').val();
    var password = $('#password').val();
    $('#resultText').html('');
    isLoginAndPasswordRight(login,password);
}

function isLoginAndPasswordRight(login,password) {
    $.post('/tracking/getToken',
        {login: login, password: password}, function (data) {
            // $.cookie("tracklog",login);
            // $.cookie("trackpass",password);
            continueLoginProcess(data);
        });
}

function continueLoginProcess(response) {
    if (response.Status == 'Error'){
        $('#resultText').html('Неправильный логин или пароль');
    }else {
        var token = response.token;
        localStorage.setItem('token', token);
        window.location.href = '/tracking';
    }
}




