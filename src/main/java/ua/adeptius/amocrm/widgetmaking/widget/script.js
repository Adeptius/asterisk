define(['jquery'], function ($) {
    var CustomWidget = function () {
        var self = this;
        var isActiveTab = true;
        var domain = AMOCRM.constant("account").subdomain;
        var userId = AMOCRM.constant("user").id;
        // var wsUrl = 'wss://adeptius.pp.ua/tracking/ws/' + domain + '/' + userId;
        var wsUrl = 'wss://cstat.nextel.com.ua:8443/tracking/ws/' + domain + '/' + userId;
        var needToClose;
        var ws;


        this.callbacks = {
            render: function () {// Это не понадобится
                // console.log('render');
                return true;
            },
            init: function () {
                // console.log('init');
                initialize();
                return true;
            },
            bind_actions: function () {
                // console.log('bind_actions');
                AMOCRM.ifvisible.on('blur', function(){
                    isActiveTab = false;
                    // console.log('not active')
                });
                /*ws.close();*/
                AMOCRM.ifvisible.on('focus', function(){
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
                    }else if (eventType === 'copySession'){
                        needToClose = true;
                        ws.close();
                    }
                };
                ws.onclose = function () {
                    if (needToClose){
                        console.log("Соединение закрывается - дубль.");
                    }else {
                        console.log("Соединение с Nextel прервалось. Повторная попытка через 5 секунд..");
                        setTimeout(function () {
                            startWs()
                        }, 5000);
                    }
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

        var incomingCall = function (incomingMessage) {
            var calledFrom = incomingMessage.from;
            var createdDealId = incomingMessage.dealId;
            var asteriskId = incomingMessage.callId;
            var callPhase = incomingMessage.callPhase;

            jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + calledFrom, function (res) {
                var contactId, contactName, contactCompany, link_type;
                if (res != undefined && res.response != undefined && res.response.contacts != undefined) {
                    var contact = res.response.contacts[0];
                    contactId = contact.id;
                    contactName = contact.name;
                    contactCompany = contact.company_name;
                    if (contact.type == 'contact') {
                        link_type = 'contacts';
                    } else if (contact.type == 'company') {
                        link_type = 'companies';
                    }
                }
                jQuery.get('//' + window.location.host + '/private/api/v2/json/leads/list?id=' + createdDealId, function (res) {
                    var dealName;
                    if (res != undefined && res.response != undefined && res.response.leads != undefined) {
                        var deal = res.response.leads[0];
                        dealName = deal.name;
                    }

                    var notifierBody = '<p><a  href="/contacts/detail/'+contactId+'">'+contactName+'</a>';
                    notifierBody += (contactCompany ? ', '+ contactCompany : '')+'</p>';
                    notifierBody += '<p><a  href="/leads/detail/'+createdDealId+'">'+dealName+'</a></p>';

                    var notification = $('.popup-inbox');
                    notification.find('.notification-call').remove(); // удаляем существующие уведомления о звонках

                    var header = '';
                    if (callPhase === 'dial'){
                        header = 'Входящий звонок'
                    }else if (callPhase === 'answer'){
                        header = 'Вы разговариваете с'
                    }else if (callPhase === 'ended'){
                        header = 'Закончен разговор с'
                    }else if (callPhase === 'noanswer'){
                        header = 'Пропущен звонок'
                    }

                    AMOCRM.notifications.show_message({
                        type: 'call',
                        id: +asteriskId,
                        header: header,
                        text: notifierBody
                    });
                });
            });
        };
        return this;
    };
    return CustomWidget;
});