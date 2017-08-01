define(['jquery'], function ($) {
    var CustomWidget = function () {
        var self = this;
        system = self.system();

        var URL = 'http://78.159.55.63:8080/tracking/';
        var user = AMOCRM.constant("user");
        var account = AMOCRM.constant("account");
        var domain = account.subdomain;
        var userId = user.id;
        var userName = user.name;
        var userLogin = user.login;
        var wsUrl = 'wss://adeptius.pp.ua/tracking/ws/' + domain + '/' + userId;

        this.callbacks = {
            render: function () {// Это не понадобится
                console.log('render');
                return true;
            },
            init: function () {
                console.log('init');

                var ws;

                function startWs() {
                    ws = new WebSocket(wsUrl);
                    ws.sendObject = function (obj) {
                        this.send(JSON.stringify(obj))
                    };
                    ws.onopen = function () {
                        console.log("Соединение открылось");
                    };
                    ws.onmessage = function (event) {
                        var incomingMessage = JSON.parse(event.data);
                        console.log("Пришло сообщение с содержанием:", incomingMessage);
                        var eventType = incomingMessage.eventType;
                        if (eventType === 'incomingCall') {
                            incomingCall(incomingMessage)
                        }


                    };
                    ws.onclose = function () {
                        console.log("Соединение закрылось. Повтор через 5 секунд");
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
                return true;
            },
            bind_actions: function () {
                console.log('bind_actions');
                return true;
            },
            settings: function () {// Это не понадобится
                return true;
            },
            onSave: function () {// Это не понадобится
                console.log('Нажато сохранение в окне виджетов');
                return true;
            },
            destroy: function () {

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


        var incomingCall = function (incomingMessage) {
            var calledFrom = incomingMessage.from;
            var createdDealId = incomingMessage.dealId;
            var asteriskId = incomingMessage.callId;
            var callPhase = incomingMessage.callPhase;

            jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + subject, function (res) {
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

                    var notification = $('#notification-popup-'+ asteriskId);
                    if (notification.length) {
                        notification.remove();
                    }

                    var header = '';
                    if (callPhase === 'dial'){
                        header = 'Входящий звонок'
                    }else if (callPhase === 'answer'){
                        header = 'Вы разговариваете с'
                    }else if (callPhase === 'ended'){
                        header = 'Закончен разговор с'
                    }

                    AMOCRM.notifications.show_message({
                        type: 'call',
                        id: asteriskId,
                        header: header,
                        text: notifierBody
                    });
                });
            });


            // searchContact(calledFrom, createdDealId, function (contactId, contactName, contactCompany, link_type, dealName) {
            //
            //     var notifierBody = '<p><a  href="/contacts/detail/'+contactId+'">'+contactName+'</a>';
            //     notifierBody += (contactCompany ? ', '+ contactCompany : '')+'</p>';
            //     notifierBody += '<p><a  href="/leads/detail/'+createdDealId+'">'+dealName+'</a></p>';
            //
            //     var notification = $('#notification-popup-'+ asteriskId);
            //     if (notification.length) {
            //         notification.remove();
            //     }
            //
            //     var header = '';
            //     if (callPhase === 'dial'){
            //         header = 'Входящий звонок'
            //     }else if (callPhase === 'answer'){
            //         header = 'Вы разговариваете с'
            //     }else if (callPhase === 'ended'){
            //         header = 'Закончен разговор с'
            //     }
            //
            //     AMOCRM.notifications.show_message({
            //         type: 'call',
            //         id: asteriskId,
            //         header: header,
            //         text: notifierBody
            //     });
            // });
        };


        // searchContact = function (subject,createdDealId, cb) {
        //     jQuery.get('//' + window.location.host + '/private/api/v2/json/contacts/list/?type=all&query=' + subject, function (res) {
        //         var contactId, contactName, contactCompany, link_type;
        //         if (res != undefined && res.response != undefined && res.response.contacts != undefined) {
        //             var contact = res.response.contacts[0];
        //             contactId = contact.id;
        //             contactName = contact.name;
        //             contactCompany = contact.company_name;
        //             if (contact.type == 'contact') {
        //                 link_type = 'contacts';
        //             } else if (contact.type == 'company') {
        //                 link_type = 'companies';
        //             }
        //         }
        //         jQuery.get('//' + window.location.host + '/private/api/v2/json/leads/list?id=' + createdDealId, function (res) {
        //             var dealName;
        //             if (res != undefined && res.response != undefined && res.response.leads != undefined) {
        //                 var deal = res.response.leads[0];
        //                 dealName = deal.name;
        //             }
        //
        //             cb(contactId, contactName, contactCompany, link_type, dealName);
        //         });
        //     });
        // };


        return this;
    };
    return CustomWidget;
});


// отправка POST
// self.crm_post(URL + '/tracking/widget/c2c', {
//         amoUserId: user.id,
//         amoUserName: user.name,
//         amoUserLogin: user.login,
//         pressedPhone: pressedPhone
//     },
//     function (msg) {
//         alert(msg)
//     }, 'text'// json
// );