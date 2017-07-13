package ua.adeptius.asterisk.json;


import ua.adeptius.asterisk.monitor.NewCall;

import java.text.SimpleDateFormat;

import static ua.adeptius.asterisk.monitor.NewCall.CallState.ANSWER;

public class RoistatPhoneCall {


    //    private String id;
    private String callee;
    private String caller;
    private String visit_id;
    private String marker;
    private String order_id;
    private int duration;
    private String file_url;
    private String status;
    private String google_client_id;
    private String yandex_client_id;
    private String date;
    private String comment;
    private String save_to_crm;
    private int answer_duration;
    private transient String roistatApiKey;
    private transient String roistatProjectNumber;


    public RoistatPhoneCall() {
    }

    public RoistatPhoneCall(NewCall call) {
//        Набранный номер
        this.callee = call.getCalledTo();

//        Номер клиента
        this.caller = call.getCalledFrom();

//      null or string Номер визита
        this.visit_id = null;

//        null or string Маркер рекламного канала
        this.marker = call.getUtm();

//        null or string     Номер заказа из CRM
        this.order_id = null;

//        Продолжительность звонка (в секундах)
        this.duration = call.getSecondsFullTime(); // полное время звонка

//        Ссылка на файл записи. В api отсутствует
//        this.file_url = "http://78.159.55.63/1.mp3";

//        Типы ROISTAT
//        ANSWER – звонок был принят и обработан сотрудником;
//        BUSY – входящий звонок был, но линия была занята;
//        NOANSWER – входящий вызов состоялся, но в течение времени ожидания ответа не был принят сотрудником;
//        CANCEL – входящий вызов состоялся, но был завершен до того, как сотрудник ответил;
//        CONGESTION – вызов не состоялся из-за технических проблем;
//        CHANUNAVAIL – вызываемый номер был недоступен;
//        DONTCALL – входящий вызов был отменен;
//        TORTURE – входящий вызов был перенаправлен на автоответчик.

//        Мои типы
//        ANSWER, BUSY, FAIL
        NewCall.CallState state = call.getCallState();
        if (state == ANSWER) {
            this.status = "ANSWER";
        } else if (state == NewCall.CallState.BUSY) {
            this.status = "BUSY";
        } else if (state == NewCall.CallState.FAIL) {
            this.status = "CONGESTION";
        }else if (state == NewCall.CallState.CHANUNAVAIL) {
            this.status = "CHANUNAVAIL"; // вызываемый номер был недоступен
        }

//        В API отсутствует
        this.google_client_id = call.getGoogleId();

//        В API отсутствует
        this.yandex_client_id = null;

//        Дата и время создания записи о звонке (в формате UTC0)
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+0300").format(call.getCalledMillis()).replaceFirst(" ", "T");

//        Сохранение лида в CRM: 0 - не сохранять 1 - сохранять.
        this.save_to_crm = "0";

//      Текст комментария
        this.comment = null;

//      Время разговора
        this.answer_duration = call.getSecondsTalk();

        this.roistatApiKey = call.getUser().getRoistatAccount().getApiKey();
        this.roistatProjectNumber = call.getUser().getRoistatAccount().getProjectNumber();
    }

    public String getRoistatApiKey() {
        return roistatApiKey;
    }

    public String getRoistatProjectNumber() {
        return roistatProjectNumber;
    }
}
