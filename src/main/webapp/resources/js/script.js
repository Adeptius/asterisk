
$(document).ready(function () {
    var loginFromCoockie = $.cookie("tracklog");
    var passwordFromCoockie = $.cookie("trackpass");
    if (loginFromCoockie == null || passwordFromCoockie == null){
        window.location.href = '/tracking/login';
    }

    $.post('/tracking/userconfig/checklogin',
        {login: loginFromCoockie, password: passwordFromCoockie}, function (data) {
            if (data == 'true'){
                processAllIfAuthIsOk(loginFromCoockie, passwordFromCoockie);
            }else {
                window.location.href = '/tracking/login.html';
            }
        });
});

function processAllIfAuthIsOk(login, password){
    repeatedFunction();

    function repeatedFunction() {
        updatePhoneTable(login, password);
        updateLogs(login);
        setTimeout(repeatedFunction, 5000)
    }

    updateBlackList(login, password);

    $('#historyButton').on('click', function () {
        showHistory(login, password, $('#dateFrom').val(), $('#dateTo').val());
    });

    $('#buttonBlockIp').on('click', function () {
        addToBlackList(login, password, $('#ipToBlock').val());
    });

    $('#buttonUnBlockIp').on('click', function () {
        removeFromBlackList(login, password, $('#ipToBlock').val());
    });


    $('#exitButton').on('click', function () {
        $.cookie("tracklog",null);
        $.cookie("trackpass",null);
        window.location.href = '/tracking/login.html';
    });




    // $('#phonesTable .statusNumber').click(function () {
    //     alert('asdf');
    //     alert($(this).val())
    // });
}

function removeFromBlackList(login, password, ip) {
    $.post('/tracking/userconfig/removefromblacklist',
        {name: login, password: password, ip: ip}, function (data) {
            alert(data);
            updateBlackList(login, password);
            $('#ipToBlock').val('')
        });
}

function addToBlackList(login, password, ip) {
    $.post('/tracking/userconfig/addtoblacklist',
        {name: login, password: password, ip: ip}, function (data) {
            alert(data);
            updateBlackList(login, password);
            $('#ipToBlock').val('')
        });
}

function updateBlackList(login, password) {
    $.post('/tracking/converter/getblacklist',
        {name: login, password: password}, function (data) {
            $('#blackListArea').html(data);
        });
}

function showHistory(login, password, datefrom, dateto) {
    $.post('/tracking/converter/history',
        {name: login, password: password, dateFrom: datefrom, dateTo: dateto}, function (data) {
            $('#divHistory').html(data);
        });
}

function updatePhoneTable(login, password) {
    $.post('/tracking/converter/siteinfo', {name: login, password: password}, function (data) {
        $('#phonesTable').html(data);
    });
}

function updateLogs(login) {
    $.post('/tracking/converter/logs', {site: login}, function (data) {
        $('#logArea').text(data);
    }, 'text');
}



