define(['jquery'], function ($) {
    var CustomWidget = function () {
        var self = this;
        system = self.system();




        this.callbacks = {
            render: function () {
                console.log('render');
                return true;
            },
            init: function () {
                console.log('init');

                self.add_action('phone', function(data) {
                    self.helpers.clickToCall(data.value);
                });

                self.add_action("phone", function (data) {
                    console.log("data.value",data.value);
                    console.log('phone pressed');
                    console.log("this",this);
                    console.log("self",self);
                    console.log("system",system);


                    // var cardInfo = this.get_ccard_info();
                    // console.log("cardInfo",cardInfo);

                    var c_data = self.list_selected().selected;
                    console.log("c_data",c_data);

                    AMOCRM.notifications.show_message_error({
                        header: 'Виджет nextel работает',
                        text: 'всё круто'
                    });
                    showDisconnectedError = false;


                    var phones = $('.card-cf-table-main-entity .phone_wrapper input[type=text]:visible'),
                        emails = $('.card-cf-table-main-entity .email_wrapper input[type=text]:visible'),
                        name = $('.card-top-name input').val(),
                        data = [],
                        c_phones = [],
                        c_emails = [];
                    console.log(phones)
                    data.name = name;
                    for (var i = 0; i < phones.length; i++) {
                        if ($(phones[i]).val().length > 0) {
                            c_phones[i] = $(phones[i]).val();
                        }
                    }
                    data['phones'] = c_phones;
                    for (var i = 0; i < emails.length; i++) {
                        if ($(emails[i]).val().length > 0) {
                            c_emails[i] = $(emails[i]).val();
                        }
                    }
                    data['emails'] = c_emails;
                    console.log(data)



                });
                return true;
            },
            bind_actions: function () {
                console.log('bind_actions');
                return true;
            },
            settings: function () {
                return true;
            },
            onSave: function () {
                alert('Нажато сохранение в окне виджетов');
                return true;
            },
            destroy: function () {

            },
            contacts: {
                //select contacts in list and clicked on widget name
                selected: function () {
                    console.log('contacts');
                }
            },
            leads: {
                //select leads in list and clicked on widget name
                selected: function () {
                    console.log('leads');
                }
            },
            tasks: {
                //select taks in list and clicked on widget name
                selected: function () {
                    console.log('tasks');
                }
            }
        };

        self.helpers.clickToCall = function(phoneNumber) {
            console.log("CLICK TO CALL", phoneNumber);
        };

        return this;
    };

    return CustomWidget;
});