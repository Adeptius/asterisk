package ua.adeptius.asterisk.webcontrollers.interceptors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.adeptius.asterisk.json.Message;

import static ua.adeptius.asterisk.json.Message.Status.Error;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    @ResponseBody
    public Object handleError1(MultipartException e, RedirectAttributes redirectAttributes) {

        if (e.getCause().getMessage().contains("FileSizeLimitExceededException")){
            return new Message(Error, "File size is too big");
        }
        return "Upload error";
    }
}
