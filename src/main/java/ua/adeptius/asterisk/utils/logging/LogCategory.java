package ua.adeptius.asterisk.utils.logging;


public enum LogCategory {

    REQUEST_NUMBER,
    BLOCKED_BY_IP,
    REPEATED_REQUEST,
    SENDING_NUMBER,
    NUMBER_FREE,
    NO_NUMBERS_LEFT,
    INCOMING_CALL,
    ANSWER_CALL,
    ENDED_CALL,
    INCOMING_CALL_NOT_REGISTER,
    DB_ERROR_CONNECTING,
    ERROR_SENDING_ANALYTICS,
    MAIL_SENDING_LOG,
    MAIL_SENDING_ERRORS,
    ONLY_ACTIVE_SITE,
    PHONE_TIME_REPORT,
    DB_OPERATIONS,
    ELSE
}