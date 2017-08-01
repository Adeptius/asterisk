package ua.adeptius.amocrm.javax_web_socket;

import com.google.gson.Gson;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncoder implements Encoder.Text<WsMessage> {

    private static Gson gson = new Gson();

    @Override
    public String encode(WsMessage wsMessage) throws EncodeException {
        return gson.toJson(wsMessage);
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