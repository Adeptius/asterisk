package ua.adeptius.amocrm.javax_web_socket;

//import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

public class MessageEncoder implements Encoder.Text<WsMessage> {

//    private static Gson gson = new Gson();
    private static ObjectMapper mapper = new ObjectMapper();


    @Override
    public String encode(WsMessage wsMessage) throws EncodeException {
        try {
            return mapper.writeValueAsString(wsMessage);
        } catch (IOException e) {
            System.out.println("ошибка в енкодере WS");
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