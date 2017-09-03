package ua.adeptius.asterisk.spring_config;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.adeptius.asterisk.json.Message;

import static ua.adeptius.asterisk.json.Message.Status.Error;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Перехват ошибки слишком большого файла из MelodiesWebController
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    public Object handleError1(MultipartException e, RedirectAttributes redirectAttributes) {
        String message = e.getCause().getMessage();
        if (message.contains("FileSizeLimitExceededException")){

            return new Message(Error, "File size is too big. Maximum 5mb");
        }
        return new Message(Error, "Upload error");
    }
}
