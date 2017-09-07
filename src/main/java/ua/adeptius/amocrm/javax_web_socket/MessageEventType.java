package ua.adeptius.amocrm.javax_web_socket;

public enum MessageEventType {

    click2call, // отправляется с фронтенда параметр "callTo" номер телефона кому позвонить
    wrongMessage, // входящее сообщение с ошибкой синтаксиса
    incomingCall, // летит во фронтенд - оповещение о звонке
    outgoingCall, // после нажатия click2call используется что бы сообщить, что оператор действительно звонит
    wrongToNumber, //после нажатия click2call используется что бы сообщить, если номер неправильный
    noOperatorNumber // после нажатия click2call используется что бы сообщить, что номер оператора неизвестен
}
