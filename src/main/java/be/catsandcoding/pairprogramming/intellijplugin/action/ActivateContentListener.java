package be.catsandcoding.pairprogramming.intellijplugin.action;

import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService;
import be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationListener;
import be.catsandcoding.pairprogramming.intellijplugin.listener.BufferContentChangeListener;
import be.catsandcoding.pairprogramming.intellijplugin.ui.PairingDialogNotConnected;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class ActivateContentListener extends AnAction {
    private final CommunicationService communicationService = ServiceManager.getService(CommunicationService.class);
    private final CommunicationListener communicationListener = ServiceManager.getService(CommunicationListener.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        DataContext context = null;
        try {
            context = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(100);
        } catch(Exception ignored) {

        }
        if(context != null) {
            Project project = context.getData(CommonDataKeys.PROJECT);
            try {
                EditorFactory.getInstance().getEventMulticaster()
                        .addDocumentListener(new BufferContentChangeListener(project), project);
                PairingDialogNotConnected pd = new PairingDialogNotConnected();
                pd.shouldCloseOnCross();
                if(pd.showAndGet()){
                    System.out.println("password: " + pd.getPassword() + " session-name: " + pd.getSessionName());
                    try {
                        communicationService.startConnection(pd.getAddress(), pd.getPort());
                        communicationService.showNotification("Connection succeeded: " + pd.getAddress() + ":" + pd.getPort());
                        communicationListener.stop();
                        communicationListener.start(project);
                    } catch (IOException e) {
                        communicationService.showNotification("Connection failed: " + pd.getAddress() + ":" + pd.getPort());
                    }
                }

            } catch(Exception e){
                throw new RuntimeException("failure: " + e.getMessage(), e);
            }
        }
    }
}
