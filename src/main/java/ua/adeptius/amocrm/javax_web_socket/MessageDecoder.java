package ua.adeptius.amocrm.javax_web_socket;


//import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import static ua.adeptius.amocrm.javax_web_socket.MessageEventType.wrongMessage;

public class MessageDecoder implements Decoder.Text<WsMessage> {

    private static Logger LOGGER = LoggerFactory.getLogger(WebSocket.class.getSimpleName());
//    private static Gson gson = new Gson();
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public WsMessage decode(String s) throws DecodeException {
        try {
//            return gson.fromJson(s, WsMessage.class);
            return mapper.readValue(s, WsMessage.class);
        }catch (Exception e){
            LOGGER.debug("Неверный синтаксис сообщения: {}", s);
            return new WsMessage(wrongMessage);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
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
