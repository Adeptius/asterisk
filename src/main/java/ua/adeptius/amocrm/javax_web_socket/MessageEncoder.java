package ua.adeptius.amocrm.javax_web_socket;

//import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.webcontrollers.AudioWebController;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class MessageEncoder implements Encoder.Text<WsMessage> {

    private static ObjectMapper mapper = new ObjectMapper();
    private static Logger LOGGER = LoggerFactory.getLogger(MessageEncoder.class.getSimpleName());

    @Override
    public String encode(WsMessage wsMessage) throws EncodeException {
        try {
            return mapper.writeValueAsString(wsMessage);
        } catch (IOException e) {
            LOGGER.error("ошибка в енкодере WS", e);
            throw new EncodeException(new Object(), e.getMessage());
        }
//        return gson.toJson(wsMessage);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}