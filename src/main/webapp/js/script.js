var token;
$(document).ready(function () {
    token = localStorage.getItem('token');
    if (token == null || token == '') {
        window.location.href = '../tracking/login';
    }

    $('#exitButton').on('click', function () {
        $.cookie("tracklog", null);
        $.cookie("trackpass", null);
        window.location.href = '/tracking/login';
    });

    setToday();

    $('#timeResetButton').on('click', function () {
        setToday();
        $('#divHistory').html('');
    });
    processAllIfAuthIsOk();
});


function setToday() {
    $('#dateFrom').val(getDate() + ' 00:00:00');
    $('#dateTo').val(getDate() + ' 23:59:59');
}

function getDate() {
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var day = date.getUTCDate();
    if (month < 10) {
        month = '0' + month;
    }
    if (day < 10) {
        day = '0' + day;
    }
    return year + '-' + month + '-' + day;
}


function processAllIfAuthIsOk() {
    repeatedFunction();
    function repeatedFunction() {
        updatePhoneTable();
        updateLogs();
        updateBlackList();
        setTimeout(repeatedFunction, 5000)
    }

    updateBlackListTimer();


    $('#historyInButton').on('click', function () {
        showHistory($('#dateFrom').val(), $('#dateTo').val(), 'IN');
    });

    $('#historyOutButton').on('click', function () {
        showHistory($('#dateFrom').val(), $('#dateTo').val(), 'OUT');
    });

    $('#buttonBlockIp').on('click', function () {
        addToBlackList($('#ipToBlock').val());
    });

    $('#buttonUnBlockIp').on('click', function () {
        removeFromBlackList($('#ipToBlock').val());
    });

    $('#saveBlockTimeButton').on('click', function () {
        changeBlackListTimer($('#timeToBlock').val());
    });


}

function removeFromBlackList(ip) {
    $.ajax({
        url: '/tracking/blacklist/remove',
        type: 'post',
        headers: {
            Authorization: token
        },
        data: {
            "ip": ip
        },
        success: function (data) {
            alert(data.Message);
            updateBlackList();
            $('#ipToBlock').val('')
        }
    });
}

function addToBlackList(ip) {
    $.ajax({
        url: '/tracking/blacklist/add',
        type: 'post',
        headers: {
            Authorization: token
        },
        data: {
            "ip": ip
        },
        success: function (data) {
            alert(data.Message);
            updateBlackListTimer();
            updateBlackList();
            $('#ipToBlock').val('')
        }
    });
}

function changeBlackListTimer(timer) {
    $.ajax({
        url: '/tracking/tracking/get',
        type: 'post',
        headers: {
            Authorization: token
        },
        success: function (data) {
            var standartNumber = data.standartNumber;
            $.ajax({
                url: '/tracking/tracking/set',
                type: 'post',
                headers: {
                    Authorization: token
                },
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                data: JSON.stringify({
                    "standartNumber": standartNumber,
                    "timeToBlock": timer
                }),
                success: function (data) {
                    updateBlackListTimer();
                }
            });
        }
    });


    $.post('/tracking/userconfig/setblocktime',
        {name: login, password: password, time: timer}, function (data) {
            updateBlackListTimer(login, password);
            alert(data);
        });
}

function updateBlackListTimer() {
    $.ajax({
        url: '/tracking/tracking/get',
        type: 'post',
        headers: {
            Authorization: token
        },
        success: function (data) {
            $('#timeToBlock').val(data.timeToBlock);
        }
    });
}

function updateBlackList() {
    $.ajax({
        url: '/tracking/converter/getblacklist',
        type: 'post',
        headers: {
            Authorization: token
        },
        success: function (data) {
            $('#blackListArea').html(data);
        }
    });
}

function showHistory(datefrom, dateto, direction) {
    $.ajax({
        url: '/tracking/converter/history',
        type: 'post',
        data: {
            dateFrom: datefrom,
            dateTo: dateto,
            direction: direction
        },
        headers: {
            Authorization: token
        },
        success: function (data) {
            $('#divHistory').html(data);
        }
    });
}

function updatePhoneTable() {
    $.ajax({
        url: '/tracking/converter/siteinfo',
        type: 'post',
        headers: {
            Authorization: token
        },
        success: function (data) {
            $('#phonesTable').html(data);
        }
    });
}

function updateLogs() {
    $.ajax({
        url: '/tracking/converter/logs',
        type: 'post',
        data: {
            adminPassword: 'pthy0eds'
        },
        headers: {
            Authorization: token
        },
        success: function (data) {
            $('#logArea').text(data);
        }
    });
}



