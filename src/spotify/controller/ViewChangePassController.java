/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import spotify.controller.LoginController;
/**
 *
 * @author ADMIN
 */
public class ViewChangePassController {

    private Socket clientSocket;
    private String host;
    private int port;
    private DataInputStream serverRes;
    private DataOutputStream serverReq;

    @FXML
    private PasswordField oldPassText;
    @FXML
    private PasswordField newPassText;
    @FXML
    private Button changeButton;
    @FXML
    private TextField userNameText;
    @FXML
    private Label noti;

    /**
     * Initializes the controller class.
     */
    public void initialize(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    @FXML
    private void changePassword(ActionEvent event) throws IOException {
        String username = userNameText.getText();
        String oldpass = oldPassText.getText();
        String newpass = newPassText.getText();

        if (!username.isEmpty() && !oldpass.isEmpty() && !newpass.isEmpty()) {
            try {
                clientSocket = new Socket(host, port);
                serverRes = new DataInputStream(clientSocket.getInputStream());
                serverReq = new DataOutputStream(clientSocket.getOutputStream());

                serverReq.writeBytes("changepassword");
                serverReq.writeByte('\n');
                serverReq.writeBytes(username);
                serverReq.writeByte('\n');
                serverReq.writeBytes(oldpass);
                serverReq.writeByte('\n');
                serverReq.writeBytes(newpass);
                serverReq.writeByte('\n');

                if (serverRes.readBoolean()) {
                    clientSocket.close();
                    serverReq.close();
                    serverRes.close();
                    Stage stage = (Stage) changeButton.getScene().getWindow();

                    URL url = Paths.get("src/spotify/view/ViewLogin.fxml").toUri().toURL();
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Scene scene = new Scene(root);

                    LoginController loginController = loader.<LoginController>getController();
                    loginController.initialize(host, port);

                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
                } else {
                    noti.setText("Failed to change password");
                    clientSocket.close();
                    serverReq.close();
                    serverRes.close();
                }

            } catch (IOException e) {
                System.out.println("Socket Error!");
            }
            System.out.println("End change password");
        }
    }

}
