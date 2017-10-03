function loadScript(url, callback) {
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    script.onreadystatechange = callback;
    script.onload = callback;
    head.appendChild(script)
}
var runMyCodeAfterJQueryLoaded = function () {
    (function (i, s, o, g, r, a, m) {
        i['GoogleAnalyticsObject'] = r;
        i[r] = i[r] || function () {
            (i[r].q = i[r].q || []).push(arguments)
        }, i[r].l = 1 * new Date();
        a = s.createElement(o), m = s.getElementsByTagName(o)[0];
        a.async = 1;
        a.src = g;
        m.parentNode.insertBefore(a, m)
    })(window, document, 'script', 'https://www.google-analytics.com/analytics.js', 'ga');
    ga('create', 'GOOGLETRACKINGID', 'auto');
    ga('send', 'pageview');
    $(document).ready(function () {
        $.getJSON("https://api.ipify.org?format=json", function (data) {
            var ip = '' + data.ip;
            if (ip.search(',') > 0) {
                ip = ip.substring(0, ip.indexOf(','));
            }
            var match = document.cookie.match('(?:^|;)\\s*_ga=([^;]*)');
            var raw = (match) ? decodeURIComponent(match[1]) : null;
            if (raw) {
                match = raw.match(/(\d+\.\d+)$/)
            }
            var gacid = (match) ? match[1] : null;
            var sPageURL = decodeURIComponent(window.location.search.substring(1));
            if (sPageURL == '') {
                sPageURL = 'null'
            }
            someRequest();
            function someRequest() {
//                        var url = 'https://adeptius.pp.ua:8443/tracking/getNumber/e404/rutracker/' + gacid + '/' + ip + '/' + sPageURL + '/';
//                        $.get(url, function (phone) {
//                            $('.ct-phone').html(phone)
//                        });

                $.post('https://SERVERADDRESS/tracking/getNumber',
                    {user: 'LOGIN', site: 'SITENAME', googleId: gacid, ip: ip, pageRequest: sPageURL}, function (phone) {
                        $('.ct-phone').html(phone);
                    });
                setTimeout(someRequest, TIMETOUPDATE000)
            }
        })
    })
};
loadScript("https://code.jquery.com/jquery-1.12.4.min.js", runMyCodeAfterJQueryLoaded);
//Сжимать здесь http://info-pages.com.ua/service/3