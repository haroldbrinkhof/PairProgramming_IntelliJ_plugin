package be.catsandcoding.pairprogramming.intellijplugin.communication;

import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.zeromq.*;

import java.io.IOException;
import java.util.logging.Logger;

@Service
final public class CommunicationListener {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private ContentChangeService contentChangeService;
    private boolean stopRunning = false;
    private Project project;


    private void reset(){
        stopRunning = false;
    }
    public void stop(){
        stopRunning = true;
    }

    public void start(Project project) {
        reset();
        Application app = ApplicationManager.getApplication();
        this.project = project;
        if(app != null) {
            contentChangeService = ServiceManager.getService(project, ContentChangeService.class);
            app.executeOnPooledThread(new ListenAndAct());
        } else {
            throw new IllegalStateException("No application returned");
        }
    }

    private class ListenAndAct implements Runnable {
        private ZContext context;
        private ZMQ.Socket zmqConnection;

        @Override
        public void run() {
            context = communicationService.getContext();
            zmqConnection = context.createSocket(SocketType.SUB);
            zmqConnection.subscribe("");

            zmqConnection.connect("tcp://127.0.0.1:5571");
            System.out.println("CommunicationListener started: " + zmqConnection );
            do {
                try {
                    System.out.println("waiting for a message from the server");
                    String command = zmqConnection.recvStr();
                    System.out.println("got a message from the server");

                    if (!command.isEmpty()) {
                        System.out.println(command);
                        //for(String url: contentChangeService.getProjectRoot()) {
                            System.out.println("PROJECT: " + contentChangeService.getProjectRoot());
                        //}
                        //communicationService.showNotification(command);
                        ObjectMapper mapper = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        CommandMessage commandMessage = mapper.readValue(command, CommandMessage.class);
                        if(commandMessage.getCommandMessageType() == CommandMessage.Type.CONTENT_CHANGE){
                            ContentChangeMessage ctMsg = mapper.readValue(command, ContentChangeMessage.class);
                            contentChangeService.performChange(ctMsg);
                        }
                    }
                    Thread.sleep(10);
                } catch(InterruptedException e){
                   e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while(!stopRunning);
            System.out.println("CommunicationListener closed");
            zmqConnection.close();
        }
    }
}
