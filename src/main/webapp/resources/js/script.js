$(document).ready(function () {
    var loginFromCoockie = $.cookie("tracklog");
    var passwordFromCoockie = $.cookie("trackpass");
    if (loginFromCoockie == null || passwordFromCoockie == null){
        window.location.href = '/tracking/login';
    }

    setToday();

    $.post('/tracking/userconfig/checklogin',
        {login: loginFromCoockie, password: passwordFromCoockie}, function (data) {
            if (data == 'true'){
                processAllIfAuthIsOk(loginFromCoockie, passwordFromCoockie);
            }else {
                window.location.href = '/tracking/login.html';
            }
        });

    $('#timeResetButton').on('click', function () {
        setToday();
        $('#divHistory').html('');
    })
});

function setToday() {
    $('#dateFrom').val( getDate() + ' 00:00:00');
    $('#dateTo').val( getDate() + ' 23:59:59');
}

function getDate() {
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth() +1;
    var day = date.getUTCDate();
    if (month < 10){
        month = '0' + month;
    }
    if (day < 10){
        day = '0' + day;
    }
    return year + '-' + month + '-' + day;
}


function processAllIfAuthIsOk(login, password){
    repeatedFunction();
    function repeatedFunction() {
        updatePhoneTable(login, password);
        updateLogs(login);
        updateBlackList(login, password);
        setTimeout(repeatedFunction, 5000)
    }
    updateBlackListTimer(login, password);


    $('#historyButton').on('click', function () {
        showHistory(login, password, $('#dateFrom').val(), $('#dateTo').val());
    });

    $('#buttonBlockIp').on('click', function () {
        addToBlackList(login, password, $('#ipToBlock').val());
    });

    $('#buttonUnBlockIp').on('click', function () {
        removeFromBlackList(login, password, $('#ipToBlock').val());
    });

    $('#saveBlockTimeButton').on('click', function () {
        changeBlackListTimer(login, password, $('#timeToBlock').val());
    });

    $('#exitButton').on('click', function () {
        $.cookie("tracklog",null);
        $.cookie("trackpass",null);
        window.location.href = '/tracking/login.html';
    });
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

function changeBlackListTimer(login, password, timer) {
    $.post('/tracking/userconfig/setblocktime',
        {name: login, password: password, time: timer}, function (data) {
            updateBlackListTimer();
        });
}

function updateBlackListTimer(login, password) {
    $.post('/tracking/status/siteinfo',
        {name: login, password: password}, function (data) {
            $('#timeToBlock').val(data.timeToBlock);
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



