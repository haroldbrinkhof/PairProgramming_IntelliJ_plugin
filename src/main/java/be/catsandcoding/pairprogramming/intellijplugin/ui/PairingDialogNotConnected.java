package be.catsandcoding.pairprogramming.intellijplugin.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService.DEFAULT_ADDRESS;
import static be.catsandcoding.pairprogramming.intellijplugin.communication.CommunicationService.DEFAULT_PORT;

public class PairingDialogNotConnected extends DialogWrapper {
    private final JTextField sessionName = new JTextField();
    private final JTextField password = new JTextField();
    private final JToggleButton readOnly = new JCheckBox();
    private final JTextField address = new JTextField();
    private final JTextField port = new JTextField();

    public PairingDialogNotConnected(){
        super(true);
        init();
        setTitle("Pairing-session properties");

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        port.setText(Integer.toString(DEFAULT_PORT));
        address.setText(DEFAULT_ADDRESS);

        JLabel sessionNameLabel = new JLabel("Session name: ");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        dialogPanel.add(sessionNameLabel, constraints);

        sessionName.setText("sessionName");
        constraints.gridx = 1;
        dialogPanel.add(sessionName, constraints);

        JLabel passwordLabel = new JLabel("password: ");
        constraints.gridy = 1;
        constraints.gridx = 0;
        dialogPanel.add(passwordLabel, constraints);

        password.setText("password");
        constraints.gridx = 1;
        dialogPanel.add(password, constraints);

        JLabel readOnlyLabel = new JLabel("read-only: ");
        constraints.gridy = 2;
        constraints.gridx = 0;
        dialogPanel.add(readOnlyLabel, constraints);
        constraints.gridx = 1;
        dialogPanel.add(readOnly, constraints);

        JLabel addressLabel = new JLabel("address: ");
        constraints.gridy = 3;
        constraints.gridx = 0;
        dialogPanel.add(addressLabel, constraints);
        constraints.gridx = 1;
        dialogPanel.add(address, constraints);
        JLabel colonLabel = new JLabel(":");
        constraints.gridx = 2;
        dialogPanel.add(colonLabel, constraints);
        constraints.gridx = 3;
        dialogPanel.add(port, constraints);


        return dialogPanel;
    }
    private class ConnectAction extends DialogWrapperAction {
        public ConnectAction(){
            super("Connect");
        }

        @Override
        protected void doAction(ActionEvent actionEvent) {
            doOKAction();
        }
    }
    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{new ConnectAction(), getCancelAction()};
    }

    public String getPassword(){
        return password.getText();
    }
    public String getSessionName(){
        return sessionName.getText();
    }
    public String getAddress(){
        return address.getText();
    }
    public int getPort(){
        try {
            return Integer.parseInt(port.getText());
        } catch(NumberFormatException e) {
            return 0;
        }
    }
}
