define(['jquery'], function ($) {
    var CustomWidget = function () {
        var self = this;
        var isActiveTab = true;
        var domain = AMOCRM.constant("account").subdomain;
        var userId = AMOCRM.constant("user").id;
        var local = 'adeptius.pp.ua';
        // var local = 'cstat.nextel.com.ua';
        var wsUrl = 'wss://' + local + ':8443/tracking/ws/' + domain + '/' + userId;
        // var wsUrl = 'wss://cstat.nextel.com.ua:8443/tracking/ws/' + domain + '/' + userId;
        var ws;


        this.callbacks = {
            render: function () {// Это не понадобится
                // console.log('render');
                return true;
            },
            init: function () {
                //https://elrumordelaluz.github.io/csshake/
                // $('head').append('<link rel="stylesheet" type="text/css" href="https://adeptius.pp.ua:8443/tracking/csshake.min.css">');
                $('head').append('<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/gsap/1.20.2/TweenMax.min.js"></script>');

                //тестовая проверка уведомления звонка
                // repeatedFunction();
                // function repeatedFunction() {
                //     var body = '<p><a  href="/contacts/detail/' + 4898551 + '">Владимир</a>';
                //     body += ', <a  href="/companies/detail/' + 5141071 + '">Крутая компания</a></p>';
                //     body += '<p><a  href="/leads/detail/' + 1976961 + '">Сделка года</a></p>';
                //     showNotification(body, 'Входящий звонок', true, 1.6, true);
                //     setTimeout(repeatedFunction, 1500)
                // }

                initialize();
                return true;
            },
            bind_actions: function () {
                // console.log('bind_actions');
                AMOCRM.ifvisible.on('blur', function () {
                    isActiveTab = false;
                    // console.log('not active')
                });
                /*ws.close();*/
                AMOCRM.ifvisible.on('focus', function () {
                    isActiveTab = true;
                    // console.log('active')
                    /*ws.connect();*/
                });
                return true;
            },
            settings: function () {// Это не понадобится
                return true;
            },
            onSave: function () {// Это не понадобится
                // console.log('Нажато сохранение в окне виджетов');
                return true;
            },
            destroy: function () {
                // if (!isActiveTab){
                //     needToClose = true;
                //     ws.close();
                // }
            },
            contacts: {//select contacts in list and clicked on widget name
                selected: function () {
                    console.log('contacts');
                }
            },
            leads: {//select leads in list and clicked on widget name
                selected: function () {
                    console.log('leads');
                }
            },
            tasks: {//select taks in list and clicked on widget name
                selected: function () {
                    console.log('tasks');
                }
            }
        };

        var initialize = function () {
            function startWs() {
                ws = new WebSocket(wsUrl);
                ws.sendObject = function (obj) {
                    this.send(JSON.stringify(obj))
                };
                ws.onopen = function () {
                    console.log("Соединение с Nextel установлено.");
                };
                ws.onmessage = function (event) {
                    var incomingMessage = JSON.parse(event.data);
                    console.log("Пришло сообщение с содержанием:", incomingMessage);
                    var eventType = incomingMessage.eventType;
                    if (eventType === 'incomingCall' && isActiveTab) {
                        incomingCall(incomingMessage)

                    } else if (eventType === 'outgoingCall' && isActiveTab) {
                        outgoingCall(incomingMessage)

                    } else if (eventType === 'wrongToNumber') {
                        var body = 'К сожалению, звонок на номер ' + incomingMessage.content + ' нельзя осуществить';
                        var header = 'Ошибка';
                        showNotification(body, header, true, 10, false);

                    } else if (eventType === 'noOperatorNumber') {
                        var body = 'Ваш текущий номер телефона не указан в системе Nextel';
                        var header = 'Ошибка';
                        showNotification(body, header, true, 10, false);
                    }
                };
                ws.onclose = function () {
                    console.log("Соединение с Nextel прервалось. Повторная попытка через 5 секунд..");
                    setTimeout(function () {
                        startWs()
                    }, 5000);
                };
            }

            startWs();

            self.add_action("phone", function (data) {
                var pressedPhone = data.value;
                ws.sendObject({
                    eventType: 'click2call',
                    callTo: pressedPhone
                })
            });
        };


        var outgoingCall = function (incomingMessage) {
            var calledTo = incomingMessage.content;

            jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + calledTo, function (res) {
                var contactId, contactName, contactCompany, link_type, companyName, companyId;
                if (res != undefined && res.response != undefined && res.response.contacts != undefined) {
                    var contact = res.response.contacts[0];
                    contactId = contact.id;
                    contactName = contact.name;
                    companyName = contact.company_name;
                    companyId = contact.linked_company_id;
                }

                var body = '<p><a  href="/contacts/detail/' + contactId + '">' + contactName + '</a>';
                if (companyId) {
                    body += ', <a  href="/companies/detail/' + companyId + '">' + companyName + '</a></p>';
                } else {
                    body += '</p>';
                }

                var notification = $('.popup-inbox');
                notification.find('.notification-call').remove(); // удаляем существующие уведомления о звонках

                var header = 'Вы звоните ' + calledTo;

                showNotification(body, header, false, 20, false);
            });
        };


        var incomingCall = function (incomingMessage) {
            var calledFrom = incomingMessage.from;
            // var dealId = incomingMessage.dealId;
            var callPhase = incomingMessage.callPhase;

            jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + calledFrom, function (res) {
                var contactId, contactName, companyName, companyId;
                if (res !== undefined && res.response !== undefined && res.response.contacts !== undefined) {
                    var contact = res.response.contacts[0];
                    contactId = contact.id;
                    contactName = contact.name;
                    companyName = contact.company_name;
                    companyId = contact.linked_company_id;
                    var leads = contact.linked_leads_id;

                    var urlToFindDeal = "/private/api/v2/json/leads/list?";
                    for (var i = 0; i < leads.length; i++) {
                        if (i !== 0){
                            urlToFindDeal += "&";
                        }
                        urlToFindDeal += "id[]=" + leads[i];
                    }
                }else {

                    contactName = calledFrom;
                    urlToFindDeal = "/private/api/v2/json/leads/list?id[]=-1";

                }

                jQuery.get('//' + window.location.host + urlToFindDeal, function (res) {
                    var activeDeal;
                    if (res !== undefined && res.response !== undefined && res.response.leads !== undefined) {
                        var deals = res.response.leads;

                        for (var i = 0; i < deals.length; i++) {
                            var obj = deals[i];
                            var statusId = obj.status_id;
                            if (statusId !== '142' && statusId !== '143'){
                                activeDeal = obj;
                            }
                        }
                    }

                    var body;
                    if (contactId !== undefined && contactId !== null && contactId.length>1) {
                        body = '<p><a  href="/contacts/detail/' + contactId + '">' + contactName + '</a>';
                    }else {
                        body = '<p>' + contactName + '</a>';
                    }

                    if (companyId !== undefined && companyId !== null && companyId.length>1) {
                        body += ', <a  href="/companies/detail/' + companyId + '">' + companyName + '</a></p>';
                    } else {
                        body += '</p>';
                    }

                    if (activeDeal !== undefined && activeDeal !== null){
                        body += '<p><a  href="/leads/detail/' + activeDeal.id + '">' + activeDeal.name + '</a></p>';
                    }

                    var header = '';
                    var shake = false;
                    var autoClose = -1; // секунд до автоскрытия
                    var needToShowIncoming = false;

                    if (callPhase === 'dial') {
                        header = 'Входящий звонок';
                        shake = true;
                        autoClose = 3;
                        needToShowIncoming = true;

                    } else if (callPhase === 'answer') {
                        header = 'Вы разговариваете с';
                        autoClose = 1800;

                    } else if (callPhase === 'ended') {
                        header = 'Закончен разговор с';
                        autoClose = 10;

                    } else if (callPhase === 'noanswer') {
                        header = 'Пропущен звонок';
                        autoClose = 15;

                    }
                    showNotification(body, header, shake, autoClose, needToShowIncoming);
                });
            });
        };

        var showNotification = function (body, header, shake, autoClose, needToShowIncoming) {
            var text = '<div id="nextel_notification" class="notification__item">\n' +
                '        <img class="popup-inbox__close" src="https://adeptiustest4.amocrm.ru/frontend/images/interface/inbox/close_notification.svg">\n' +
                '        <div class="notification-call__non-select">\n' +
                // '            <img src="https://adeptiustest4.amocrm.ru/upl/nextel_widget/widget/images/logo.png" style="display: block; margin: auto auto;">\n' +
                '            <img src="https://ucarecdn.com/a24c0583-f815-4d4b-a421-0d0528dbdb93/logo_noti.png" style="display: block; margin: auto auto;">\n' +
                '        </div>\n' +
                '        <div class="notification-call__container_text">\n' +
                '            <div id="nextel_notification_header"><b>' + header + '</b></div>\n' +
                '            <div>' + body + '</div>\n' +
                '        </div>\n' +
                '    </div>';

            var $newNotification = $(text);

            $newNotification.css({
                'z-index': '101',
                'position': 'relative',
                'width': '364px',
                'height': '60px',
                'top': '30px',
                'left': '40%',
                'opacity': '1 !important;',
                'box-shadow': '0 -3px 25px 0 rgba(0, 0, 0, .13)',
                'background': '#3c4451',
                'color': '#ffffff'
            });

            $newNotification.find('a').css({
                color: '#f7fffc'
            });

            $newNotification.find('#nextel_notification_header').css({
                'font-weight': 'bold'
            });

            $newNotification.find('.notification-call__container_text').css({
                'padding-left': '10px'
            });

            // $newNotification.find('a').hover(function () {
            //     this.css({
            //         color: '#006aff'
            //     });
            // });

            var $notificationCloseButton = $newNotification.find('.popup-inbox__close');

            $notificationCloseButton.on('click', function () {
                $newNotification.remove();
            });


            // Если уведомление уже есть и это уведомление о входящем звонке - то старое удаляем, а новое вешаем без анимации
            // Если это другое уведомление - то красиво прячем и новое открываем
            var $oldNotification = $('#nextel_notification');
            var notificationIsAlreadyPresent = $oldNotification.length;
            var oldNotyfIsIncomingCall = $oldNotification.find('#nextel_notification_header').text() === 'Входящий звонок';

            if (notificationIsAlreadyPresent) {
                if (oldNotyfIsIncomingCall && needToShowIncoming) {
                    $oldNotification.remove();
                    $('body').append($newNotification);
                } else {
                    $oldNotification.slideUp(250, function () {
                        $oldNotification.remove();
                        $('body').append($newNotification);
                        $newNotification.hide();
                        $newNotification.slideDown(250);
                    });
                }

            } else {
                $('body').append($newNotification);
                $newNotification.hide();
                $newNotification.slideDown(250);
            }

            // repeatedFunction();
            // function repeatedFunction() {
            //     if (shake) {
            //         $newNotification.shake(4, 3, 500);
            //     }
            //     setTimeout(repeatedFunction, 1500)
            // }


            // https://elrumordelaluz.github.io/csshake/
            //css shake
            // setTimeout(function () {// ждём пока всё выплывет и только тогда трясём
            //     if (shake) {
            //         $newNotification.addClass('shake-rotate shake-constant');
            //         setTimeout(function () {
            //             $newNotification.removeClass('shake-rotate shake-constant')
            //         }, 500)
            //     }
            // }, 200);


            //js shake
            // setTimeout(function () {// ждём пока всё выплывет и только тогда трясём
            //     if (shake) {
            //         $newNotification.shake(4, 3, 500);
            //     }
            // }, 200);

            //TweenMax shaking
            setTimeout(function () {// ждём пока всё выплывет и только тогда трясём
                if (shake) {
                    shakeAnimation($newNotification)
                }
            }, 200);


            var delay = autoClose * 1000;
            setTimeout(function () {
                $newNotification.slideUp(250, function () {
                    $newNotification.remove();
                });
            }, delay)
        };

        //TwinMax shaking
        function shakeAnimation(element) {
            TweenMax.to(element, .1, {
                x: -7,
                ease: Quad.easeInOut
            });
            TweenMax.to(element, .1, {
                repeat: 4,
                x: 7,
                yoyo: true,
                delay: .1,
                ease: Quad.easeInOut
            });
            TweenMax.to(element, .1, {
                x: 0,
                delay: .1 * 4
            });
        }


        //jQuery shaking
        $.fn.shake = function (intShakes, intDistance, intDuration) {
            this.each(function () {
                $(this).css("position", "relative");
                for (var x = 1; x <= intShakes; x++) {
                    $(this).animate({top: (intDistance * -1)}, ((intDuration / intShakes) / 4))
                        .animate({top: intDistance}, ((intDuration / intShakes) / 2))
                        .animate({top: 0}, ((intDuration / intShakes) / 4));
                }
            });
            return this;
        };

        function loadjscssfile(filename, filetype) {
            if (filetype == "js") { //if filename is a external JavaScript file
                // alert('called');
                var fileref = document.createElement('script')
                fileref.setAttribute("type", "text/javascript")
                fileref.setAttribute("src", filename)
                alert('called');
            } else if (filetype == "css") { //if filename is an external CSS file
                var fileref = document.createElement("link")
                fileref.setAttribute("rel", "stylesheet")
                fileref.setAttribute("type", "text/css")
                fileref.setAttribute("href", filename)
            }
            if (typeof fileref != "undefined")
                document.getElementsByTagName("head")[0].appendChild(fileref)
        }

        return this;
    };
    return CustomWidget;
});