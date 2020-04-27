package be.catsandcoding.pairprogramming.intellijplugin.communication;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.ui.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

@Service
final public class CommunicationService {
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Pair Programming", NotificationDisplayType.BALLOON, true);
    private PrintWriter out;
    private BufferedReader in;
    private Socket connection;
    private ZMQ.Socket zmqConnection;
    private ZContext context;
    private boolean zmqMsgSend = false;
    private static Random rand = new Random(System.nanoTime());
    private String identity = String.format(
            "%04X-%04X", rand.nextInt(), rand.nextInt()
    );

    public void showNotification(String text){
        Notifications.Bus.notify(NOTIFICATION_GROUP.createNotification(text, MessageType.INFO));
    }
    public boolean isConnected(){
        return (connection != null && connection.isConnected());
    }

    public ZContext getContext(){
        return context;
    }
    public String getIdentity(){
        return identity;
    }

    public void startConnection(String host, int port) throws IOException{
        if(isConnected()) stopConnection();

        context = new ZContext();
            System.out.println("Connecting to hello world server");

            //  Socket to talk to server
            zmqConnection = context.createSocket(SocketType.DEALER);
        //  Set random identity to make tracing easier
        zmqConnection.setIdentity(identity.getBytes(ZMQ.CHARSET));

            zmqConnection.connect("tcp://127.0.0.1:5570");


        //connection = new Socket(host, port);
        //out = new PrintWriter(connection.getOutputStream(), true);
        //in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        //out.println("connection established from intellij");


    }
    public String sendMessage(String msg) throws IOException {
        if(zmqConnection != null) {
            zmqConnection.send(msg.getBytes(ZMQ.CHARSET), 0);
            zmqMsgSend = true;
        }
        //out.println(msg);
        //return listen();
        return "";
    }

    public String listen() throws IOException {
        ZMQ.Poller poller;
        poller = context.createPoller(1);
        poller.register(zmqConnection, ZMQ.Poller.POLLIN);
        poller.poll(10);
        String response = "";
        if (zmqConnection != null && poller.pollin(0)) {
            ZMsg msg = ZMsg.recvMsg(zmqConnection);
            response = String.format("ZMQ message: {%s}", msg.getLast().toString());
            msg.getLast().print(identity);
            msg.destroy();
        }
        return response;
        /*
        String received = "";
        if(zmqConnection != null && zmqMsgSend) {
            byte[] reply = zmqConnection.recv(0);
            received += "ZMQ Received " + new String(reply, ZMQ.CHARSET);
            zmqMsgSend = false;
        }
        received += (connection == null || in == null || connection.getInputStream().available() < 1)? "" : "RAW: " + in.readLine();
        return received;

         */
    }

    public void stopConnection() throws IOException {
        if(in != null) in.close();
        if(out != null) out.close();
        if(connection != null) connection.close();
    }
}
