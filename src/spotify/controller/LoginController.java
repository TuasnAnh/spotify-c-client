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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author ADMIN
 */
public class LoginController {

    private Socket clientSocket;
    private String host;
    private int port;
    private DataInputStream serverRes;
    private DataOutputStream serverReq;
    private int userId = -1;

    @FXML
    private TextField usernameText;

    @FXML
    private PasswordField passwordText;

    @FXML
    private Button loginButton;

    @FXML
    private void login(ActionEvent event) throws IOException {
        String username = usernameText.getText();
        String password = passwordText.getText();

        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                clientSocket = new Socket(host, port);
                serverRes = new DataInputStream(clientSocket.getInputStream());
                serverReq = new DataOutputStream(clientSocket.getOutputStream());

                serverReq.writeBytes("login");
                serverReq.writeByte('\n');
                serverReq.writeBytes(username);
                serverReq.writeByte('\n');
                serverReq.writeBytes(password);
                serverReq.writeByte('\n');

                if (serverRes.readBoolean()) {
                    userId = serverRes.readInt();
                    System.out.println(userId);
                    closeStream();
                    Stage stage = (Stage) loginButton.getScene().getWindow();

                    URL url = Paths.get("src/spotify/view/MyScene.fxml").toUri().toURL();
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.setTitle("Spotify C");
                    stage.show();
                    
                    homeViewController mainController = loader.<homeViewController>getController();
                    mainController.initialize(host, port, userId, mainController);
                } else {
                    usernameText.clear();
                    passwordText.clear();
                    closeStream();
                }

            } catch (IOException e) {
                System.out.println("Socket error");
//                usernameText.clear();
//                passwordText.clear();
            }
        }
    }

    private void closeStream() throws IOException {
        clientSocket.close();
        serverReq.close();
        serverRes.close();
    }

    @FXML
    private void goToRegisterView(ActionEvent event) throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();

        URL url = Paths.get("src/spotify/view/ViewRegister.fxml").toUri().toURL();
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        RegisterController registerController = loader.<RegisterController>getController();
        registerController.initialize(host, port, false);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public void initialize(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }
    
}
