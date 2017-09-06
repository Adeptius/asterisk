define(['jquery'], function ($) {
    var CustomWidget = function () {
        var self = this;
        var isActiveTab = true;
        var domain = AMOCRM.constant("account").subdomain;
        var userId = AMOCRM.constant("user").id;
        var wsUrl = 'wss://adeptius.pp.ua:8443/tracking/ws/' + domain + '/' + userId;
        // var wsUrl = 'wss://cstat.nextel.com.ua:8443/tracking/ws/' + domain + '/' + userId;
        var ws;

        $.fn.shake = function (intShakes, intDistance, intDuration) {
            this.each(function () {
                $(this).css("position", "relative");
                for (var x = 1; x <= intShakes; x++) {
                    $(this).animate({top: (intDistance * -1)}, (((intDuration / intShakes) / 4)))
                        .animate({top: intDistance}, ((intDuration / intShakes) / 2))
                        .animate({top: 0}, (((intDuration / intShakes) / 4)));
                }
            });
            return this;
        };

        this.callbacks = {
            render: function () {// Это не понадобится
                // console.log('render');
                return true;
            },
            init: function () {
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
                        AMOCRM.notifications.show_message_error({
                            header: 'Ошибка',
                            text: 'К сожалению, звонок на номер ' + incomingMessage.content + ' нельзя осуществить'
                        });
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
                var contactId, contactName, contactCompany, link_type;
                if (res != undefined && res.response != undefined && res.response.contacts != undefined) {
                    var contact = res.response.contacts[0];
                    contactId = contact.id;
                    contactName = contact.name;
                    contactCompany = contact.company_name;
                }

                var notifierBody = '<p><a  href="/contacts/detail/' + contactId + '">' + contactName + ' </a>';
                notifierBody += (contactCompany ? ', ' + contactCompany : '') + '</p>';
                // notifierBody += '<p><a  href="/leads/detail/' + createdDealId + '">' + dealName + '</a></p>';

                var notification = $('.popup-inbox');
                notification.find('.notification-call').remove(); // удаляем существующие уведомления о звонках

                var header = 'Вы звоните ' + calledTo;

                AMOCRM.notifications.show_message({
                    type: 'call',
                    header: header,
                    text: notifierBody
                });
            });
        };


        var incomingCall = function (incomingMessage) {
            var calledFrom = incomingMessage.from;
            var dealId = incomingMessage.dealId;
            var callPhase = incomingMessage.callPhase;

            jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + calledFrom, function (res) {
                var contactId, contactName, companyName, companyId;
                if (res != undefined && res.response != undefined && res.response.contacts != undefined) {
                    var contact = res.response.contacts[0];
                    contactId = contact.id;
                    contactName = contact.name;
                    companyName = contact.company_name;
                    companyId = contact.linked_company_id;
                }

                jQuery.get('//' + window.location.host + '/private/api/v2/json/leads/list?id=' + dealId, function (res) {
                    var dealName;
                    if (res != undefined && res.response != undefined && res.response.leads != undefined) {
                        var deal = res.response.leads[0];
                        dealName = deal.name;
                    }

                    var body = '<p><a  href="/contacts/detail/' + contactId + '">' + contactName + '</a>';
                    if (companyId) {
                        body += ', <a  href="/companies/detail/' + companyId + '">' + companyName + '</a></p>';
                    } else {
                        body += '</p>';
                    }
                    body += '<p><a  href="/leads/detail/' + dealId + '">' + dealName + '</a></p>';

                    var header = '';
                    var shake = false;
                    var autoClose = false;
                    if (callPhase === 'dial') {
                        header = 'Входящий звонок';
                        shake = true;

                    } else if (callPhase === 'answer') {
                        header = 'Вы разговариваете с';

                    } else if (callPhase === 'ended') {
                        header = 'Закончен разговор с';
                        autoClose = true;

                    } else if (callPhase === 'noanswer') {
                        header = 'Пропущен звонок';
                        autoClose = true;
                    }
                    showNotification(body, header, shake, autoClose);
                });
            });
        };

        var showNotification = function (body, header, shake, autoClose) {
            var $nextelNotification = $('#nextel_notification');
            if ($nextelNotification.length) {
                $nextelNotification.slideUp(250, function () {
                    $nextelNotification.remove();
                    showNotificationAfterHide(body, header, shake, autoClose);
                });
            } else {
                showNotificationAfterHide(body, header, shake, autoClose);
            }
        };

        var showNotificationAfterHide = function (body, header, shake, autoClose) {
            var text = '<div id="nextel_notification" class="notification__item">\n' +
                '        <img class="popup-inbox__close" src="https://adeptiustest4.amocrm.ru/frontend/images/interface/inbox/close_notification.svg">\n' +
                '        <div class="notification-call__non-select">\n' +
                '            <img src="https://adeptiustest4.amocrm.ru/upl/nextel_widget/widget/images/logo_min.png" style="display: block; margin: auto auto;">\n' +
                '        </div>\n' +
                '        <div class="notification-call__container_text">\n' +
                '            <div>' + header + '</div>\n' +
                '            <div>' + body + '</div>\n' +
                '        </div>\n' +
                '    </div>';

            var $notification = $(text);

            $notification.css({
                'z-index': '101',
                'position': 'relative',
                'width': '364px',
                'left': '40%',
                'opacity': '1 !important;',
                'box-shadow': '0 -3px 25px 0 rgba(0, 0, 0, .13)'
            });

            var $notificationCloseButton = $notification.find('.popup-inbox__close');

            $notificationCloseButton.on('click', function () {
                $notification.remove();
            });

            $notification.hide();
            $('body').append($notification);
            $notification.slideDown(250, function () {
                if (shake) {
                    repeatedFunction();

                    function repeatedFunction() {
                        $notification.shake(4, 4, 500);
                        setTimeout(repeatedFunction, 2000)
                    }
                }
            });

            if (autoClose) {
                setTimeout(function () {
                    $notification.slideUp(250);
                }, 5000)
            }
        };

        return this;
    };
    return CustomWidget;
});