$(function () {

    var $button = $('.callButton');
    var $loginField = $('.form-control');

    $button.on('click', function () {
        var numbers = [];
        $loginField.each(function( index ) {
            numbers.push($(this).val());
        });

        var foundedNumber = null;

        for (var i = 0; i < numbers.length; i++) {
            var str = numbers[i];
            str = str.split(/\D+/).join("");
            if (str.length < 10){
                continue;
            }
            if (str.length > 10){
                str = str.substr(str.length-10);
            }
            if (str[0] != '0'){
                continue;
            }
            foundedNumber = str;
            break;
        }

        if (foundedNumber != null){
            $.get( "https://adeptius.pp.ua:8443/tracking/c2c/e404/0934027182", function( data ) {
                console.log(data);
            });
        }
    });
});