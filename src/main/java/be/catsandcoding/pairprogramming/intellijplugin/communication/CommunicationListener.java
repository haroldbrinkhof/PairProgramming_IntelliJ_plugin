package be.catsandcoding.pairprogramming.intellijplugin.communication;

import be.catsandcoding.pairprogramming.intellijplugin.communication.messages.*;
import be.catsandcoding.pairprogramming.intellijplugin.editing.ContentChangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.zeromq.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
final public class CommunicationListener {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private ContentChangeService contentChangeService;
    private final AtomicBoolean stopRunning = new AtomicBoolean();


    private void reset(){
        stopRunning.set(false);
    }
    public void stop(){
        stopRunning.set(true);
    }

    public void start(Project project) {
        reset();
        Application app = ApplicationManager.getApplication();
        if(app != null) {
            contentChangeService = ServiceManager.getService(project, ContentChangeService.class);
            app.executeOnPooledThread(new ListenAndAct());
        } else {
            throw new IllegalStateException("No application returned");
        }
    }

    private class ListenAndAct implements Runnable {
        private final ZMQ.Socket zmqConnection = communicationService.createConnectionInContext(SocketType.SUB);


        @Override
        public void run() {
            zmqConnection.subscribe(communicationService.getSessionId().getBytes(ZMQ.CHARSET));
            zmqConnection.connect(String.format("tcp://%s:%d",communicationService.getHost(), communicationService.getSubPort()));
            System.out.println("CommunicationListener started: " + zmqConnection);

            listenForCommandsAndActOnThem();

            System.out.println("CommunicationListener closed");
            communicationService.stopConnectionInContext(zmqConnection);
        }

        private void listenForCommandsAndActOnThem() {
            do {
                try {
                    String command = zmqConnection.recvStr();

                    if (!command.isEmpty() && !command.equals(communicationService.getSessionId())) {
                        handleCommand(command);
                    }
                    Thread.sleep(10);
                } catch(InterruptedException | IOException e){
                   e.printStackTrace();
                }
            } while(!stopRunning.get());
        }

        private void handleCommand(String command) throws JsonProcessingException {
            ObjectMapper permissiveMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectMapper mapper = new ObjectMapper();

            CommandMessage commandMessage = permissiveMapper.readValue(command, CommandMessage.class);
            if(weIssuedThis(commandMessage)) return;
            System.out.println(String.format("We[%s] didn't issue this: %s",communicationService.getIdentity(),commandMessage.getActorId()));
            System.out.println(commandMessage.getCommandMessageType() + " by " + commandMessage.getActorId());

            switch(commandMessage.getCommandMessageType()){
                case CONTENT_CHANGE:
                    ContentChangeMessage ctMsg = mapper.readValue(command, ContentChangeMessage.class);
                    contentChangeService.handle(ctMsg);
                    break;
                case DELETE_FILE:
                    DeleteFileMessage dfMsg = mapper.readValue(command, DeleteFileMessage.class);
                    contentChangeService.handle(dfMsg);
                    break;
                case NEW_FILE:
                    CreateFileMessage cfMsg = mapper.readValue(command, CreateFileMessage.class);
                    contentChangeService.handle(cfMsg);
                    break;
                case RENAME_FILE:
                    RenameFileMessage rnMsg = mapper.readValue(command, RenameFileMessage.class);
                    contentChangeService.handle(rnMsg);
                    break;
                case COPY_FILE:
                    CopyFileMessage cpMsg = mapper.readValue(command, CopyFileMessage.class);
                    contentChangeService.handle(cpMsg);
                    break;
                case COPY_OUTSIDE_FILE:
                    CopyOutsideFileMessage cpoMsg = mapper.readValue(command, CopyOutsideFileMessage.class);
                    contentChangeService.handle(cpoMsg);
                    break;
                case MOVE_FILE:
                    MoveFileMessage mvMsg = mapper.readValue(command, MoveFileMessage.class);
                    contentChangeService.handle(mvMsg);
                    break;
                case WHOLE_FILE_CONTENT_CHANGE:
                    CompleteFileContentChangeMessage ccMsg = mapper.readValue(command, CompleteFileContentChangeMessage.class);
                    contentChangeService.handle(ccMsg);
            }

        }

        private boolean weIssuedThis(CommandMessage commandMessage) {
            return commandMessage.getActorId().equals(communicationService.getIdentity());
        }
    }
}
