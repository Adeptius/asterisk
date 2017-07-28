define(['jquery',], function($) {
    var BinotelAmoWidget = function() {
        var self = this,
            ws,
            showDisconnectedError = false,
            widgetWasInitialized = false,
            isActiveTab = true;

        this.callbacks = {
            init: function() {
                $.getScript('//s3-eu-west-1.amazonaws.com/jsforamo/ws.client.js', function() {
                    /**
                     * Initialize WS
                     */
                    ws = createWs({
                        url: 'wss://ws.binotel.com:9002'
                    });


                    $.ajax({
                        type: 'POST',
                        url: 'https://my.binotel.ua/?module=ajax&action=getAccountData',
                        dataType: 'jsonp'
                    }).done(function(resp) {
                        /* self.get_settings().login */
                        if (resp.email && resp.sessionMask) {
                            ws.initMessage({
                                task: 'authLikeEmployee',
                                username: resp.email,
                                sessionMask: resp.sessionMask
                            });
                            ws.connect();
                        } else {
                            /*
                            AMOCRM.notifications.show_message_error({
                                header: 'Виджет Binotel не работает!',
                                text: 'Пройдите авторизацию в MyBinotel.'
                            });
                            */
                        }
                    });


                    ws.on('connect', function() {
                        showDisconnectedError = true;
                    });


                    ws.on('disconnect', function() {
                        if (showDisconnectedError) {
                            AMOCRM.notifications.show_message_error({
                                header: 'Виджет Binotel не работает!',
                                text: 'Обратитесь в техподдержку Binotel.'
                            });
                            showDisconnectedError = false;
                        }
                    });

                    ws.on('message', function(msg) {
                        if (msg === 'Bad username or password!') {
                            AMOCRM.notifications.show_message_error({
                                header: 'Виджет Binotel не работает!',
                                text: 'Пройдите авторизацию в MyBinotel.'
                            });
                            showDisconnectedError = false;
                            ws.close();
                        } else if (msg === 'You are logged in!') {
                            self.helpers.init();
                        } else if (msg.eventName === 'callStart' && isActiveTab) {
                            self.helpers.callStartHandler(msg);
                        } else if (msg.eventName === 'callAnswer' && isActiveTab) {
                            self.helpers.callAnswerHandler(msg);
                        } else if (msg.eventName === 'callStop') {
                            self.helpers.callStopHandler(msg);
                        }

                    });
                });

                return true;
            },

            render: function() {
                return true;
            },

            bind_actions: function() {
                AMOCRM.ifvisible.on('blur', function(){
                    isActiveTab = false;
                    /*ws.close();*/
                });
                AMOCRM.ifvisible.on('focus', function(){
                    isActiveTab = true;
                    /*ws.connect();*/
                });

                return true;
            },

            settings: function() {

            },

            onSave: function(fields) {
                return true;
            }
        };


        self.helpers = {};


        self.helpers.init = function() {
            if (widgetWasInitialized) {
                return;
            }

            /**
             * Mapping in amoCRM click to call function
             */
            self.add_action('phone', function(data) {
                self.helpers.clickToCall(data.value);
            });

            widgetWasInitialized = true;
        }


        self.helpers.clickToCall = function(phoneNumber) {
            ws.send({
                task: 'click2call',
                phoneNumber: phoneNumber
            });
        }


        self.helpers.callStartHandler = function(callData) {
            var phoneNumber = callData.callType === 0 ? callData.srcNumber : callData.dstNumber;

            self.helpers.searchContact(phoneNumber, function(contactId, contactName, contactCompany, link_type) {
                var notifierBody = '';
                if (contactId > 0) {
                    notifierBody += '<a class="js-navigate-link notification-call-link" href="/' + link_type + '/detail/'+ contactId +'">'+ contactName;
                    notifierBody += (contactCompany ? ', '+ contactCompany : '');
                    notifierBody += '</a>';
                } else {
                    notifierBody += '<a class="js-navigate-link notification-call-link" href="/contacts/add/?phone='+ encodeURIComponent(phoneNumber) +'">Создать контакт</a>';
                }

                /**
                 * If notification not exist, we show it.
                 */
                if (!$('#notification-popup-'+ callData.generalCallID).length) {
                    AMOCRM.notifications.show_message({
                        type: 'call',
                        id: callData.generalCallID,
                        header: phoneNumber,
                        text: notifierBody
                    });
                }
            });
        }


        self.helpers.callAnswerHandler = function(callData) {
            var phoneNumber = callData.callType === 0 ? callData.srcNumber : callData.dstNumber;

            self.helpers.searchContact(phoneNumber, function(contactId, contactName, contactCompany, link_type) {
                var notifierBody = '';

                if (contactId > 0) {
                    notifierBody += '<a class="js-navigate-link notification-call-link" href="/' + link_type + '/detail/'+ contactId +'">'+ contactName;
                    notifierBody += (contactCompany ? ', '+ contactCompany : '');
                    notifierBody += '</a>';
                } else {
                    notifierBody += '<a class="js-navigate-link notification-call-link" href="/contacts/add/?phone='+ encodeURIComponent(phoneNumber) +'">Создать контакт</a>';
                }

                /**
                 * If notification already exist, we need to hide it.
                 */
                if ($('#notification-popup-'+ callData.generalCallID).length) {
                    $('#notification-popup-'+ callData.generalCallID).remove();
                }

                AMOCRM.notifications.show_message({
                    type: 'call',
                    id: callData.generalCallID,
                    header: phoneNumber,
                    text: notifierBody
                });

                setTimeout(function() {
                    $('#notification-popup-'+ callData.generalCallID).find('div.nav__notifications__bar__inner-call').css('background', '#81D28B');
                    var activeNotifier = $('#notification-popup-'+ callData.generalCallID).find('div.nav__notifications__bar__text-call-info');
                    activeNotifier.css('color', '#fff');
                    activeNotifier.text(activeNotifier.text().replace('Входящий вызов от', 'Вы разговариваете с'));
                }, 50);
            });
        }


        self.helpers.callStopHandler = function(callData) {
            if ($('#notification-popup-'+ callData.generalCallID).length) {
                $('#notification-popup-'+ callData.generalCallID).remove();
            }
        }


        self.helpers.searchContact = function(subject, cb) {
            jQuery.get('//'+ window.location.host +'/private/api/v2/json/contacts/list/?type=all&query='+ subject, function(res) {
                var contactId, contactName, contactCompany, link_type;
                if (res != undefined && res.response != undefined && res.response.contacts != undefined) {

                    contactId = res.response.contacts[0].id;
                    contactName = res.response.contacts[0].name;
                    contactCompany = res.response.contacts[0].company_name;
                    if (res.response.contacts[0].type == 'contact') {
                        link_type = 'contacts';
                    } else if (res.response.contacts[0].type == 'company') {
                        link_type = 'companies';
                    }
                }

                cb(contactId, contactName, contactCompany, link_type);
            });
        }


        return this;
    };

    return BinotelAmoWidget;
});