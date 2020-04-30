package be.catsandcoding.pairprogramming.intellijplugin.communication;

import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.CommandMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.ui.MessageType;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

@Service
final public class CommunicationService {
    public static final int DEFAULT_PORT = 5570;
    public static final String DEFAULT_ADDRESS = "127.0.0.1";
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Pair Programming", NotificationDisplayType.BALLOON, true);
    private ZMQ.Socket zmqConnection;
    private ZContext context;
    private static final Random rand = new Random(System.nanoTime());
    private final String identity = String.format(
            "%04X-%04X", rand.nextInt(), rand.nextInt()
    );
    private String sessionId = "";
    private String host = DEFAULT_ADDRESS;
    private int port = DEFAULT_PORT;

    public void showNotification(String text){
        Notifications.Bus.notify(NOTIFICATION_GROUP.createNotification(text, MessageType.INFO));
    }

    public ZMQ.Socket createConnectionInContext(SocketType type){
        return context.createSocket(type);
    }

    public void stopConnectionInContext(ZMQ.Socket socket){
        socket.close();
        context.destroySocket(socket);
    }

    public String getIdentity(){
        return identity;
    }

    public String getSessionId(){
        return sessionId;
    }

    public void setHost(String host){
        this.host = host;
    }
    public String getHost(){
        return host;
    }
    public void setPort(int port){
        this.port = port;
    }
    public int getPort(){
        return port;
    }
    public int getSubPort(){
        return getPort() + 1;
    }


    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void startConnection(String host, int port) {
        setPort(port);
        setHost(host);
        context = new ZContext();
        zmqConnection = context.createSocket(SocketType.DEALER);
        zmqConnection.setIdentity(identity.getBytes(ZMQ.CHARSET));
        zmqConnection.connect(String.format("tcp://%s:%d", host, port));
    }
    private void sendMessage(String msg) {
        if(zmqConnection != null) {
            zmqConnection.send(msg.getBytes(ZMQ.CHARSET), 0);
        }
    }

    public void sendMessage(CommandMessage msg){
        try {
            msg.setActorId(getIdentity());
            msg.setSessionId(getSessionId());
            System.out.println("SENDING MSG: " + new ObjectMapper().writeValueAsString(msg));
            sendMessage(new ObjectMapper().writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    public void stopConnection() {
        zmqConnection.close();
        context.destroySocket(zmqConnection);
    }
}
