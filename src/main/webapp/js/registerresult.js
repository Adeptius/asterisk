$(document).ready(function () {
    var query = window.location.search;
    var key = query.substring(query.indexOf('=') + 1);

    $.post('/tracking/registration/key',
        {key: key}, function (result) {
            $('body').append($('<p>' + JSON.stringify(result) + '</p>'));
        });
});
