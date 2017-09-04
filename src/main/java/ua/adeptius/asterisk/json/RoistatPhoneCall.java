package ua.adeptius.asterisk.json;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import ua.adeptius.asterisk.model.Call;

import java.text.SimpleDateFormat;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static ua.adeptius.asterisk.model.Call.CallState.ANSWER;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE, setterVisibility = NONE)
public class RoistatPhoneCall {

    @JsonProperty
    private String callee;

    @JsonProperty
    private String caller;

    @JsonProperty
    private String marker;

    @JsonProperty
    private int order_id;

    @JsonProperty
    private int duration;

    @JsonProperty
    private String file_url;

    @JsonProperty
    private String status;

    @JsonProperty
    private String google_client_id;

    @JsonProperty
    private String date;

    @JsonProperty
    private String comment;

    @JsonProperty
    private int answer_duration;


    private String save_to_crm;
    private String visit_id;
    private String yandex_client_id;
    private transient String roistatApiKey;
    private transient String roistatProjectNumber;

    public RoistatPhoneCall() {
    }

    public RoistatPhoneCall(Call call) {
//        Набранный номер
        this.callee = call.getCalledTo();

//        Номер клиента
        this.caller = call.getCalledFrom();

//      null or string Номер визита
        this.visit_id = null;

//        null or string Маркер рекламного канала
        this.marker = call.getUtm();

//        null or string     Номер заказа из CRM
//        int amoDealId = call.getAmoDealId();//todo
//        if (amoDealId != 0) {
//            this.order_id = amoDealId;
//        }

//        Продолжительность звонка (в секундах)
        this.duration = call.getSecondsToAnswer() + call.getSecondsTalk(); // полное время звонка


        String date = new SimpleDateFormat("yyyy-MM-dd").format(call.getCalledMillis());
//        Ссылка на файл записи. В api отсутствует
        this.file_url = "https://cstat.nextel.com.ua:8443/tracking/history/record/" + call.getAsteriskId() + "/" + date;

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
        Call.CallState state = call.getCallState();
        if (state == ANSWER) {
            this.status = "ANSWER";
        } else if (state == Call.CallState.BUSY) {
            this.status = "BUSY";
        } else if (state == Call.CallState.FAIL) {
            this.status = "CONGESTION";
        } else if (state == Call.CallState.CHANUNAVAIL) {
            this.status = "CHANUNAVAIL"; // вызываемый номер был недоступен
        } else if (state == Call.CallState.NOANSWER) {
            this.status = "NOANSWER"; // вызываемый номер был недоступен
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
        this.comment = "Nextel";

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

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getVisit_id() {
        return visit_id;
    }

    public void setVisit_id(String visit_id) {
        this.visit_id = visit_id;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGoogle_client_id() {
        return google_client_id;
    }

    public void setGoogle_client_id(String google_client_id) {
        this.google_client_id = google_client_id;
    }

    public String getYandex_client_id() {
        return yandex_client_id;
    }

    public void setYandex_client_id(String yandex_client_id) {
        this.yandex_client_id = yandex_client_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSave_to_crm() {
        return save_to_crm;
    }

    public void setSave_to_crm(String save_to_crm) {
        this.save_to_crm = save_to_crm;
    }

    public int getAnswer_duration() {
        return answer_duration;
    }

    public void setAnswer_duration(int answer_duration) {
        this.answer_duration = answer_duration;
    }

    public void setRoistatApiKey(String roistatApiKey) {
        this.roistatApiKey = roistatApiKey;
    }

    public void setRoistatProjectNumber(String roistatProjectNumber) {
        this.roistatProjectNumber = roistatProjectNumber;
    }
}
